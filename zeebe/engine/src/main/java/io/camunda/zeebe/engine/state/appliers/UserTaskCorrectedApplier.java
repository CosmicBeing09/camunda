/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.appliers;

import io.camunda.zeebe.engine.state.TypedEventApplier;
import io.camunda.zeebe.engine.state.mutable.MutableAsyncProcessingContext;
import io.camunda.zeebe.engine.state.mutable.MutableTaskState;
import io.camunda.zeebe.protocol.impl.record.value.usertask.TaskRecord;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;

public final class UserTaskCorrectedApplier
    implements TypedEventApplier<UserTaskIntent, TaskRecord> {

  private final MutableTaskState userTaskState;

  public UserTaskCorrectedApplier(final MutableAsyncProcessingContext state) {
    userTaskState = state.getTaskState();
  }

  @Override
  public void applyState(final long key, final TaskRecord value) {
    userTaskState.updateIntermediateState(
        key,
        intermediateStateValue ->
            intermediateStateValue
                .getRecord()
                .setAssignee(value.getAssigneeBuffer())
                .setDueDate(value.getDueDateBuffer())
                .setFollowUpDate(value.getFollowUpDateBuffer())
                .setCandidateGroupsList(value.getCandidateGroupsList())
                .setCandidateUsersList(value.getCandidateUsersList())
                .setPriority(value.getPriority()));
  }
}
