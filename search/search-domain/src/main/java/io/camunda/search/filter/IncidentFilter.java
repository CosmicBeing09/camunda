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

import io.camunda.search.entities.IncidentEntity.ErrorType;
import io.camunda.search.entities.IncidentEntity.IncidentState;
import io.camunda.util.ObjectBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record IncidentFilter(
    List<Long> incidentKeyOperations,
    List<Long> processDefinitionKeyOperations,
    List<String> processDefinitionIdOperations,
    List<Long> processInstanceKeyOperations,
    List<ErrorType> errorTypeOperations,
    List<String> errorMessageOperations,
    List<Integer> errorMessageHashOperations,
    List<String> flowNodeIdOperations,
    List<Long> flowNodeInstanceKeyOperations,
    DateValueFilter creationTime,
    List<IncidentState> states,
    String treePath,
    List<Long> jobKeys,
    List<String> tenantIds)
    implements FilterBase {

  public static final class Builder implements ObjectBuilder<IncidentFilter> {

    private List<Long> incidentKeyOperations;
    private List<Long> processDefinitionKeyOperations;
    private List<String> processDefinitionIdOperations;
    private List<Long> processInstanceKeys;
    private List<ErrorType> errorTypes;
    private List<String> errorMessages;
    private List<Integer> errorMessageHashes;
    private List<String> flowNodeIds;
    private List<Long> flowNodeInstanceKeys;
    private DateValueFilter creationTimeFilter;
    private List<IncidentState> states;
    private String treePath;
    private List<Long> jobKeys;
    private List<String> tenantIds;

    public Builder incidentKeys(final Long value, final Long... values) {
      return incidentKeys(collectValues(value, values));
    }

    public Builder incidentKeys(final List<Long> values) {
      incidentKeyOperations = addValuesToList(incidentKeyOperations, values);
      return this;
    }

    public Builder processDefinitionKeys(final Long value, final Long... values) {
      return processDefinitionKeyOperations(collectValues(value, values));
    }

    public Builder processDefinitionKeyOperations(final List<Long> values) {
      processDefinitionKeyOperations = addValuesToList(processDefinitionKeyOperations, values);
      return this;
    }

    public Builder processDefinitionIds(final String value, final String... values) {
      return processDefinitionIds(collectValues(value, values));
    }

    public Builder processDefinitionIds(final List<String> values) {
      processDefinitionIdOperations = addValuesToList(processDefinitionIdOperations, values);
      return this;
    }

    public Builder processInstanceKeys(final Long value, final Long... values) {
      return processInstanceKeys(collectValues(value, values));
    }

    public Builder processInstanceKeys(final List<Long> values) {
      processInstanceKeys = addValuesToList(processInstanceKeys, values);
      return this;
    }

    public Builder errorTypes(final ErrorType value, final ErrorType... values) {
      return errorTypes(collectValues(value, values));
    }

    public Builder errorTypes(final List<ErrorType> values) {
      errorTypes = addValuesToList(errorTypes, values);
      return this;
    }

    public Builder errorMessages(final String value, final String... values) {
      return errorMessages(collectValues(value, values));
    }

    public Builder errorMessages(final List<String> values) {
      errorMessages = addValuesToList(errorMessages, values);
      return this;
    }

    public Builder errorMessageHashes(final Integer value, final Integer... values) {
      return errorMessageHashes(collectValues(value, values));
    }

    public Builder errorMessageHashes(final List<Integer> values) {
      errorMessageHashes = addValuesToList(errorMessageHashes, values);
      return this;
    }

    public Builder creationTime(final DateValueFilter value) {
      creationTimeFilter = value;
      return this;
    }

    public Builder flowNodeIds(final String value, final String... values) {
      return flowNodeIds(collectValues(value, values));
    }

    public Builder flowNodeIds(final List<String> values) {
      flowNodeIds = addValuesToList(flowNodeIds, values);
      return this;
    }

    public Builder flowNodeInstanceKeys(final Long value, final Long... values) {
      return flowNodeInstanceKeys(collectValues(value, values));
    }

    public Builder flowNodeInstanceKeys(final List<Long> values) {
      flowNodeInstanceKeys = addValuesToList(flowNodeInstanceKeys, values);
      return this;
    }

    public Builder states(final IncidentState value, final IncidentState... values) {
      return states(collectValues(value, values));
    }

    public Builder states(final List<IncidentState> values) {
      states = addValuesToList(states, values);
      return this;
    }

    public Builder treePath(final String value) {
      treePath = value;
      return this;
    }

    public Builder jobKeys(final Long value, final Long... values) {
      return jobKeys(collectValues(value, values));
    }

    public Builder jobKeys(final List<Long> values) {
      jobKeys = addValuesToList(jobKeys, values);
      return this;
    }

    public Builder tenantIds(final String value, final String... values) {
      return tenantIds(collectValues(value, values));
    }

    public Builder tenantIds(final List<String> values) {
      tenantIds = addValuesToList(tenantIds, values);
      return this;
    }

    @Override
    public IncidentFilter build() {
      return new IncidentFilter(
          Objects.requireNonNullElse(incidentKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionKeyOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionIdOperations, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(errorTypes, Collections.emptyList()),
          Objects.requireNonNullElse(errorMessages, Collections.emptyList()),
          Objects.requireNonNullElse(errorMessageHashes, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeIds, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeInstanceKeys, Collections.emptyList()),
          creationTimeFilter,
          Objects.requireNonNullElse(states, Collections.emptyList()),
          treePath,
          Objects.requireNonNullElse(jobKeys, Collections.emptyList()),
          Objects.requireNonNullElse(tenantIds, Collections.emptyList()));
    }
  }
}
