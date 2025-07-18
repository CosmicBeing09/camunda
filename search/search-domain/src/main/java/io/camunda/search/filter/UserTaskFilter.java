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
import static io.camunda.util.CollectionUtil.collectValuesAsList;

import io.camunda.util.FilterUtil;
import io.camunda.util.ObjectBuilder;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record UserTaskFilter(
    List<Long> userTaskKeys,
    List<String> elementIds,
    List<String> names,
    List<String> bpmnProcessIds,
    List<FilterOperation<String>> assigneeOperations,
    List<FilterOperation<Integer>> priorityOperations,
    List<String> states,
    List<Long> processInstanceKeys,
    List<Long> processDefinitionKeys,
    List<FilterOperation<String>> candidateUserOperations,
    List<FilterOperation<String>> candidateGroupOperations,
    List<String> tenantIds,
    List<VariableValueFilter> processInstanceVariableFilter,
    List<VariableValueFilter> localVariableFilters,
    List<Long> elementInstanceKeys,
    List<FilterOperation<OffsetDateTime>> creationDateOperations,
    List<FilterOperation<OffsetDateTime>> completionDateOperations,
    List<FilterOperation<OffsetDateTime>> followUpDateOperations,
    List<FilterOperation<OffsetDateTime>> dueDateOperations,
    String type)
    implements FilterBase {

  public static final class Builder implements ObjectBuilder<UserTaskFilter> {

    private List<Long> userTaskKeys;
    private List<String> elementIds;
    private List<String> names;
    private List<String> bpmnProcessIds;
    private List<FilterOperation<String>> assigneeOperations;
    private List<FilterOperation<Integer>> priorityOperations;
    private List<String> states;
    private List<Long> processInstanceKeys;
    private List<Long> processDefinitionKeys;
    private List<FilterOperation<String>> candidateUserOperations;
    private List<FilterOperation<String>> candidateGroupOperations;
    private List<String> tenantIds;
    private List<VariableValueFilter> processInstanceVariableFilters;
    private List<VariableValueFilter> localVariableFilters;
    private List<Long> elementInstanceKeys;
    private List<FilterOperation<OffsetDateTime>> creationDateOperations;
    private List<FilterOperation<OffsetDateTime>> completionDateOperations;
    private List<FilterOperation<OffsetDateTime>> followUpDateOperations;
    private List<FilterOperation<OffsetDateTime>> dueDateOperations;
    private String type;

    public Builder userTaskKeys(final Long... values) {
      return userTaskKeys(collectValuesAsList(values));
    }

    public Builder userTaskKeys(final List<Long> values) {
      userTaskKeys = addValuesToList(userTaskKeys, values);
      return this;
    }

    public Builder elementIds(final String... values) {
      return elementIds(collectValuesAsList(values));
    }

    public Builder elementIds(final List<String> values) {
      elementIds = addValuesToList(elementIds, values);
      return this;
    }

    public Builder names(final String... values) {
      return names(collectValuesAsList(values));
    }

    public Builder names(final List<String> values) {
      names = addValuesToList(names, values);
      return this;
    }

    public Builder bpmnProcessIds(final String... values) {
      return bpmnProcessIds(collectValuesAsList(values));
    }

    public Builder bpmnProcessIds(final List<String> values) {
      bpmnProcessIds = addValuesToList(bpmnProcessIds, values);
      return this;
    }

    public Builder assigneeOperations(final List<FilterOperation<String>> operations) {
      assigneeOperations = addValuesToList(assigneeOperations, operations);
      return this;
    }

    public Builder assignees(final String value, final String... values) {
      return assigneeOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder assigneeOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return assigneeOperations(collectValues(operation, operations));
    }

    public Builder priorityOperations(final List<FilterOperation<Integer>> operations) {
      priorityOperations = addValuesToList(priorityOperations, operations);
      return this;
    }

    public Builder priorities(final Integer value, final Integer... values) {
      return priorityOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder priorityOperations(
        final FilterOperation<Integer> operation, final FilterOperation<Integer>... operations) {
      return priorityOperations(collectValues(operation, operations));
    }

    public Builder states(final String... values) {
      return states(collectValuesAsList(values));
    }

    public Builder states(final List<String> values) {
      states = addValuesToList(states, values);
      return this;
    }

    public Builder processInstanceKeys(final Long... values) {
      return processInstanceKeys(collectValuesAsList(values));
    }

    public Builder processInstanceKeys(final List<Long> values) {
      processInstanceKeys = addValuesToList(processInstanceKeys, values);
      return this;
    }

    public Builder processDefinitionKeys(final Long... values) {
      return processDefinitionKeys(collectValuesAsList(values));
    }

    public Builder processDefinitionKeys(final List<Long> values) {
      processDefinitionKeys = addValuesToList(processDefinitionKeys, values);
      return this;
    }

    public Builder candidateUserOperations(final List<FilterOperation<String>> operations) {
      candidateUserOperations = addValuesToList(candidateUserOperations, operations);
      return this;
    }

    public Builder candidateUsers(final String value, final String... values) {
      return candidateUserOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder candidateUserOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return candidateUserOperations(collectValues(operation, operations));
    }

    public Builder candidateGroupOperations(final List<FilterOperation<String>> operations) {
      candidateGroupOperations = addValuesToList(candidateGroupOperations, operations);
      return this;
    }

    public Builder candidateGroups(final String value, final String... values) {
      return candidateGroupOperations(FilterUtil.mapDefaultToOperation(value, values));
    }

    @SafeVarargs
    public final Builder candidateGroupOperations(
        final FilterOperation<String> operation, final FilterOperation<String>... operations) {
      return candidateGroupOperations(collectValues(operation, operations));
    }

    public Builder tenantIds(final String... values) {
      return tenantIds(collectValuesAsList(values));
    }

    public Builder tenantIds(final List<String> values) {
      tenantIds = addValuesToList(tenantIds, values);
      return this;
    }

    public Builder processInstanceVariables(final List<VariableValueFilter> values) {
      processInstanceVariableFilters = addValuesToList(processInstanceVariableFilters, values);
      return this;
    }

    public Builder localVariables(final List<VariableValueFilter> values) {
      localVariableFilters = addValuesToList(localVariableFilters, values);
      return this;
    }

    public Builder elementInstanceKeys(final Long... values) {
      return elementInstanceKeys(collectValuesAsList(values));
    }

    public Builder elementInstanceKeys(final List<Long> values) {
      elementInstanceKeys = addValuesToList(elementInstanceKeys, values);
      return this;
    }

    public Builder creationDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      creationDateOperations = addValuesToList(creationDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder creationDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return creationDateOperations(collectValues(operation, operations));
    }

    public Builder completionDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      completionDateOperations = addValuesToList(completionDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder completionDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return completionDateOperations(collectValues(operation, operations));
    }

    public Builder followUpDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      followUpDateOperations = addValuesToList(followUpDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder followUpDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return followUpDateOperations(collectValues(operation, operations));
    }

    public Builder dueDateOperations(final List<FilterOperation<OffsetDateTime>> operations) {
      dueDateOperations = addValuesToList(dueDateOperations, operations);
      return this;
    }

    @SafeVarargs
    public final Builder dueDateOperations(
        final FilterOperation<OffsetDateTime> operation, final FilterOperation<OffsetDateTime>... operations) {
      return dueDateOperations(collectValues(operation, operations));
    }

    public Builder type(final String value) {
      type = value;
      return this;
    }

    @Override
    public UserTaskFilter build() {
      return new UserTaskFilter(
          Objects.requireNonNullElse(userTaskKeys, Collections.emptyList()),
          Objects.requireNonNullElse(elementIds, Collections.emptyList()),
          Objects.requireNonNullElse(names, Collections.emptyList()),
          Objects.requireNonNullElse(bpmnProcessIds, Collections.emptyList()),
          Objects.requireNonNullElse(assigneeOperations, Collections.emptyList()),
          Objects.requireNonNullElse(priorityOperations, Collections.emptyList()),
          Objects.requireNonNullElse(states, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionKeys, Collections.emptyList()),
          Objects.requireNonNullElse(candidateUserOperations, Collections.emptyList()),
          Objects.requireNonNullElse(candidateGroupOperations, Collections.emptyList()),
          Objects.requireNonNullElse(tenantIds, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceVariableFilters, Collections.emptyList()),
          Objects.requireNonNullElse(localVariableFilters, Collections.emptyList()),
          Objects.requireNonNullElse(elementInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(creationDateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(completionDateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(followUpDateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(dueDateOperations, Collections.emptyList()),
          type);
    }
  }
}
