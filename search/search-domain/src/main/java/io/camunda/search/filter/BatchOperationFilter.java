/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.filter;

import static io.camunda.util.CollectionUtil.addValuesToList;
import static io.camunda.util.CollectionUtil.collectValues;

import io.camunda.util.FilterUtil;
import io.camunda.util.ObjectBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record BatchOperationFilter(
    List<FilterOperation<String>> batchOperationIdOperations,
    List<FilterOperation<String>> operationTypeOperations,
    List<FilterOperation<String>> stateOperations)
    implements FilterBase {

  public static final class Builder implements ObjectBuilder<BatchOperationFilter> {

    private List<FilterOperation<String>> batchOperationIdOperations;
    private List<FilterOperation<String>> operationTypeOperations;
    private List<FilterOperation<String>> stateOperations;

    public Builder batchOperationIdOperations(final List<FilterOperation<String>> operations) {
      batchOperationIdOperations = addValuesToList(batchOperationIdOperations, operations);
      return this;
    }

    public Builder batchOperationIds(final String value, final String... values) {
      return batchOperationIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder replaceBatchOperationIdOperations(final List<FilterOperation<String>> operations) {
      batchOperationIdOperations = new ArrayList<>(operations);
      return this;
    }

    @SafeVarargs
    public final Builder batchOperationIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return batchOperationIdOperations(collectValues(operation, operations));
    }

    public Builder operationTypeOperations(final List<FilterOperation<String>> operations) {
      operationTypeOperations = addValuesToList(operationTypeOperations, operations);
      return this;
    }

    public Builder operationTypes(final String value, final String... values) {
      return operationTypeOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder replaceOperationTypeOperations(final List<FilterOperation<String>> operations) {
      operationTypeOperations = new ArrayList<>(operations);
      return this;
    }

    @SafeVarargs
    public final Builder operationTypeOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return operationTypeOperations(collectValues(operation, operations));
    }

    public Builder stateOperations(final List<FilterOperation<String>> operations) {
      stateOperations = addValuesToList(stateOperations, operations);
      return this;
    }

    public Builder states(final String value, final String... values) {
      return stateOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder replaceStateOperations(final List<FilterOperation<String>> operations) {
      stateOperations = new ArrayList<>(operations);
      return this;
    }

    @SafeVarargs
    public final Builder stateOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return stateOperations(collectValues(operation, operations));
    }

    @Override
    public BatchOperationFilter build() {
      return new BatchOperationFilter(
          Objects.requireNonNullElse(batchOperationIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(operationTypeOperations, Collections.emptyList()),
          Objects.requireNonNullElse(stateOperations, Collections.emptyList()));
    }
  }
}
