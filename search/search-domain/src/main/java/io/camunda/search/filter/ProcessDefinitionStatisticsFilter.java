/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.filter;

import static io.camunda.util.CollectionUtil.*;

import io.camunda.util.FilterUtil;
import io.camunda.util.ObjectBuilder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ProcessDefinitionStatisticsFilter(
    long processDefinitionKey,
    List<FilterOperation<Long>> processInstanceKeyOperations,
    List<FilterOperation<Long>> parentProcessInstanceKeyOperations,
    List<FilterOperation<Long>> parentFlowNodeInstanceKeyOperations,
    List<FilterOperation<OffsetDateTime>> startDateOperations,
    List<FilterOperation<OffsetDateTime>> endDateOperations,
    List<FilterOperation<String>> stateOperations,
    Boolean hasIncident,
    List<FilterOperation<String>> tenantIdOperations,
    List<VariableValueFilter> variableFilters,
    List<FilterOperation<String>> errorMessageOperations,
    List<FilterOperation<String>> batchOperationIdOperations,
    Boolean hasRetriesLeft,
    List<FilterOperation<String>> flowNodeIdOperations,
    Boolean hasFlowNodeInstanceIncident,
    List<FilterOperation<String>> flowNodeInstanceStateOperations,
    List<Integer> incidentErrorHashCodes,
    List<ProcessDefinitionStatisticsFilter> orFilters)
    implements FilterBase {

  public Builder toBuilder() {
    return new Builder(processDefinitionKey)
        .processInstanceKeyOperations(processInstanceKeyOperations)
        .parentProcessInstanceKeyOperations(parentProcessInstanceKeyOperations)
        .parentFlowNodeInstanceKeyOperations(parentFlowNodeInstanceKeyOperations)
        .startDateOperations(startDateOperations)
        .endDateOperations(endDateOperations)
        .stateOperations(stateOperations)
        .hasIncident(hasIncident)
        .tenantIdOperations(tenantIdOperations)
        .variables(variableFilters)
        .batchOperationIdOperations(batchOperationIdOperations);
  }

  public static final class Builder implements ObjectBuilder<ProcessDefinitionStatisticsFilter> {

    private final long processDefinitionKey;
    private List<FilterOperation<Long>> processInstanceKeyOperations;
    private List<FilterOperation<Long>> parentProcessInstanceKeyOperations;
    private List<FilterOperation<Long>> parentFlowNodeInstanceKeyOperations;
    private List<FilterOperation<OffsetDateTime>> startDateOperations;
    private List<FilterOperation<OffsetDateTime>> endDateOperations;
    private List<FilterOperation<String>> stateOperations;
    private Boolean hasIncident;
    private List<FilterOperation<String>> tenantIdOperations;
    private List<VariableValueFilter> variableFilters;
    private List<FilterOperation<String>> errorMessageOperations;
    private List<FilterOperation<String>> batchOperationIdOperations;
    private Boolean hasRetriesLeft;
    private List<FilterOperation<String>> flowNodeIdOperations;
    private Boolean hasFlowNodeInstanceIncident;
    private List<FilterOperation<String>> flowNodeInstanceStateOperations;
    private List<Integer> incidentErrorHashCodes;
    private List<ProcessDefinitionStatisticsFilter> orFilters;

    public Builder(final long processDefinitionKey) {
      this.processDefinitionKey = processDefinitionKey;
    }

    public Builder processInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      processInstanceKeyOperations = addValuesToList(processInstanceKeyOperations, operations);
      return this;
    }

    public Builder processInstanceKeys(final Long value, final Long... values) {
      return processInstanceKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder replaceProcessInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      processInstanceKeyOperations = operations;
      return this;
    }

    @SafeVarargs
    public final Builder processInstanceKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return processInstanceKeyOperations(collectValues(operation, operations));
    }

    public Builder parentProcessInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      parentProcessInstanceKeyOperations =
          addValuesToList(parentProcessInstanceKeyOperations, operations);
      return this;
    }

    public Builder parentProcessInstanceKeys(final Long value, final Long... values) {
      return parentProcessInstanceKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder parentProcessInstanceKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return parentProcessInstanceKeyOperations(collectValues(operation, operations));
    }

    public Builder parentFlowNodeInstanceKeyOperations(final List<FilterOperation<Long>> operations) {
      parentFlowNodeInstanceKeyOperations =
          addValuesToList(parentFlowNodeInstanceKeyOperations, operations);
      return this;
    }

    public Builder parentFlowNodeInstanceKeys(final Long value, final Long... values) {
      return parentFlowNodeInstanceKeyOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder parentFlowNodeInstanceKeyOperations(
        final FilterOperation<Long> operation, final FilterOperation<Long>... operations) {
      return parentFlowNodeInstanceKeyOperations(collectValues(operation, operations));
    }

    public Builder startDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      startDateOperations = addValuesToList(startDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder startDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return startDateOperations(collectValues(operation, operations));
    }

    public Builder endDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      endDateOperations = addValuesToList(endDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder endDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return endDateOperations(collectValues(operation, operations));
    }

    public Builder stateOperations(final List<FilterOperation<String>> operations) {
      stateOperations = addValuesToList(stateOperations, operations);
      return this;
    }

    public Builder states(final String value, final String... values) {
      return stateOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder stateOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return stateOperations(collectValues(operation, operations));
    }

    public Builder hasIncident(final Boolean value) {
      hasIncident = value;
      return this;
    }

    public Builder tenantIdOperations(final List<FilterOperation<String>> operations) {
      tenantIdOperations = addValuesToList(tenantIdOperations, operations);
      return this;
    }

    public Builder tenantIds(final String value, final String... values) {
      return tenantIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder tenantIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return tenantIdOperations(collectValues(operation, operations));
    }

    public Builder variables(final List<VariableValueFilter> values) {
      variableFilters = addValuesToList(variableFilters, values);
      return this;
    }

    public Builder batchOperationIdOperations(final List<FilterOperation<String>> operations) {
      batchOperationIdOperations = addValuesToList(batchOperationIdOperations, operations);
      return this;
    }

    public Builder batchOperationIds(final String value, final String... values) {
      return batchOperationIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder batchOperationIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return batchOperationIdOperations(collectValues(operation, operations));
    }

    public Builder errorMessages(final String value, final String... values) {
      return errorMessageOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder errorMessageOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return errorMessageOperations(collectValues(operation, operations));
    }

    public Builder errorMessageOperations(final List<FilterOperation<String>> operations) {
      errorMessageOperations = addValuesToList(errorMessageOperations, operations);
      return this;
    }

    public Builder hasRetriesLeft(final Boolean value) {
      hasRetriesLeft = value;
      return this;
    }

    @SafeVarargs
    public final Builder flowNodeIdOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return flowNodeIdOperations(collectValues(operation, operations));
    }

    public Builder flowNodeIdOperations(final List<FilterOperation<String>> values) {
      flowNodeIdOperations = addValuesToList(flowNodeIdOperations, values);
      return this;
    }

    public Builder flowNodeIds(final String value, final String... values) {
      return flowNodeIdOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    public Builder hasFlowNodeInstanceIncident(final Boolean value) {
      hasFlowNodeInstanceIncident = value;
      return this;
    }

    public Builder flowNodeInstanceStateOperations(final List<FilterOperation<String>> operations) {
      flowNodeInstanceStateOperations =
          addValuesToList(flowNodeInstanceStateOperations, operations);
      return this;
    }

    public Builder flowNodeInstanceState(final String value, final String... values) {
      return flowNodeInstanceStateOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder flowNodeInstanceStateOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return flowNodeInstanceStateOperations(collectValues(operation, operations));
    }

    public Builder incidentErrorHashCodes(final Integer value, final Integer... values) {
      return incidentErrorHashCodes(collectValues(value, values));
    }

    public Builder incidentErrorHashCodes(final List<Integer> values) {
      incidentErrorHashCodes = addValuesToList(incidentErrorHashCodes, values);
      return this;
    }

    public Builder addOrOperation(final ProcessDefinitionStatisticsFilter orOperation) {
      if (orFilters == null) {
        orFilters = new ArrayList<>();
      }
      orFilters.add(orOperation);
      return this;
    }

    @Override
    public ProcessDefinitionStatisticsFilter build() {
      return new ProcessDefinitionStatisticsFilter(
          processDefinitionKey,
          Objects.requireNonNullElse(processInstanceKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(parentProcessInstanceKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(parentFlowNodeInstanceKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(startDateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(endDateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(stateOperations, Collections.emptyList()),
          hasIncident,
          Objects.requireNonNullElse(tenantIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(variableFilters, Collections.emptyList()),
          Objects.requireNonNullElse(errorMessageOperations, Collections.emptyList()),
          Objects.requireNonNullElse(batchOperationIdOperations, Collections.emptyList()),
          hasRetriesLeft,
          Objects.requireNonNullElse(flowNodeIdOperations, Collections.emptyList()),
          hasFlowNodeInstanceIncident,
          Objects.requireNonNullElse(flowNodeInstanceStateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(incidentErrorHashCodes, Collections.emptyList()),
          orFilters);
    }
  }
}
