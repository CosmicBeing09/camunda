/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.filter;

import io.camunda.util.ListBuilder;
import io.camunda.util.ObjectBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record VariableValueFilter(String name, List<UntypedOperation> valueOperations)
    implements FilterBase {

  @Override
  public String toString() {
    return "VariableValueFilter["
        + "name="
        + name
        + ", "
        + "valueOperation="
        + valueOperations
        + ']';
  }

  public static final class Builder
      implements ObjectBuilder<VariableValueFilter>, ListBuilder<VariableValueFilter> {

    private String name;
    private final List<UntypedOperation> errorTypeOperations = new ArrayList<>();

    public Builder name(final String value) {
      name = value;
      return this;
    }

    public Builder valueOperation(final UntypedOperation operation) {
      errorTypeOperations.add(operation);
      return this;
    }

    public Builder valueOperations(final List<UntypedOperation> operations) {
      errorTypeOperations.addAll(operations);
      return this;
    }

    public <T> Builder valueTypedOperations(final List<Operation<T>> operations) {
      operations.forEach(operation -> errorTypeOperations.add(UntypedOperation.of(operation)));
      return this;
    }

    @Override
    public VariableValueFilter build() {
      return new VariableValueFilter(Objects.requireNonNull(name), errorTypeOperations);
    }

    @Override
    public List<VariableValueFilter> buildList() {
      final List<VariableValueFilter> variableValueFilters = new ArrayList<>();
      for (final UntypedOperation untypedOperation : errorTypeOperations) {
        final VariableValueFilter variableValueFilter =
            new VariableValueFilter.Builder().name(name).valueOperation(untypedOperation).build();
        variableValueFilters.add(variableValueFilter);
      }
      return variableValueFilters;
    }
  }
}
