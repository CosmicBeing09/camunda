/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.usertask;

import io.camunda.zeebe.engine.processing.bpmn.behavior.BpmnBehaviors;
import io.camunda.zeebe.engine.processing.common.EventHandle;
import io.camunda.zeebe.engine.processing.identity.AccessControlBehavior;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskCommandCompleteProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskAssignProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskCancelProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskClaimProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskCommandProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskCreateProcessor;
import io.camunda.zeebe.engine.processing.usertask.processors.UserTaskUpdateProcessor;
import io.camunda.zeebe.engine.state.immutable.ProcessingState;
import io.camunda.zeebe.protocol.record.intent.TaskIntent;
import io.camunda.zeebe.stream.api.state.RecordKeyProvider;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TaskCommandProcessors {

  private final Map<TaskIntent, UserTaskCommandProcessor> commandToProcessor;

  public TaskCommandProcessors(
      final ProcessingState processingState,
      final RecordKeyProvider keyGenerator,
      final BpmnBehaviors bpmnBehaviors,
      final Writers writers,
      final AccessControlBehavior authCheckBehavior) {
    final EventHandle eventHandle =
        new EventHandle(
            keyGenerator,
            processingState.getEventScopeInstanceState(),
            writers,
            processingState.getProcessState(),
            bpmnBehaviors.eventTriggerBehavior(),
            bpmnBehaviors.stateBehavior());

    commandToProcessor =
        new EnumMap<>(
            Map.of(
                TaskIntent.CREATE,
                new UserTaskCreateProcessor(
                    processingState,
                    writers,
                    authCheckBehavior,
                    bpmnBehaviors.userTaskBehavior(),
                    bpmnBehaviors.jobBehavior()),
                TaskIntent.ASSIGN,
                new UserTaskAssignProcessor(processingState, writers, authCheckBehavior),
                TaskIntent.CLAIM,
                new UserTaskClaimProcessor(processingState, writers, authCheckBehavior),
                TaskIntent.UPDATE,
                new UserTaskUpdateProcessor(
                    processingState, writers, bpmnBehaviors.variableBehavior(), authCheckBehavior),
                TaskIntent.COMPLETE,
                new UserTaskCommandCompleteProcessor(
                    processingState, eventHandle, writers, authCheckBehavior),
                TaskIntent.CANCEL,
                new UserTaskCancelProcessor(processingState, writers)));
    validateProcessorsSetup(commandToProcessor);
  }

  public UserTaskCommandProcessor getCommandProcessor(final TaskIntent userTaskIntent) {
    if (userTaskIntent.isEvent()) {
      throw new IllegalArgumentException(
          "Expected a command, but received an event: '%s'. Valid UserTask commands are: %s"
              .formatted(userTaskIntent, TaskIntent.commands()));
    }

    return Optional.ofNullable(commandToProcessor.get(userTaskIntent))
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    "No processor found for the '%s' UserTask command".formatted(userTaskIntent)));
  }

  private static void validateProcessorsSetup(
      final Map<TaskIntent, UserTaskCommandProcessor> commandToProcessor) {
    final var missingProcessors =
        TaskIntent.commands().stream()
            // Exclude COMPLETE_TASK_LISTENER and REJECT_TASK_LISTENER as they don't require a
            // dedicated processor.
            // This intent is handled internally within UserTaskProcessor
            .filter(intent -> intent != TaskIntent.COMPLETE_TASK_LISTENER)
            .filter(intent -> intent != TaskIntent.DENY_TASK_LISTENER)
            .filter(intent -> !commandToProcessor.containsKey(intent))
            .collect(Collectors.toSet());

    if (!missingProcessors.isEmpty()) {
      throw new IllegalStateException(
          "No processors defined for the following UserTask commands: %s"
              .formatted(missingProcessors));
    }
  }
}
