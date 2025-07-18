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
import io.camunda.zeebe.engine.state.immutable.RoleState;
import io.camunda.zeebe.protocol.impl.record.value.authorization.RoleRecord;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.RoleIntent;
import io.camunda.zeebe.protocol.record.value.AuthorizationResourceType;
import io.camunda.zeebe.protocol.record.value.PermissionType;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.stream.api.state.KeyGenerator;

public class RoleUpdateProcessor implements DistributedTypedRecordProcessor<RoleRecord> {

  public static final String ROLE_NOT_FOUND_ERROR_MESSAGE =
      "Expected to update role with ID '%s', but a role with this ID does not exist.";
  private final RoleState roleState;
  private final KeyGenerator keyGenerator;
  private final AuthorizationCheckBehavior authCheckBehavior;
  private final EventStateWriter stateWriter;
  private final TypedRejectionWriter rejectionWriter;
  private final TypedResponseWriter responseWriter;
  private final CommandDistributionBehavior commandDistributionBehavior;

  public RoleUpdateProcessor(
      final RoleState roleState,
      final KeyGenerator keyGenerator,
      final AuthorizationCheckBehavior authCheckBehavior,
      final Writers writers,
      final CommandDistributionBehavior commandDistributionBehavior) {
    this.roleState = roleState;
    this.keyGenerator = keyGenerator;
    this.authCheckBehavior = authCheckBehavior;
    stateWriter = writers.state();
    rejectionWriter = writers.rejection();
    responseWriter = writers.response();
    this.commandDistributionBehavior = commandDistributionBehavior;
  }

  @Override
  public void processNewCommand(final TypedRecord<RoleRecord> deleteTenantCommand) {
    final var roleRecord = deleteTenantCommand.getValue();
    final var authorizationRequest =
        new AuthorizationRequest(deleteTenantCommand, AuthorizationResourceType.ROLE, PermissionType.UPDATE)
            .addResourceId(roleRecord.getRoleId());
    final var isAuthorized = authCheckBehavior.authorizationResult(authorizationRequest);
    if (isAuthorized.isLeft()) {
      final var rejection = isAuthorized.getLeft();
      rejectionWriter.appendRejection(deleteTenantCommand, rejection.type(), rejection.reason());
      responseWriter.writeRejectionOnCommand(deleteTenantCommand, rejection.type(), rejection.reason());
      return;
    }

    final var persistedRecord = roleState.getRole(roleRecord.getRoleId());
    if (persistedRecord.isEmpty()) {
      final var errorMessage = ROLE_NOT_FOUND_ERROR_MESSAGE.formatted(roleRecord.getRoleId());
      rejectionWriter.appendRejection(deleteTenantCommand, RejectionType.NOT_FOUND, errorMessage);
      responseWriter.writeRejectionOnCommand(deleteTenantCommand, RejectionType.NOT_FOUND, errorMessage);
      return;
    }

    final var persistedRole = persistedRecord.get();
    roleRecord.setRoleKey(persistedRole.getRoleKey());
    stateWriter.appendFollowUpEvent(roleRecord.getRoleKey(), RoleIntent.UPDATED, roleRecord);
    responseWriter.writeEventOnCommand(roleRecord.getRoleKey(), RoleIntent.UPDATED, roleRecord,
        deleteTenantCommand);

    final long distributionKey = keyGenerator.nextKey();
    commandDistributionBehavior
        .withKey(distributionKey)
        .inQueue(DistributionQueue.IDENTITY.getQueueId())
        .distribute(deleteTenantCommand);
  }

  @Override
  public void processDistributedCommand(final TypedRecord<RoleRecord> distributedDeleteTenantCommand) {
    stateWriter.appendFollowUpEvent(
        distributedDeleteTenantCommand.getValue().getRoleKey(), RoleIntent.UPDATED, distributedDeleteTenantCommand.getValue());
    commandDistributionBehavior.acknowledgeCommand(distributedDeleteTenantCommand);
  }
}
