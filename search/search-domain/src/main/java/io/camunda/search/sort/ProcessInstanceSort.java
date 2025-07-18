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

public record ProcessInstanceSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static ProcessInstanceSort of(
      final Function<Builder, ObjectBuilder<ProcessInstanceSort>> fn) {
    return SortOptionBuilders.processInstance(fn);
  }

  public static final class Builder extends SortOption.AbstractBuilder<Builder>
      implements ObjectBuilder<ProcessInstanceSort> {

    public Builder processInstanceKey() {
      currentFieldSorting = new FieldSorting("processInstanceKey", null);
      return this;
    }

    public Builder processDefinitionId() {
      currentFieldSorting = new FieldSorting("processDefinitionId", null);
      return this;
    }

    public Builder processDefinitionName() {
      currentFieldSorting = new FieldSorting("processDefinitionName", null);
      return this;
    }

    public Builder processDefinitionVersion() {
      currentFieldSorting = new FieldSorting("processDefinitionVersion", null);
      return this;
    }

    public Builder processDefinitionVersionTag() {
      currentFieldSorting = new FieldSorting("processDefinitionVersionTag", null);
      return this;
    }

    public Builder processDefinitionKey() {
      currentFieldSorting = new FieldSorting("processDefinitionKey", null);
      return this;
    }

    public Builder parentProcessInstanceKey() {
      currentFieldSorting = new FieldSorting("parentProcessInstanceKey", null);
      return this;
    }

    public Builder parentFlowNodeInstanceKey() {
      currentFieldSorting = new FieldSorting("parentFlowNodeInstanceKey", null);
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

    public Builder state() {
      currentFieldSorting = new FieldSorting("state", null);
      return this;
    }

    public Builder hasIncident() {
      currentFieldSorting = new FieldSorting("hasIncident", null);
      return this;
    }

    public Builder tenantId() {
      currentFieldSorting = new FieldSorting("tenantId", null);
      return this;
    }

    @Override
    protected Builder self() {
      return this;
    }

    @Override
    public ProcessInstanceSort build() {
      return new ProcessInstanceSort(fieldSortings);
    }
  }
}
