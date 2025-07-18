/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.identity;

import io.camunda.zeebe.engine.processing.distribution.CommandDistributionBehavior;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior.AuthorizationRequest;
import io.camunda.zeebe.engine.processing.streamprocessor.DistributedTypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.EventStateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.distribution.DistributionQueue;
import io.camunda.zeebe.engine.state.immutable.MappingState;
import io.camunda.zeebe.protocol.impl.record.value.authorization.MappingRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.MappingIntent;
import io.camunda.zeebe.protocol.record.value.AuthorizationResourceType;
import io.camunda.zeebe.protocol.record.value.PermissionType;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;

public class MappingUpdateProcessor implements DistributedTypedRecordProcessor<MappingRecord> {
  private static final String MAPPING_NULL_VALUE_ERROR_MESSAGE =
      "Expected to update mappingRule with claimName '%s' and claimValue '%s' and name '%s' and mappingRuleId '%s', but at least one of them is null.";
  private static final String MAPPING_SAME_CLAIM_ALREADY_EXISTS_ERROR_MESSAGE =
      "Expected to update mapping with claimName '%s' and claimValue '%s', but a mapping with this claim already exists.";
  private static final String MAPPING_ID_DOES_NOT_EXIST_ERROR_MESSAGE =
      "Expected to update mapping with id '%s', but a mapping with this id does not exist.";

  private final MappingState mappingState;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final KeyGenerator keyGenerator;
  private final EventStateWriter stateWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final CommandDistributionBehavior commandDistributionBehavior;

  public MappingUpdateProcessor(
      final MappingState mappingState,
      final AuthorizationCheckBehavior authCheckBehavior,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior) {
    this.mappingState = mappingState;
    this.authCheckBehavior = authCheckBehavior;
    this.keyGenerator = keyGenerator;
    stateWriter = writers.state();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    this.commandDistributionBehavior = commandDistributionBehavior;
  }

  @Override
  public void processNewCommand(final TypedRecord<MappingRecord> updateUserCommand) {

    final var record = updateUserCommand.getValue();
    final var mappingId = record.getMappingId();
    if (record.getMappingId() == null
        || record.getMappingId().isBlank()
        || record.getName() == null
        || record.getName().isBlank()
        || record.getClaimName() == null
        || record.getClaimName().isBlank()
        || record.getClaimValue() == null
        || record.getClaimValue().isBlank()) {
      final var errorMessage =
          MAPPING_NULL_VALUE_ERROR_MESSAGE.formatted(
              record.getClaimName(),
              record.getClaimValue(),
              record.getName(),
              record.getMappingId());
      rejectionWriter.appendRejection(updateUserCommand, RejectionType.NULL_VAL, errorMessage);
      responseWriter.writeRejectionOnCommand(updateUserCommand, RejectionType.NULL_VAL, errorMessage);
      return;
    }

    final var persistedRecord = mappingState.getMappingById(mappingId);
    if (persistedRecord.isEmpty()) {
      final var errorMessage = MAPPING_ID_DOES_NOT_EXIST_ERROR_MESSAGE.formatted(mappingId);
      rejectionWriter.appendRejection(updateUserCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(updateUserCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    final var authorizationRequest =
        new AuthorizationRequest(
            updateUserCommand, AuthorizationResourceType.MAPPING_RULE, PermissionType.UPDATE)
            .addResourceId(mappingId);
    final var isAuthorized = authCheckBehavior.authorizationResult(authorizationRequest);
    if (isAuthorized.isLeft()) {
      final var rejection = isAuthorized.getLeft();
      rejectionWriter.appendRejection(updateUserCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(updateUserCommand, rejection.type(), rejection.reason());
      return;
    }

    final var persistedMappingWithSameClaim =
        mappingState.getMappingByClaim(record.getClaimName(), record.getClaimValue());
    if (persistedMappingWithSameClaim.isPresent()
        && !persistedMappingWithSameClaim.get().getMappingId().equals(mappingId)) {
      final var errorMessage =
          MAPPING_SAME_CLAIM_ALREADY_EXISTS_ERROR_MESSAGE.formatted(
              record.getClaimName(), record.getClaimValue());
      rejectionWriter.appendRejection(updateUserCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      responseWriter.writeRejectionOnCommand(updateUserCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      return;
    }

    stateWriter.appendFollowUpEvent(record.getMappingKey(), MappingIntent.UPDATED, record);
    responseWriter.writeEventOnCommand(
        record.getMappingKey(), MappingIntent.UPDATED, record, updateUserCommand);

    commandDistributionBehavior
        .withKey(keyGenerator.nextKey())
        .inQueue(DistributionQueue.IDENTITY.getQueueId())
        .distribute(updateUserCommand);
  }

  @Override
  public void processDistributedCommand(final TypedRecord<MappingRecord> distributedDeleteTenantCommand) {
    stateWriter.appendFollowUpEvent(distributedDeleteTenantCommand.getKey(), MappingIntent.UPDATED, distributedDeleteTenantCommand.getValue());
    commandDistributionBehavior.acknowledgeCommand(distributedDeleteTenantCommand);
  }
}
