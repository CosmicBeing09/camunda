/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.job;

import io.camunda.zeebe.engine.processing.job.behaviour.JobUpdateBehaviour;
import io.camunda.zeebe.engine.processing.streamprocessor.TypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.EventStateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.protocol.impl.record.value.job.JobRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.stream.api.records.TypedRecord;

public class JobUpdateTimeoutProcessor implements TypedRecordProcessor<JobRecord> {

  private final JobUpdateBehaviour jobUpdateBehaviour;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final EventStateWriter stateWriter;

  public JobUpdateTimeoutProcessor(
      final JobUpdateBehaviour jobUpdateBehaviour, final Writers writers) {
    this.jobUpdateBehaviour = jobUpdateBehaviour;
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    stateWriter = writers.state();
  }

  @Override
  public void processRecord(final TypedRecord<JobRecord> commandRecord) {
    final long jobKey = commandRecord.getKey();
    jobUpdateBehaviour
        .fetchJobOrReject(jobKey, commandRecord)
        .flatMap(job -> jobUpdateBehaviour.authorizeJobUpdate(commandRecord, job))
        .ifRightOrLeft(
            job ->
                jobUpdateBehaviour
                    .updateJobTimeout(jobKey, commandRecord.getValue().getTimeout(), job)
                    .ifPresentOrElse(
                        errorMessage -> {
                          rejectionWriter.appendRejection(
                              commandRecord, RejectionType.INVALID_STATE, errorMessage);
                          responseWriter.writeRejectionOnCommand(
                              commandRecord, RejectionType.INVALID_STATE, errorMessage);
                        },
                        () -> {
                          stateWriter.appendFollowUpEvent(jobKey, JobIntent.TIMEOUT_UPDATED, job);
                          responseWriter.writeEventOnCommand(
                              jobKey, JobIntent.TIMEOUT_UPDATED, job, commandRecord);
                        }),
            rejection -> {
              rejectionWriter.appendRejection(commandRecord, rejection.type(), rejection.reason());
              responseWriter.writeRejectionOnCommand(commandRecord, rejection.type(), rejection.reason());
            });
  }
}
