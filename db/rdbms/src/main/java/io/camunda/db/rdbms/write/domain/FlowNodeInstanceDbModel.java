/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.db.rdbms.write.domain;

import io.camunda.search.entities.FlowNodeInstanceEntity.FlowNodeState;
import io.camunda.search.entities.FlowNodeInstanceEntity.FlowNodeType;
import io.camunda.util.ObjectBuilder;
import java.time.OffsetDateTime;
import java.util.function.Function;

public record FlowNodeInstanceDbModel(
    Long key,
    Long processInstanceKey,
    Long processDefinitionKey,
    String processDefinitionId,
    OffsetDateTime startDate,
    OffsetDateTime endDate,
    String flowNodeId,
    String flowNodeName,
    String treePath,
    FlowNodeType type,
    FlowNodeState state,
    Long incidentKey,
    Long numSubprocessIncidents,
    String tenantId,
    int partitionId,
    OffsetDateTime historyCleanupDate)
    implements Copyable<FlowNodeInstanceDbModel> {

  @Override
  public FlowNodeInstanceDbModel copy(
      final Function<ObjectBuilder<FlowNodeInstanceDbModel>, ObjectBuilder<FlowNodeInstanceDbModel>>
          builderFunction) {
    return builderFunction
        .apply(
            new Builder()
                .flowNodeInstanceKey(key)
                .processInstanceKey(processInstanceKey())
                .processDefinitionKey(processDefinitionKey)
                .processDefinitionId(processDefinitionId)
                .startDate(startDate)
                .endDate(endDate)
                .flowNodeId(flowNodeId)
                .flowNodeName(flowNodeName)
                .treePath(treePath)
                .type(type)
                .state(state)
                .incidentKey(incidentKey)
                .numSubprocessIncidents(numSubprocessIncidents)
                .tenantId(tenantId)
                .partitionId(partitionId)
                .historyCleanupDate(historyCleanupDate))
        .build();
  }

  public static class Builder
      implements ObjectBuilder<FlowNodeInstanceDbModel> {

    private Long flowNodeInstanceKey;
    private Long processInstanceKey;
    private Long processDefinitionKey;
    private String processDefinitionId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String flowNodeId;
    private String flowNodeName;
    private String treePath;
    private FlowNodeType type;
    private FlowNodeState state;
    private Long incidentKey;
    private Long numSubprocessIncidents = 0L;
    private String tenantId;
    private int partitionId;
    private OffsetDateTime historyCleanupDate;

    // Public constructor to initialize the builder
    public Builder() {}

    // Builder methods for each field
    public Builder flowNodeInstanceKey(final Long key) {
      flowNodeInstanceKey = key;
      return this;
    }

    public Builder processInstanceKey(final Long processInstanceKey) {
      this.processInstanceKey = processInstanceKey;
      return this;
    }

    public Builder processDefinitionKey(final Long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
      return this;
    }

    public Builder startDate(final OffsetDateTime startDate) {
      this.startDate = startDate;
      return this;
    }

    public Builder endDate(final OffsetDateTime endDate) {
      this.endDate = endDate;
      return this;
    }

    public Builder flowNodeId(final String flowNodeId) {
      this.flowNodeId = flowNodeId;
      return this;
    }

    public Builder flowNodeName(final String flowNodeName) {
      this.flowNodeName = flowNodeName;
      return this;
    }

    public Builder treePath(final String treePath) {
      this.treePath = treePath;
      return this;
    }

    public Builder type(final FlowNodeType type) {
      this.type = type;
      return this;
    }

    public Builder state(final FlowNodeState state) {
      this.state = state;
      return this;
    }

    public Builder incidentKey(final Long incidentKey) {
      this.incidentKey = incidentKey;
      return this;
    }

    public Long numSubprocessIncidents() {
      return numSubprocessIncidents;
    }

    public Builder numSubprocessIncidents(
        final Long numSubprocessIncidents) {
      this.numSubprocessIncidents = numSubprocessIncidents;
      return this;
    }

    public Builder processDefinitionId(final String bpmnProcessId) {
      processDefinitionId = bpmnProcessId;
      return this;
    }

    public Builder tenantId(final String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder partitionId(final int partitionId) {
      this.partitionId = partitionId;
      return this;
    }

    public Builder historyCleanupDate(final OffsetDateTime value) {
      historyCleanupDate = value;
      return this;
    }

    @Override
    public FlowNodeInstanceDbModel build() {
      return new FlowNodeInstanceDbModel(
          flowNodeInstanceKey,
          processInstanceKey,
          processDefinitionKey,
          processDefinitionId,
          startDate,
          endDate,
          flowNodeId,
          flowNodeName,
          treePath,
          type,
          state,
          incidentKey,
          numSubprocessIncidents,
          tenantId,
          partitionId,
          historyCleanupDate);
    }
  }
}
