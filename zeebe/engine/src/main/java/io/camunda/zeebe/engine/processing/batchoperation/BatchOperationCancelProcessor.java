/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.batchoperation;

import io.camunda.zeebe.engine.metrics.BatchOperationMetrics;
import io.camunda.zeebe.engine.processing.ExcludeAuthorizationCheck;
import io.camunda.zeebe.engine.processing.Rejection;
import io.camunda.zeebe.engine.processing.distribution.CommandDistributionBehavior;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior.AuthorizationRequest;
import io.camunda.zeebe.engine.processing.streamprocessor.DistributedTypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.FollowUpEventMetadata;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.EventStateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.distribution.DistributionQueue;
import io.camunda.zeebe.engine.state.immutable.BatchOperationState;
import io.camunda.zeebe.engine.state.immutable.ProcessingState;
import io.camunda.zeebe.protocol.impl.record.value.batchoperation.BatchOperationLifecycleManagementRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.BatchOperationIntent;
import io.camunda.zeebe.protocol.record.value.AuthorizationResourceType;
import io.camunda.zeebe.protocol.record.value.PermissionType;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExcludeAuthorizationCheck
public final class BatchOperationCancelProcessor
    implements DistributedTypedRecordProcessor<BatchOperationLifecycleManagementRecord> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchOperationCancelProcessor.class);

  private static final String MESSAGE_PREFIX =
      "Expected to cancel a batch operation with key '%d', but ";
  private static final String BATCH_OPERATION_NOT_FOUND_MESSAGE =
      MESSAGE_PREFIX + "no such batch operation was found";

  private final CommandDistributionBehavior commandDistributionBehavior;
  private final EventStateWriter stateWriter;
  private final TypedResponseWriter responseWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final BatchOperationState batchOperationState;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final KeyGenerator keyGenerator;
  private final BatchOperationMetrics metrics;

  public BatchOperationCancelProcessor(
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior,
      final ProcessingState processingState,
      final AuthorizationCheckBehavior authCheckBehavior,
      final KeyGenerator keyGenerator,
      final BatchOperationMetrics metrics) {
    stateWriter = writers.state();
    responseWriter = writers.response();
    rejectionWriter = writers.rejection();
    this.commandDistributionBehavior = commandDistributionBehavior;
    batchOperationState = processingState.getBatchOperationState();
    this.authCheckBehavior = authCheckBehavior;
    this.keyGenerator = keyGenerator;
    this.metrics = metrics;
  }

  @Override
  public void processNewCommand(
      final TypedRecord<BatchOperationLifecycleManagementRecord> updateUserCommand) {
    final var request =
        new AuthorizationRequest(
            updateUserCommand, AuthorizationResourceType.BATCH_OPERATION, PermissionType.UPDATE);
    final var authorizationResult = authCheckBehavior.authorizationResult(request);
    if (authorizationResult.isLeft()) {
      final Rejection rejection = authorizationResult.getLeft();
      rejectionWriter.appendRejection(updateUserCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(updateUserCommand, rejection.type(), rejection.reason());
      return;
    }

    final var lifecycleManagementRecord = updateUserCommand.getValue();
    final var batchOperationKey = lifecycleManagementRecord.getBatchOperationKey();
    final var cancelKey = keyGenerator.nextKey();
    LOGGER.debug(
        "Processing new command to cancel a batch operation with key '{}': {}",
        batchOperationKey,
        lifecycleManagementRecord);

    final var batchOperation = batchOperationState.get(batchOperationKey);
    if (batchOperation.isPresent() && batchOperation.get().canCancel()) {
      cancelBatchOperationEvent(cancelKey, lifecycleManagementRecord);
      responseWriter.writeEventOnCommand(
          cancelKey, BatchOperationIntent.CANCELED, updateUserCommand.getValue(),
          updateUserCommand);
      commandDistributionBehavior
          .withKey(cancelKey)
          .inQueue(DistributionQueue.BATCH_OPERATION)
          .distribute(updateUserCommand);

      metrics.recordCancelled(batchOperation.get().getBatchOperationType());
    } else {
      rejectionWriter.appendRejection(
          updateUserCommand,
          RejectionType.NOT_FOUND,
          String.format(BATCH_OPERATION_NOT_FOUND_MESSAGE, batchOperationKey));
      responseWriter.writeRejectionOnCommand(
          updateUserCommand,
          RejectionType.NOT_FOUND,
          String.format(BATCH_OPERATION_NOT_FOUND_MESSAGE, batchOperationKey));
    }
  }

  @Override
  public void processDistributedCommand(
      final TypedRecord<BatchOperationLifecycleManagementRecord> distributedDeleteTenantCommand) {
    final var recordValue = distributedDeleteTenantCommand.getValue();
    final var batchOperationKey = recordValue.getBatchOperationKey();

    final var batchOperation = batchOperationState.get(batchOperationKey);
    if (batchOperation.isEmpty()) {
      rejectionWriter.appendRejection(
          distributedDeleteTenantCommand, RejectionType.NOT_FOUND, "Batch operation does not exist!");
      commandDistributionBehavior.acknowledgeCommand(distributedDeleteTenantCommand);
      return;
    }

    LOGGER.debug(
        "Processing distributed command to cancel a batch operation with key '{}': {}",
        batchOperationKey,
        recordValue);
    cancelBatchOperationEvent(batchOperationKey, recordValue);
    commandDistributionBehavior.acknowledgeCommand(distributedDeleteTenantCommand);
  }

  private void cancelBatchOperationEvent(
      final Long cancelKey, final BatchOperationLifecycleManagementRecord recordValue) {
    stateWriter.appendFollowUpEvent(
        cancelKey,
        BatchOperationIntent.CANCELED,
        recordValue,
        FollowUpEventMetadata.of(
            b -> b.batchOperationReference(recordValue.getBatchOperationKey())));
  }
}
