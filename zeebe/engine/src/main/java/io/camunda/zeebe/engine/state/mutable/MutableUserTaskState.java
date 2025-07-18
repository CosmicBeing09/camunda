/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.mutable;

import io.camunda.zeebe.engine.state.immutable.UserTaskState;
import io.camunda.zeebe.engine.state.instance.UserTaskIntermediateStateValue;
import io.camunda.zeebe.engine.state.instance.UserTaskTransitionTriggerRequest;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskEntity;
import java.util.function.Consumer;

public interface MutableUserTaskState extends UserTaskState {

  void create(final UserTaskEntity userTask);

  void update(final UserTaskEntity userTask);

  void updateUserTaskLifecycleState(final long userTaskKey, final LifecycleState newLifecycleState);

  void delete(final long userTaskKey);

  void storeIntermediateState(final UserTaskEntity userTask, final LifecycleState lifecycleState);

  void updateIntermediateState(long key, Consumer<UserTaskIntermediateStateValue> updater);

  void deleteIntermediateState(final long userTaskKey);

  void deleteIntermediateStateIfExists(final long userTaskKey);

  void storeTransitionTriggerRequest(
      final long userTaskKey, final UserTaskTransitionTriggerRequest transitionTriggerRequest);

  void deleteRecordRequestMetadata(final long userTaskKey);

  void storeInitialAssignee(final long userTaskKey, String assignee);

  void deleteInitialAssignee(final long userTaskKey);
}
