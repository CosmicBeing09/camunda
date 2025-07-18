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
import io.camunda.zeebe.protocol.impl.record.value.group.GroupRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.GroupIntent;
import io.camunda.zeebe.protocol.record.value.AuthorizationResourceType;
import io.camunda.zeebe.protocol.record.value.EntityType;
import io.camunda.zeebe.protocol.record.value.PermissionType;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;

public class GroupAddEntityProcessor implements DistributedTypedRecordProcessor<GroupRecord> {
  private static final String ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE =
      "Expected to add entity with ID '%s' to group with ID '%s', but the entity is already assigned to this group.";

  private final GroupState groupState;
  private final MappingState mappingState;
  private final MembershipState membershipState;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final KeyGenerator keyGenerator;
  private final StateWriter stateWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final CommandDistributionBehavior commandDistributionBehavior;

  public GroupAddEntityProcessor(
      final ProcessingState processingState,
      final AuthorizationCheckBehavior authCheckBehavior,
      final KeyGenerator keyGenerator,
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior) {
    this.commandDistributionBehavior = commandDistributionBehavior;
    this.keyGenerator = keyGenerator;
    this.authCheckBehavior = authCheckBehavior;
    groupState = processingState.getGroupState();
    membershipState = processingState.getMembershipState();
    mappingState = processingState.getMappingState();
    stateWriter = writers.state();
    responseWriter = writers.response();
    rejectionWriter = writers.rejection();
  }

  @Override
  public void processNewCommand(final TypedRecord<GroupRecord> resourceDeletionCommand) {
    final var groupRecord = resourceDeletionCommand.getValue();
    final var authorizationRequest =
        new AuthorizationRequest(resourceDeletionCommand, AuthorizationResourceType.GROUP, PermissionType.UPDATE)
            .addResourceId(groupRecord.getGroupId());
    final var isAuthorized = authCheckBehavior.authorizationResult(authorizationRequest);
    if (isAuthorized.isLeft()) {
      final var rejection = isAuthorized.getLeft();
      rejectionWriter.appendRejection(resourceDeletionCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(resourceDeletionCommand, rejection.type(), rejection.reason());
      return;
    }

    final var groupId = groupRecord.getGroupId();
    final var persistedRecord = groupState.get(groupId);
    if (persistedRecord.isEmpty()) {
      final var errorMessage =
          "Expected to update group with ID '%s', but a group with this ID does not exist."
              .formatted(groupId);
      rejectionWriter.appendRejection(resourceDeletionCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(resourceDeletionCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    final var groupKey = persistedRecord.get().getGroupKey();
    final var entityId = groupRecord.getEntityId();
    final var entityType = groupRecord.getEntityType();
    if (!isEntityPresent(entityId, entityType)) {
      final var errorMessage =
          "Expected to add an entity with ID '%s' and type '%s' to group with ID '%s', but the entity does not exist."
              .formatted(entityId, entityType, groupId);
      rejectionWriter.appendRejection(resourceDeletionCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(resourceDeletionCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    if (isEntityAssigned(groupRecord)) {
      final var errorMessage =
          ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE.formatted(
              groupRecord.getEntityId(), groupRecord.getGroupId());
      rejectionWriter.appendRejection(resourceDeletionCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      responseWriter.writeRejectionOnCommand(resourceDeletionCommand, RejectionType.ALREADY_EXISTS, errorMessage);
      return;
    }

    stateWriter.appendFollowUpEvent(groupKey, GroupIntent.ENTITY_ADDED, groupRecord);
    responseWriter.writeEventOnCommand(groupKey, GroupIntent.ENTITY_ADDED, groupRecord,
        resourceDeletionCommand);

    final long distributionKey = keyGenerator.nextKey();
    commandDistributionBehavior
        .withKey(distributionKey)
        .inQueue(DistributionQueue.IDENTITY.getQueueId())
        .distribute(resourceDeletionCommand);
  }

  @Override
  public void processDistributedCommand(final TypedRecord<GroupRecord> distributedDeleteCommand) {
    final var record = distributedDeleteCommand.getValue();
    if (isEntityAssigned(record)) {
      final var errorMessage =
          ENTITY_ALREADY_ASSIGNED_ERROR_MESSAGE.formatted(
              record.getEntityId(), record.getGroupId());
      rejectionWriter.appendRejection(distributedDeleteCommand, RejectionType.ALREADY_EXISTS, errorMessage);
    } else {
      stateWriter.appendFollowUpEvent(distributedDeleteCommand.getKey(), GroupIntent.ENTITY_ADDED, record);
    }

    commandDistributionBehavior.acknowledgeCommand(distributedDeleteCommand);
  }

  private boolean isEntityPresent(final String entityId, final EntityType entityType) {
    return switch (entityType) {
      case EntityType.USER, CLIENT ->
          true; // With simple mappings, any username or client id can be assigned
      case EntityType.MAPPING -> mappingState.get(entityId).isPresent();
      default -> false;
    };
  }

  private boolean isEntityAssigned(final GroupRecord record) {
    return membershipState.hasRelation(
        record.getEntityType(), record.getEntityId(), RelationType.GROUP, record.getGroupId());
  }
}
