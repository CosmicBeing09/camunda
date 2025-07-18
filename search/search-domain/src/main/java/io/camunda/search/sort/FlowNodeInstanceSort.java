/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.sort;

import io.camunda.util.ObjectBuilder;
import java.util.List;
import java.util.function.Function;

public record FlowNodeInstanceSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static FlowNodeInstanceSort of(
      final Function<FlowNodeInstanceSort.Builder, ObjectBuilder<FlowNodeInstanceSort>> fn) {
    return SortOptionBuilders.flowNodeInstance(fn);
  }

  public static final class Builder extends AbstractBuilder<Builder>
      implements ObjectBuilder<FlowNodeInstanceSort> {

    public Builder flowNodeInstanceKey() {
      currentFieldSorting = new FieldSorting("flowNodeInstanceKey", null);
      return this;
    }

    public Builder processInstanceKey() {
      currentFieldSorting = new FieldSorting("processInstanceKey", null);
      return this;
    }

    public Builder processDefinitionKey() {
      currentFieldSorting = new FieldSorting("processDefinitionKey", null);
      return this;
    }

    public Builder processDefinitionId() {
      currentFieldSorting = new FieldSorting("processDefinitionId", null);
      return this;
    }

    public Builder startDate() {
      currentFieldSorting = new FieldSorting("startDate", null);
      return this;
    }

    public Builder endDate() {
      currentFieldSorting = new FieldSorting("endDate", null);
      return this;
    }

    public Builder flowNodeId() {
      currentFieldSorting = new FieldSorting("flowNodeId", null);
      return this;
    }

    public Builder flowNodeName() {
      currentFieldSorting = new FieldSorting("flowNodeName", null);
      return this;
    }

    public Builder type() {
      currentFieldSorting = new FieldSorting("type", null);
      return this;
    }

    public Builder state() {
      currentFieldSorting = new FieldSorting("state", null);
      return this;
    }

    public Builder incidentKey() {
      currentFieldSorting = new FieldSorting("incidentKey", null);
      return this;
    }

    public Builder tenantId() {
      currentFieldSorting = new FieldSorting("tenantId", null);
      return this;
    }

    @Override
    public FlowNodeInstanceSort build() {
      return new FlowNodeInstanceSort(fieldSortings);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
