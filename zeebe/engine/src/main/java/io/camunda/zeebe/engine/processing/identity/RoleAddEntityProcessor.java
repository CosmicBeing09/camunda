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

public class RoleAddEntityProcessor implements DistributedTypedRecordProcessor<RoleRecord> {

  public static final String ROLE_NOT_FOUND_ERROR_MESSAGE =
      "Expected to update role with ID '%s', but a role with this ID does not exist.";
  public static final String ENTITY_NOT_FOUND_ERROR_MESSAGE =
      "Expected to add an entity with ID '%s' and type '%s' to role with ID '%s', but the entity doesn't exist.";
  private static final String ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE =
      "Expected to add entity with ID '%s' to role with ID '%s', but the entity is already assigned to this role.";
  private final RoleState roleState;
  private final MappingState mappingState;
  private final MembershipState membershipState;
  private final GroupState groupState;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final KeyGenerator keyGenerator;
  private final StateWriter stateWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final CommandDistributionBehavior commandDistributionBehavior;

  public RoleAddEntityProcessor(
      final ProcessingState processingState,
      final AuthorizationCheckBehavior authCheckBehavior,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior) {
    roleState = processingState.getRoleState();
    mappingState = processingState.getMappingState();
    membershipState = processingState.getMembershipState();
    groupState = processingState.getGroupState();
    this.authCheckBehavior = authCheckBehavior;
    this.keyGenerator = keyGenerator;
    stateWriter = writers.state();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    this.commandDistributionBehavior = commandDistributionBehavior;
  }

  @Override
  public void processNewCommand(final TypedRecord<RoleRecord> groupRemovalCommand) {
    final var roleRecord = groupRemovalCommand.getValue();
    final var authorizationRequest =
        new AuthorizationRequest(groupRemovalCommand, AuthorizationResourceType.ROLE, PermissionType.UPDATE)
            .addResourceId(roleRecord.getRoleId());

    final var authorizationResult = authCheckBehavior.authorizationResult(authorizationRequest);
    if (authorizationResult.isLeft()) {
      final var rejection = authorizationResult.getLeft();
      rejectionWriter.appendRejection(groupRemovalCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(groupRemovalCommand, rejection.type(), rejection.reason());
      return;
    }

    final var existingRole = roleState.getRole(roleRecord.getRoleId());
    if (existingRole.isEmpty()) {
      final var errorMessage = ROLE_NOT_FOUND_ERROR_MESSAGE.formatted(roleRecord.getRoleId());
      rejectionWriter.appendRejection(groupRemovalCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(groupRemovalCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    final var entityId = roleRecord.getEntityId();
    final var entityType = roleRecord.getEntityType();
    if (!isEntityPresent(entityId, entityType, isInternalGroupsEnabled(groupRemovalCommand))) {
      final var errorMessage =
          ENTITY_NOT_FOUND_ERROR_MESSAGE.formatted(entityId, entityType, roleRecord.getRoleId());
      rejectionWriter.appendRejection(groupRemovalCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(groupRemovalCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    if (isEntityAlreadyAssigned(roleRecord)) {
      final var errorMessage =
          ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE.formatted(roleRecord.getEntityId(), roleRecord.getRoleId());
      rejectionWriter.appendRejection(groupRemovalCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      responseWriter.writeRejectionOnCommand(groupRemovalCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      return;
    }

    stateWriter.appendFollowUpEvent(roleRecord.getRoleKey(), RoleIntent.ENTITY_ADDED, roleRecord);
    responseWriter.writeEventOnCommand(
        roleRecord.getRoleKey(), RoleIntent.ENTITY_ADDED, roleRecord, groupRemovalCommand);

    final long distributionKey = keyGenerator.nextKey();
    commandDistributionBehavior
        .withKey(distributionKey)
        .inQueue(DistributionQueue.IDENTITY.getQueueId())
        .distribute(groupRemovalCommand);
  }

  @Override
  public void processDistributedCommand(final TypedRecord<RoleRecord> command) {
    final var roleRecord = command.getValue();
    if (isEntityAlreadyAssigned(roleRecord)) {
      final var errorMessage =
          ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE.formatted(roleRecord.getEntityId(), roleRecord.getRoleId());
      rejectionWriter.appendRejection(command, RejectionType.ALREADY_EXISTS, errorMessage);
    } else {
      stateWriter.appendFollowUpEvent(command.getKey(), RoleIntent.ENTITY_ADDED, roleRecord);
    }

    commandDistributionBehavior.acknowledgeCommand(command);
  }

  private boolean isEntityPresent(
      final String entityId, final EntityType entityType, final boolean internalGroupsEnabled) {
    return switch (entityType) {
      case USER, CLIENT -> true; // With simple mappings, any username and client id can be assigned
      case MAPPING -> mappingState.get(entityId).isPresent();
      case GROUP -> !internalGroupsEnabled || groupState.get(entityId).isPresent();
      default -> false;
    };
  }

  private boolean isInternalGroupsEnabled(final TypedRecord<RoleRecord> command) {
    return Boolean.getBoolean(
        (String) command.getAuthorizations().get(Authorization.INTERNAL_GROUPS_ENABLED));
  }

  private boolean isEntityAlreadyAssigned(final RoleRecord roleRecord) {
    return membershipState.hasRelation(
        roleRecord.getEntityType(), roleRecord.getEntityId(), RelationType.ROLE, roleRecord.getRoleId());
  }
}
