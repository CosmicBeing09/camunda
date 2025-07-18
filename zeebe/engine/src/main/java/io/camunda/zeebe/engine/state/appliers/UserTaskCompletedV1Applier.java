/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.appliers;

import io.camunda.zeebe.engine.state.TypedEventApplier;
import io.camunda.zeebe.engine.state.instance.ElementInstance;
import io.camunda.zeebe.engine.state.mutable.MutableElementInstanceState;
import io.camunda.zeebe.engine.state.mutable.MutableAsyncProcessingContext;
import io.camunda.zeebe.engine.state.mutable.MutableTaskState;
import io.camunda.zeebe.protocol.impl.record.value.usertask.TaskRecord;
import io.camunda.zeebe.protocol.record.intent.UserTaskIntent;

public final class UserTaskCompletedV1Applier
    implements TypedEventApplier<UserTaskIntent, TaskRecord> {

  private final MutableTaskState userTaskState;

  private final MutableElementInstanceState elementInstanceState;

  public UserTaskCompletedV1Applier(final MutableAsyncProcessingContext processingState) {
    userTaskState = processingState.getTaskState();
    elementInstanceState = processingState.getElementInstanceState();
  }

  @Override
  public void applyState(final long key, final TaskRecord value) {
    userTaskState.delete(key);

    final long elementInstanceKey = value.getElementInstanceKey();
    final ElementInstance elementInstance = elementInstanceState.getInstance(elementInstanceKey);

    if (elementInstance != null) {
      final long scopeKey = elementInstance.getValue().getFlowScopeKey();
      final ElementInstance scopeInstance = elementInstanceState.getInstance(scopeKey);

      if (scopeInstance != null && scopeInstance.isActive()) {

        elementInstance.setUserTaskKey(-1);
        elementInstanceState.updateInstance(elementInstance);
      }
    }
  }
}
