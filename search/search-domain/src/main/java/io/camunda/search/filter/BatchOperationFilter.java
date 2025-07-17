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

FilterBase {

  public static final class Builder implements ObjectBuilder<BatchOperationFilter> {

    private List<Operation<String>> batchOperationIdFilters;
    private List<String> operationTypeFilters;
    private List<String> stateFilters;

    public Builder batchOperationIdFilters(final List<Operation<String>> operations) {
      batchOperationIdFilters = addValuesToList(batchOperationIdFilters, operations);
      return this;
    }

    public Builder batchOperationIds(final String value, final String... values) {
      return batchOperationIdFilters(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder replaceBatchOperationIdFilters(final List<Operation<String>> operations) {
      batchOperationIdFilters = new ArrayList<>(operations);
      return this;
    }

    @SafeVarargs
    public final Builder batchOperationIdFilters(
        final Operation<String> operation, final Operation<String>... operations) {
      return batchOperationIdFilters(collectValues(operation, operations));
    }

    public Builder operationTypeFiltersUpdated(final String value, final String... values) {
      return operationTypeFilters(collectValues(value, values));
    }

    public Builder operationTypeFilters(final List<String> values) {
      operationTypeFilters = addValuesToList(operationTypeFilters, values);
      return this;
    }

    public Builder state(final String value, final String... values) {
      return state(collectValues(value, values));
    }

    public Builder state(final List<String> values) {
      stateFilters = addValuesToList(stateFilters, values);
      return this;
    }

    @Override
    public BatchOperationFilter build() {
      return new BatchOperationFilter(
          Objects.requireNonNullElse(batchOperationIdFilters, Collections.emptyList()),
          Objects.requireNonNullElse(operationTypeFilters, Collections.emptyList()),
          Objects.requireNonNullElse(stateFilters, Collections.emptyList()));
    }
  }
}
    implements
public record BatchOperationFilter(
    List<Operation<String>> batchOperationIdFilters,
    List<String> operationTypes,
    List<String> state)
