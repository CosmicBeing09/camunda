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
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.util.Either;
import java.util.List;

public final class UserTaskClaimProcessor implements UserTaskCommandProcessor {

  private static final String DEFAULT_ACTION = "claim";

  private static final String INVALID_USER_TASK_ASSIGNEE_MESSAGE =
      "Expected to claim user task with key '%d', but it has already been assigned";
  private static final String INVALID_USER_TASK_EMPTY_ASSIGNEE_MESSAGE =
      "Expected to claim user task with key '%d', but provided assignee is empty";

  private final UserTaskState userTaskState;
  private final StateWriter stateWriter;
  private final TypedResponseWriter responseWriter;
  private final UserTaskCommandPreconditionChecker preconditionChecker;

  public UserTaskClaimProcessor(
      final ProcessingState state,
      final Writers writers,
      final AuthorizationCheckBehavior authCheckBehavior) {
    userTaskState = state.getUserTaskState();
    stateWriter = writers.state();
    responseWriter = writers.response();
    preconditionChecker =
        new UserTaskCommandPreconditionChecker(
            List.of(LifecycleState.CREATED),
            "claim",
            UserTaskClaimProcessor::checkClaim,
            state.getUserTaskState(),
            authCheckBehavior);
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

    stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.CLAIMING, userTaskEntity);
  }

  @Override
  public void onFinalizeCommand(
      final TypedRecord<UserTaskEntity> command, final UserTaskEntity userTaskEntity) {
    final long userTaskKey = command.getKey();

    userTaskEntity.setAssignee(command.getValue().getAssignee());
    userTaskEntity.setAction(command.getValue().getActionOrDefault(DEFAULT_ACTION));

    if (command.hasRequest()) {
      stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity);
      responseWriter.writeEventOnCommand(
          userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity, command);
    } else {
      final var triggerRequestDetails = userTaskState.findTriggerRequest(userTaskKey);
      stateWriter.appendFollowUpEvent(userTaskKey, UserTaskIntent.ASSIGNED, userTaskEntity);

      triggerRequestDetails.ifPresent(
          triggerRequestDetails ->
              responseWriter.writeResponse(
                  userTaskKey,
                  UserTaskIntent.ASSIGNED,
                  userTaskEntity,
                  ValueType.USER_TASK,
                  triggerRequestDetails.getRequestId(),
                  triggerRequestDetails.getRequestStreamId()));
    }
  }

  private static Either<Rejection, UserTaskEntity> checkClaim(
      final TypedRecord<UserTaskEntity> command, final UserTaskEntity userTaskRecord) {

    final long userTaskKey = command.getKey();
    final String newAssignee = command.getValue().getAssignee();
    if (newAssignee.isBlank()) {
      return Either.left(
          new Rejection(
              RejectionType.INVALID_STATE,
              String.format(INVALID_USER_TASK_EMPTY_ASSIGNEE_MESSAGE, userTaskKey)));
    }

    final String currentAssignee = userTaskRecord.getAssignee();
    final boolean canClaim = currentAssignee.isBlank() || currentAssignee.equals(newAssignee);
    if (!canClaim) {
      return Either.left(
          new Rejection(
              RejectionType.INVALID_STATE,
              String.format(INVALID_USER_TASK_ASSIGNEE_MESSAGE, userTaskKey)));
    }

    return Either.right(userTaskRecord);
  }
}
