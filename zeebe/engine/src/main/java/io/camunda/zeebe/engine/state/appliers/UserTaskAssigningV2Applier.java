/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.appliers;

import io.camunda.zeebe.engine.state.TypedEventApplier;
import io.camunda.zeebe.engine.state.immutable.UserTaskState.LifecycleState;
import io.camunda.zeebe.engine.state.mutable.MutableProcessingState;
import io.camunda.zeebe.engine.state.mutable.MutableUserTaskState;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskEntity;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;

public final class UserTaskAssigningV2Applier
    implements TypedEventApplier<UserTaskIntent, UserTaskEntity> {

  private final MutableUserTaskState userTaskState;

  public UserTaskAssigningV2Applier(final MutableProcessingState processingState) {
    userTaskState = processingState.getUserTaskState();
  }

  @Override
  public void applyState(final long key, final UserTaskEntity value) {
    userTaskState.updateUserTaskLifecycleState(key, LifecycleState.ASSIGNING);
    userTaskState.storeIntermediateState(value, LifecycleState.ASSIGNING);
  }
}
