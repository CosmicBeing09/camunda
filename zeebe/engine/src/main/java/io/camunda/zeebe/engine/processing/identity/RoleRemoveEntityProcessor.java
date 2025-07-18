/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.identity;

import io.camunda.zeebe.auth.Authorization;
import io.camunda.zeebe.engine.processing.distribution.CommandDistributionBehavior;
import io.camunda.zeebe.engine.processing.identity.AuthorizationCheckBehavior.AuthorizationRequest;
import io.camunda.zeebe.engine.processing.streamprocessor.DistributedTypedRecordProcessor;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.StateWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedRejectionWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.TypedResponseWriter;
import io.camunda.zeebe.engine.processing.streamprocessor.writers.Writers;
import io.camunda.zeebe.engine.state.authorization.DbMembershipState.RelationType;
import io.camunda.zeebe.engine.state.distribution.DistributionQueue;
import io.camunda.zeebe.engine.state.immutable.GroupState;
import io.camunda.zeebe.engine.state.immutable.MappingState;
import io.camunda.zeebe.engine.state.immutable.MembershipState;
import io.camunda.zeebe.engine.state.immutable.ProcessingState;
import io.camunda.zeebe.engine.state.immutable.RoleState;
import io.camunda.zeebe.protocol.impl.record.value.authorization.RoleRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.RoleIntent;
import io.camunda.zeebe.protocol.record.value.AuthorizationResourceType;
import io.camunda.zeebe.protocol.record.value.EntityType;
import io.camunda.zeebe.protocol.record.value.PermissionType;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;

public class RoleRemoveEntityProcessor implements DistributedTypedRecordProcessor<RoleRecord> {

  public static final String ROLE_NOT_FOUND_ERROR_MESSAGE =
      "Expected to update role with ID '%s', but a role with this ID does not exist.";
  public static final String ENTITY_NOT_FOUND_ERROR_MESSAGE =
      "Expected to remove an entity with ID '%s' and type '%s' from role with ID '%s', but the entity doesn't exist.";
  private static final String ENTITY_NOT_ASSIGNED_ERROR_MESSAGE =
      "Expected to remove entity with ID '%s' from role with ID '%s', but the entity is not assigned to this role.";
  private final RoleState roleState;
  private final MappingState mappingState;
  private final GroupState groupState;
  private final MembershipState membershipState;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final KeyGenerator keyGenerator;
  private final StateWriter stateWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final CommandDistributionBehavior commandDistributionBehavior;

  public RoleRemoveEntityProcessor(
      final ProcessingState processingState,
      final AuthorizationCheckBehavior authCheckBehavior,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior) {
    roleState = processingState.getRoleState();
    mappingState = processingState.getMappingState();
    groupState = processingState.getGroupState();
    membershipState = processingState.getMembershipState();
    this.authCheckBehavior = authCheckBehavior;
    this.keyGenerator = keyGenerator;
    stateWriter = writers.state();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    this.commandDistributionBehavior = commandDistributionBehavior;
  }

  @Override
  public void processNewCommand(final TypedRecord<RoleRecord> tenantCreateCommand) {
    final var record = tenantCreateCommand.getValue();
    final var authorizationRequest =
        new AuthorizationRequest(tenantCreateCommand, AuthorizationResourceType.ROLE, PermissionType.UPDATE)
            .addResourceId(record.getRoleId());
    final var isAuthorized = authCheckBehavior.authorizationResult(authorizationRequest);
    if (isAuthorized.isLeft()) {
      final var rejection = isAuthorized.getLeft();
      rejectionWriter.appendRejection(tenantCreateCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(tenantCreateCommand, rejection.type(), rejection.reason());
      return;
    }

    final var persistedRecord = roleState.getRole(record.getRoleId());
    if (persistedRecord.isEmpty()) {
      final var errorMessage = ROLE_NOT_FOUND_ERROR_MESSAGE.formatted(record.getRoleId());
      rejectionWriter.appendRejection(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    final var entityId = record.getEntityId();
    final var entityType = record.getEntityType();
    if (!isEntityPresent(entityId, entityType, isInternalGroupsEnabled(tenantCreateCommand))) {
      final var errorMessage =
          ENTITY_NOT_FOUND_ERROR_MESSAGE.formatted(entityId, entityType, record.getRoleId());
      rejectionWriter.appendRejection(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    if (!isEntityAssigned(record)) {
      final var errorMessage =
          ENTITY_NOT_ASSIGNED_ERROR_MESSAGE.formatted(record.getEntityId(), record.getRoleId());
      rejectionWriter.appendRejection(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(tenantCreateCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    stateWriter.appendFollowUpEvent(record.getRoleKey(), RoleIntent.ENTITY_REMOVED, record);
    responseWriter.writeEventOnCommand(
        record.getRoleKey(), RoleIntent.ENTITY_REMOVED, record, tenantCreateCommand);

    final long distributionKey = keyGenerator.nextKey();
    commandDistributionBehavior
        .withKey(distributionKey)
        .inQueue(DistributionQueue.IDENTITY.getQueueId())
        .distribute(tenantCreateCommand);
  }

  @Override
  public void processDistributedCommand(final TypedRecord<RoleRecord> distributedCreateCommand) {
    final var record = distributedCreateCommand.getValue();

    if (isEntityAssigned(record)) {
      stateWriter.appendFollowUpEvent(
          distributedCreateCommand.getKey(), RoleIntent.ENTITY_REMOVED, distributedCreateCommand.getValue());
    } else {
      final var errorMessage =
          ENTITY_NOT_ASSIGNED_ERROR_MESSAGE.formatted(record.getEntityId(), record.getRoleId());
      rejectionWriter.appendRejection(distributedCreateCommand, RejectionType.NOT_FOUND, errorMessage);
    }
    commandDistributionBehavior.acknowledgeCommand(distributedCreateCommand);
  }

  private boolean isEntityAssigned(final RoleRecord record) {
    return membershipState.hasRelation(
        record.getEntityType(), record.getEntityId(), RelationType.ROLE, record.getRoleId());
  }

  private boolean isInternalGroupsEnabled(final TypedRecord<RoleRecord> command) {
    return Boolean.getBoolean(
        (String) command.getAuthorizations().get(Authorization.INTERNAL_GROUPS_ENABLED));
  }

  private boolean isEntityPresent(
      final String entityId, final EntityType entityType, final boolean internalGroupsEnabled) {
    return switch (entityType) {
      case USER, CLIENT -> true; // With simple mappings, any username or client id can be assigned
      case MAPPING -> mappingState.get(entityId).isPresent();
      case GROUP -> !internalGroupsEnabled || groupState.get(entityId).isPresent();
      default -> false;
    };
  }
}
