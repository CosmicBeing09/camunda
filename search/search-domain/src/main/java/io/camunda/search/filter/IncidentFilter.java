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
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record IncidentFilter(
    List<FilterOperation<Long>> incidentKeyOperations,
    List<FilterOperation<Long>> processDefinitionKeyOperations,
    List<FilterOperation<String>> processDefinitionIdOperations,
    List<FilterOperation<Long>> processInstanceKeyOperations,
    List<FilterOperation<String>> errorTypeOperations,
    List<FilterOperation<String>> errorMessageOperations,
    List<FilterOperation<Integer>> errorMessageHashOperations,
    List<FilterOperation<String>> flowNodeIdOperations,
    List<FilterOperation<Long>> flowNodeInstanceKeyOperations,
    List<FilterOperation<OffsetDateTime>> creationTimeOperations,
    List<FilterOperation<String>> stateOperations,
    List<FilterOperation<String>> treePathOperations,
    List<FilterOperation<Long>> jobKeyOperations,
    List<FilterOperation<String>> tenantIdOperations)
    implements FilterBase {

  public static final class Builder implements ObjectBuilder<IncidentFilter> {

    private List<FilterOperation<Long>> incidentKeyOperations;
    private List<FilterOperation<Long>> processDefinitionKeyOperations;
    private List<FilterOperation<String>> processDefinitionIdOperations;
    private List<FilterOperation<Long>> processInstanceKeyOperations;
    private List<FilterOperation<String>> errorTypeOperations;
    private List<FilterOperation<String>> errorMessageOperations;
    private List<FilterOperation<Integer>> errorMessageHashOperations;
    private List<FilterOperation<String>> flowNodeIdOperations;
    private List<FilterOperation<Long>> flowNodeInstanceKeyOperations;
    private List<FilterOperation<OffsetDateTime>> creationTimeOperations;
    private List<FilterOperation<String>> stateOperations;
    private List<FilterOperation<String>> treePathOperations;
    private List<FilterOperation<Long>> jobKeyOperations;
    private List<FilterOperation<String>> tenantIdOperations;

    public Builder incidentKeys(final Long value, final Long... values) {
      return incidentKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder incidentKeyOperations(final List<FilterOperation<Long>> operations) {
      incidentKeyOperations = addValuesToList(incidentKeyOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder incidentKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return incidentKeyOperations(collectValues(operation, operations));
    }

    public Builder processDefinitionKeys(final Long value, final Long... values) {
      return processDefinitionKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder processDefinitionKeyOperations(final List<FilterOperation<Long>> operations) {
      processDefinitionKeyOperations = addValuesToList(processDefinitionKeyOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder processDefinitionKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return processDefinitionKeyOperations(collectValues(operation, operations));
    }

    public Builder processDefinitionIds(final String value, final String... values) {
      return processDefinitionIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder processDefinitionIdOperations(final List<FilterOperation<String>> operations) {
      processDefinitionIdOperations = addValuesToList(processDefinitionIdOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder processDefinitionIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return processDefinitionIdOperations(collectValues(operation, operations));
    }

    public Builder processInstanceKeys(final Long value, final Long... values) {
      return processInstanceKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder processInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      processInstanceKeyOperations = addValuesToList(processInstanceKeyOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder processInstanceKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return processInstanceKeyOperations(collectValues(operation, operations));
    }

    public Builder errorTypes(final String value, final String... values) {
      return errorTypeOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder errorTypeOperations(final List<FilterOperation<String>> operations) {
      errorTypeOperations = addValuesToList(errorTypeOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder errorTypeOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return errorTypeOperations(collectValues(operation, operations));
    }

    public Builder errorMessages(final String value, final String... values) {
      return errorMessageOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder errorMessageOperations(final List<FilterOperation<String>> values) {
      errorMessageOperations = addValuesToList(errorMessageOperations, values);
      return this;
    }

    @SafeVarargs
    public final Builder errorMessageOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return errorMessageOperations(collectValues(operation, operations));
    }

    public Builder errorMessageHashes(final Integer value, final Integer... values) {
      return errorMessageHashOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder errorMessageHashOperations(final List<FilterOperation<Integer>> operations) {
      errorMessageHashOperations = addValuesToList(errorMessageHashOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder errorMessageHashOperations(
        final FilterOperation<Integer> operation, final FilterOperation<Integer>... operations) {
      return errorMessageHashOperations(collectValues(operation, operations));
    }

    public Builder creationTime(final OffsetDateTime value) {
      return creationTimeOperations(FilterUtil.mapDefaultToOperation(value));
    }

    public Builder creationTimeOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      creationTimeOperations = addValuesToList(creationTimeOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder creationTimeOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return creationTimeOperations(collectValues(operation, operations));
    }

    public Builder flowNodeIds(final String value, final String... values) {
      return flowNodeIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder flowNodeIdOperations(final List<FilterOperation<String>> operations) {
      flowNodeIdOperations = addValuesToList(flowNodeIdOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder flowNodeIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return flowNodeIdOperations(collectValues(operation, operations));
    }

    public Builder flowNodeInstanceKeys(final Long value, final Long... values) {
      return flowNodeInstanceKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder flowNodeInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      flowNodeInstanceKeyOperations = addValuesToList(flowNodeInstanceKeyOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder flowNodeInstanceKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return flowNodeInstanceKeyOperations(collectValues(operation, operations));
    }

    public Builder states(final String value, final String... values) {
      return stateOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder stateOperations(final List<FilterOperation<String>> values) {
      stateOperations = addValuesToList(stateOperations, values);
      return this;
    }

    @SafeVarargs
    public final Builder stateOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return stateOperations(collectValues(operation, operations));
    }

    public Builder treePaths(final String value, final String... values) {
      return treePathOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder treePathOperations(final List<FilterOperation<String>> operations) {
      treePathOperations = addValuesToList(treePathOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder treePathOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return treePathOperations(collectValues(operation, operations));
    }

    public Builder jobKeys(final Long value, final Long... values) {
      return jobKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder jobKeyOperations(final List<FilterOperation<Long>> operations) {
      jobKeyOperations = addValuesToList(jobKeyOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder jobKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return jobKeyOperations(collectValues(operation, operations));
    }

    public Builder tenantIds(final String value, final String... values) {
      return tenantIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder tenantIdOperations(final List<FilterOperation<String>> operations) {
      tenantIdOperations = addValuesToList(tenantIdOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder tenantIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return tenantIdOperations(collectValues(operation, operations));
    }

    @Override
    public IncidentFilter build() {
      return new IncidentFilter(
          Objects.requireNonNullElse(incidentKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(errorTypeOperations, Collections.emptyList()),
          Objects.requireNonNullElse(errorMessageOperations, Collections.emptyList()),
          Objects.requireNonNullElse(errorMessageHashOperations, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeInstanceKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(creationTimeOperations, Collections.emptyList()),
          Objects.requireNonNullElse(stateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(treePathOperations, Collections.emptyList()),
          Objects.requireNonNullElse(jobKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(tenantIdOperations, Collections.emptyList()));
    }
  }
}
