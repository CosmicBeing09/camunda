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

    private List<Operation<String>> batchOperationIdOperations;
    private List<String> operationTypes;
    private List<String> states;

    public Builder batchOperationIdOperations(final List<Operation<String>> batchOperationIdOperations) {
      this.batchOperationIdOperations = addValuesToList(this.batchOperationIdOperations,
          batchOperationIdOperations);
      return this;
    }

    public Builder batchOperationIds(final String batchOperationId, final String... batchOperationIds) {
      return batchOperationIdOperations(FilterUtil.mapDefaultToOperation(batchOperationId, batchOperationIds));
    }

    public Builder replaceBatchOperationIdOperations(final List<Operation<String>> batchOperationIdOperations) {
      this.batchOperationIdOperations = new ArrayList<>(batchOperationIdOperations);
      return this;
    }

    @SafeVarargs
    public final Builder batchOperationIdOperations(
        final Operation<String> operation, final Operation<String>... operations) {
      return batchOperationIdOperations(collectValues(operation, operations));
    }

    public Builder operationTypes(final String operationType, final String... operationTypes) {
      return operationTypes(collectValues(operationType, operationTypes));
    }

    public Builder operationTypes(final List<String> operationTypes) {
      this.operationTypes = addValuesToList(this.operationTypes, operationTypes);
      return this;
    }

    public Builder state(final String stateValue, final String... states) {
      return state(collectValues(stateValue, states));
    }

    public Builder state(final List<String> states) {
      this.states = addValuesToList(this.states, states);
      return this;
    }

    @Override
    public BatchOperationFilter build() {
      return new BatchOperationFilter(
          Objects.requireNonNullElse(batchOperationIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(operationTypes, Collections.emptyList()),
          Objects.requireNonNullElse(states, Collections.emptyList()));
    }
  }
}
    implements
public record BatchOperationFilter(
    List<Operation<String>> batchOperationIdOperations,
    List<String> operationTypes,
    List<String> states)
