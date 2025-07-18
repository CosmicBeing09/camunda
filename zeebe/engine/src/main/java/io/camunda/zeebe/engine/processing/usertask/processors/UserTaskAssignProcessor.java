/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.usertask.processors;

import io.camunda.zeebe.engine.processing.Rejection;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.StateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.immutable.ProcessingState;
import io.camunda.zeebe.engine.state.immutable.UserTaskState;
import io.camunda.zeebe.engine.state.immutable.UserTaskState.LifecycleState;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskEntity;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.util.Either;
import java.util.List;

public final class UserTaskAssignProcessor implements UserTaskCommandProcessor {

  private static final String DEFAULT_ACTION = "assign";

  private final UserTaskState userTaskState;
  private final StateWriter stateWriter;
  private final TypedResponseWriter responseWriter;
  private final UserTaskCommandPreconditionChecker preconditionChecker;

  public UserTaskAssignProcessor(
      final ProcessingState state,
      final Writers writers,
      final AuthorizationCheckBehavior authCheckBehavior) {
    userTaskState = state.getUserTaskState();
    stateWriter = writers.state();
    responseWriter = writers.response();
    preconditionChecker =
        new UserTaskCommandPreconditionChecker(
            List.of(LifecycleState.CREATED), "assign", state.getUserTaskState(), authCheckBehavior);
  }

  @Override
  public Either<Rejection, UserTaskEntity> validateCommand(
      final TypedRecord<UserTaskEntity> command) {
    return preconditionChecker.check(command);
  }

  @Override
  public void onCommand(
      final TypedRecord<UserTaskEntity> command, final UserTaskEntity userTaskEntity) {
    final long userTaskKey = command.getKey();

    final var newAssignee = command.getValue().getAssignee();
    if (!userTaskEntity.getAssignee().equals(newAssignee)) {
      userTaskEntity.setAssignee(newAssignee);
      userTaskEntity.setAssigneeChanged();
    }
    userTaskEntity.setAction(command.getValue().getActionOrDefault(DEFAULT_ACTION));

    stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.ASSIGNING, userTaskEntity);
  }

  @Override
  public void onFinalizeCommand(
      final TypedRecord<UserTaskEntity> command, final UserTaskEntity userTaskEntity) {
    final long userTaskKey = command.getKey();

    if (command.hasRequest()) {
      stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity);
      responseWriter.writeEventOnCommand(
          userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity, command);
    } else {
      final var requestDetails = userTaskState.findTriggerRequest(userTaskKey);
      stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity);

      requestDetails.ifPresent(
          requestDetails ->
              responseWriter.writeResponse(
                  userTaskKey,
                  UserTaskIntent.ASSIGNED,
                  userTaskEntity,
                  ValueType.USER_TASK,
                  requestDetails.getRequestId(),
                  requestDetails.getRequestStreamId()));
    }
  }
}
