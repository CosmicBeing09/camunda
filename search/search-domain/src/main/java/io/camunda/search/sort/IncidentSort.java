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

public record IncidentSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static IncidentSort of(final Function<Builder, ObjectBuilder<IncidentSort>> fn) {
    return SortOptionBuilders.incident(fn);
  }

  public static final class Builder extends SortOption.AbstractBuilder<IncidentSort.Builder>
      implements ObjectBuilder<IncidentSort> {

    public Builder incidentKey() {
      currentFieldSorting = new FieldSorting("incidentKey", null);
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

    public Builder processInstanceKey() {
      currentFieldSorting = new FieldSorting("processInstanceKey", null);
      return this;
    }

    public Builder errorType() {
      currentFieldSorting = new FieldSorting("errorType", null);
      return this;
    }

    public Builder errorMessage() {
      currentFieldSorting = new FieldSorting("errorMessage", null);
      return this;
    }

    public Builder flowNodeId() {
      currentFieldSorting = new FieldSorting("flowNodeId", null);
      return this;
    }

    public Builder flowNodeInstanceKey() {
      currentFieldSorting = new FieldSorting("flowNodeInstanceKey", null);
      return this;
    }

    public Builder creationTime() {
      currentFieldSorting = new FieldSorting("creationTime", null);
      return this;
    }

    public Builder state() {
      currentFieldSorting = new FieldSorting("state", null);
      return this;
    }

    public Builder jobKey() {
      currentFieldSorting = new FieldSorting("jobKey", null);
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
    public IncidentSort build() {
      return new IncidentSort(fieldSortings);
    }
  }
}
