/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.appliers;

import io.camunda.zeebe.engine.state.TypedEventApplier;
import io.camunda.zeebe.engine.state.immutable.TaskState.LifecycleState;
import io.camunda.zeebe.engine.state.mutable.MutableElementInstanceState;
import io.camunda.zeebe.engine.state.mutable.MutableProcessingState;
import io.camunda.zeebe.engine.state.mutable.MutableUserTaskState;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskListenerEventType;
import io.camunda.zeebe.protocol.impl.record.value.usertask.TaskRecord;
import io.camunda.zeebe.protocol.record.intent.TaskIntent;

public final class UserTaskUpdatedV2Applier
    implements TypedEventApplier<TaskIntent, TaskRecord> {

  private final MutableUserTaskState userTaskState;
  private final MutableElementInstanceState elementInstanceState;

  public UserTaskUpdatedV2Applier(final MutableProcessingState processingState) {
    userTaskState = processingState.getUserTaskState();
    elementInstanceState = processingState.getElementInstanceState();
  }

  @Override
  public void applyState(final long key, final TaskRecord value) {
    final TaskRecord userTask = userTaskState.getUserTask(key);
    userTask.wrapChangedAttributes(value, false);
    userTaskState.update(userTask);
    userTaskState.updateUserTaskLifecycleState(key, LifecycleState.CREATED);

    // Clear operational data related to the current update transition
    userTaskState.deleteIntermediateState(key);
    userTaskState.deleteRecordRequestMetadata(key);

    final var elementInstance = elementInstanceState.getInstance(value.getElementInstanceKey());
    if (elementInstance != null) {
      final long scopeKey = elementInstance.getValue().getFlowScopeKey();
      final var scopeInstance = elementInstanceState.getInstance(scopeKey);
      if (scopeInstance != null && scopeInstance.isActive()) {
        elementInstance.resetTaskListenerIndex(ZeebeTaskListenerEventType.updating);
        elementInstanceState.updateInstance(elementInstance);
      }
    }
  }
}
