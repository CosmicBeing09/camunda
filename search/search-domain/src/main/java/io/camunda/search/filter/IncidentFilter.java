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
    List<Long> incidentKeys,
    List<Long> processDefinitionKeys,
    List<String> processDefinitionIds,
    List<Long> processInstanceKeys,
    List<ErrorType> errorTypes,
    List<String> errorMessages,
    List<Integer> errorMessageHashes,
    List<String> flowNodeIds,
    List<Long> flowNodeInstanceKeys,
    DateValueFilter creationTime,
    List<IncidentState> states,
    String treePath,
    List<Long> jobKeys,
    List<String> tenantIds)
    implements FilterBase {

  public static final class Builder implements ObjectBuilder<IncidentFilter> {

    private List<Long> incidentKeys;
    private List<Long> processDefinitionKeys;
    private List<String> processDefinitionIds;
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

    public Builder incidentKeys(final Long value, final Long... operations) {
      return incidentKeys(collectValues(value, operations));
    }

    public Builder incidentKeys(final List<Long> operations) {
      incidentKeys = addValuesToList(incidentKeys, operations);
      return this;
    }

    public Builder processDefinitionKeys(final Long value, final Long... operations) {
      return processDefinitionKeys(collectValues(value, operations));
    }

    public Builder processDefinitionKeys(final List<Long> operations) {
      processDefinitionKeys = addValuesToList(processDefinitionKeys, operations);
      return this;
    }

    public Builder processDefinitionIds(final String value, final String... operations) {
      return processDefinitionIds(collectValues(value, operations));
    }

    public Builder processDefinitionIds(final List<String> operations) {
      processDefinitionIds = addValuesToList(processDefinitionIds, operations);
      return this;
    }

    public Builder processInstanceKeys(final Long value, final Long... operations) {
      return processInstanceKeys(collectValues(value, operations));
    }

    public Builder processInstanceKeys(final List<Long> operations) {
      processInstanceKeys = addValuesToList(processInstanceKeys, operations);
      return this;
    }

    public Builder errorTypes(final ErrorType value, final ErrorType... operations) {
      return errorTypes(collectValues(value, operations));
    }

    public Builder errorTypes(final List<ErrorType> operations) {
      errorTypes = addValuesToList(errorTypes, operations);
      return this;
    }

    public Builder errorMessages(final String value, final String... operations) {
      return errorMessages(collectValues(value, operations));
    }

    public Builder errorMessages(final List<String> operations) {
      errorMessages = addValuesToList(errorMessages, operations);
      return this;
    }

    public Builder errorMessageHashes(final Integer value, final Integer... operations) {
      return errorMessageHashes(collectValues(value, operations));
    }

    public Builder errorMessageHashes(final List<Integer> operations) {
      errorMessageHashes = addValuesToList(errorMessageHashes, operations);
      return this;
    }

    public Builder creationTime(final DateValueFilter value) {
      creationTimeFilter = value;
      return this;
    }

    public Builder flowNodeIds(final String value, final String... operations) {
      return flowNodeIds(collectValues(value, operations));
    }

    public Builder flowNodeIds(final List<String> operations) {
      flowNodeIds = addValuesToList(flowNodeIds, operations);
      return this;
    }

    public Builder flowNodeInstanceKeys(final Long value, final Long... operations) {
      return flowNodeInstanceKeys(collectValues(value, operations));
    }

    public Builder flowNodeInstanceKeys(final List<Long> operations) {
      flowNodeInstanceKeys = addValuesToList(flowNodeInstanceKeys, operations);
      return this;
    }

    public Builder states(final IncidentState value, final IncidentState... operations) {
      return states(collectValues(value, operations));
    }

    public Builder states(final List<IncidentState> operations) {
      states = addValuesToList(states, operations);
      return this;
    }

    public Builder treePath(final String value) {
      treePath = value;
      return this;
    }

    public Builder jobKeys(final Long value, final Long... operations) {
      return jobKeys(collectValues(value, operations));
    }

    public Builder jobKeys(final List<Long> operations) {
      jobKeys = addValuesToList(jobKeys, operations);
      return this;
    }

    public Builder tenantIds(final String value, final String... operations) {
      return tenantIds(collectValues(value, operations));
    }

    public Builder tenantIds(final List<String> operations) {
      tenantIds = addValuesToList(tenantIds, operations);
      return this;
    }

    @Override
    public IncidentFilter build() {
      return new IncidentFilter(
          Objects.requireNonNullElse(incidentKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionIds, Collections.emptyList()),
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