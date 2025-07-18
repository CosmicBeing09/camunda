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

public record ProcessDefinitionSort(List<FieldSorting> orderings) implements SortOption {

  @Override
  public List<FieldSorting> getFieldSortings() {
    return orderings;
  }

  public static ProcessDefinitionSort of(
      final Function<ProcessDefinitionSort.Builder, ObjectBuilder<ProcessDefinitionSort>> fn) {
    return SortOptionBuilders.processDefinition(fn);
  }

  public static final class Builder
      extends SortOption.AbstractBuilder<ProcessDefinitionSort.Builder>
      implements ObjectBuilder<ProcessDefinitionSort> {

    public Builder processDefinitionKey() {
      currentFieldSorting = new FieldSorting("processDefinitionKey", null);
      return this;
    }

    public Builder resourceName() {
      currentFieldSorting = new FieldSorting("resourceName", null);
      return this;
    }

    public Builder name() {
      currentFieldSorting = new FieldSorting("name", null);
      return this;
    }

    public Builder version() {
      currentFieldSorting = new FieldSorting("version", null);
      return this;
    }

    public Builder versionTag() {
      currentFieldSorting = new FieldSorting("versionTag", null);
      return this;
    }

    public Builder processDefinitionId() {
      currentFieldSorting = new FieldSorting("processDefinitionId", null);
      return this;
    }

    public Builder tenantId() {
      currentFieldSorting = new FieldSorting("tenantId", null);
      return this;
    }

    @Override
    protected ProcessDefinitionSort.Builder self() {
      return this;
    }

    @Override
    public ProcessDefinitionSort build() {
      return new ProcessDefinitionSort(fieldSortings);
    }
  }
}
