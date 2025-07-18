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
import io.camunda.zeebe.engine.state.mutable.MutableAsyncProcessingContext;
import io.camunda.zeebe.engine.state.mutable.MutableTaskState;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskListenerEventType;
import io.camunda.zeebe.protocol.impl.record.value.usertask.TaskRecord;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;
import java.util.List;

public final class TaskAssignedV2Applier
    implements TypedEventApplier<UserTaskIntent, TaskRecord> {

  private final MutableTaskState taskState;
  private final MutableElementInstanceState elementInstanceState;

  public TaskAssignedV2Applier(final MutableAsyncProcessingContext processingState) {
    taskState = processingState.getTaskState();
    elementInstanceState = processingState.getElementInstanceState();
  }

  @Override
  public void applyState(final long key, final TaskRecord value) {
    final var userTaskRecord = new TaskRecord();
    userTaskRecord.wrapWithoutVariables(value);
    taskState.update(userTaskRecord.setChangedAttributes(List.of()).setAction(""));
    taskState.updateUserTaskLifecycleState(key, LifecycleState.CREATED);

    // Clear operational data related to the current assign(claim) transition
    taskState.deleteIntermediateState(key);
    taskState.deleteRecordRequestMetadata(key);
    taskState.deleteInitialAssignee(key);

    final var elementInstance = elementInstanceState.getInstance(value.getElementInstanceKey());
    if (elementInstance != null) {
      final long scopeKey = elementInstance.getValue().getFlowScopeKey();
      final var scopeInstance = elementInstanceState.getInstance(scopeKey);
      if (scopeInstance != null && scopeInstance.isActive()) {
        elementInstance.resetTaskListenerIndex(ZeebeTaskListenerEventType.assigning);
        elementInstanceState.updateInstance(elementInstance);
      }
    }
  }
}
