/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.job;

import io.camunda.zeebe.engine.EngineConfiguration;
import io.camunda.zeebe.engine.metrics.ProcessingMetrics;
import io.camunda.zeebe.engine.processing.bpmn.behavior.ProcessBehaviors;
import io.camunda.zeebe.engine.processing.common.EventHandler;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessors;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.immutable.ScheduledTaskState;
import io.camunda.zeebe.engine.state.mutable.MutableAsyncProcessingContext;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.JobBatchIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import java.time.InstantSource;
import java.util.function.Supplier;

public final class JobProcessorsRegistry {

  public static void addJobProcessors(
      final TypedRecordProcessors typedRecordProcessors,
      final MutableAsyncProcessingContext processingState,
      final Supplier<ScheduledTaskState> scheduledTaskStateFactory,
      final ProcessBehaviors bpmnBehaviors,
      final Writers writers,
      final ProcessingMetrics jobMetrics,
      final EngineConfiguration config,
      final InstantSource clock,
      final AuthorizationCheckBehavior authCheckBehavior) {

    final var asyncKeyGenerator = processingState.getKeyGenerator();

    final EventHandler eventHandle =
        new EventHandler(
            asyncKeyGenerator,
            processingState.getEventScopeInstanceState(),
            writers,
            processingState.getProcessState(),
            bpmnBehaviors.eventTriggerBehavior(),
            bpmnBehaviors.stateBehavior());

    final var jobBackoffChecker =
        new JobBackoffChecker(clock, scheduledTaskStateFactory.get().getJobState());
    typedRecordProcessors
        .onCommand(
            ValueType.JOB,
            JobIntent.COMPLETE,
            new JobCompleteProcessor(processingState, jobMetrics, eventHandle, authCheckBehavior))
        .onCommand(
            ValueType.JOB,
            JobIntent.FAIL,
            new JobFailProcessor(
                processingState,
                writers,
                processingState.getKeyGenerator(),
                jobMetrics,
                jobBackoffChecker,
                bpmnBehaviors,
                authCheckBehavior))
        .onCommand(
            ValueType.JOB,
            JobIntent.YIELD,
            new JobYieldProcessor(processingState, bpmnBehaviors, writers, authCheckBehavior))
        .onCommand(
            ValueType.JOB,
            JobIntent.THROW_ERROR,
            new JobThrowErrorProcessor(
                processingState,
                bpmnBehaviors.eventPublicationBehavior(),
                asyncKeyGenerator,
                jobMetrics,
                authCheckBehavior))
        .onCommand(
            ValueType.JOB,
            JobIntent.TIME_OUT,
            new JobTimeOutProcessor(
                processingState, writers, jobMetrics, bpmnBehaviors.jobActivationBehavior(), clock))
        .onCommand(
            ValueType.JOB,
            JobIntent.UPDATE_RETRIES,
            new JobUpdateRetriesProcessor(bpmnBehaviors.jobUpdateBehaviour(), writers))
        .onCommand(
            ValueType.JOB,
            JobIntent.UPDATE_TIMEOUT,
            new JobUpdateTimeoutProcessor(bpmnBehaviors.jobUpdateBehaviour(), writers))
        .onCommand(
            ValueType.JOB,
            JobIntent.UPDATE,
            new JobUpdateProcessor(bpmnBehaviors.jobUpdateBehaviour(), writers))
        .onCommand(
            ValueType.JOB, JobIntent.CANCEL, new JobCancelProcessor(processingState, jobMetrics))
        .onCommand(
            ValueType.JOB,
            JobIntent.RECUR_AFTER_BACKOFF,
            new JobRecurProcessor(
                processingState, writers, bpmnBehaviors.jobActivationBehavior(), clock))
        .onCommand(
            ValueType.JOB_BATCH,
            JobBatchIntent.ACTIVATE,
            new JobBatchActivateProcessor(
                writers,
                processingState,
                processingState.getKeyGenerator(),
                jobMetrics,
                authCheckBehavior))
        .withListener(
            new JobTimeoutCheckerScheduler(
                scheduledTaskStateFactory.get().getJobState(),
                config.getJobsTimeoutCheckerPollingInterval(),
                config.getJobsTimeoutCheckerBatchLimit(),
                clock))
        .withListener(jobBackoffChecker);
  }
}
