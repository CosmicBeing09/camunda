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

import io.camunda.search.entities.FlowNodeInstanceEntity.FlowNodeType;
import io.camunda.util.FilterUtil;
import io.camunda.util.ObjectBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record FlowNodeInstanceFilter(
    List<Long> flowNodeInstanceKeys,
    List<Long> processInstanceKeys,
    List<Long> processDefinitionKeys,
    List<String> processDefinitionIds,
    List<Operation<String>> stateOperations,
    List<FlowNodeType> types,
    List<String> flowNodeIds,
    List<String> flowNodeNames,
    List<String> treePaths,
    Boolean hasIncident,
    List<Long> incidentKeys,
    List<String> tenantIds,
    List<String> startDates,
    List<String> endDates)
    implements FilterBase {

  public static FlowNodeInstanceFilter of(
      final Function<FlowNodeInstanceFilter.Builder, ObjectBuilder<FlowNodeInstanceFilter>> fn) {
    return FilterBuilders.flowNodeInstance(fn);
  }

  public static final class Builder implements ObjectBuilder<FlowNodeInstanceFilter> {

    private List<Long> flowNodeInstanceKeys;
    private List<Long> processInstanceKeys;
    private List<Long> processDefinitionKeys;
    private List<String> processDefinitionIds;
    private List<Operation<String>> stateOperations;
    private List<FlowNodeType> types;
    private List<String> flowNodeIds;
    private List<String> flowNodeNames;
    private List<String> treePaths;
    private Boolean hasIncident;
    private List<Long> incidentKeys;
    private List<String> tenantIds;
    private List<String> startDates;
    private List<String> endDates;

    public FlowNodeInstanceFilter.Builder flowNodeInstanceKeys(final List<Long> operations) {
      flowNodeInstanceKeys = addValuesToList(flowNodeInstanceKeys, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder flowNodeInstanceKeys(final Long... operations) {
      return flowNodeInstanceKeys(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder processInstanceKeys(final List<Long> operations) {
      processInstanceKeys = addValuesToList(processInstanceKeys, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder processInstanceKeys(final Long... operations) {
      return processInstanceKeys(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder processDefinitionKeys(final List<Long> operations) {
      processDefinitionKeys = addValuesToList(processDefinitionKeys, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder processDefinitionKeys(final Long... operations) {
      return processDefinitionKeys(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder processDefinitionIds(final List<String> operations) {
      processDefinitionIds = addValuesToList(processDefinitionIds, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder processDefinitionIds(final String... operations) {
      return processDefinitionIds(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder stateOperations(
        final List<Operation<String>> operations) {
      stateOperations = addValuesToList(stateOperations, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder states(final String value, final String... operations) {
      return stateOperations(FilterUtil.mapDefaultToOperation(value, operations));
    }

    @SafeVarargs
    public final FlowNodeInstanceFilter.Builder stateOperations(
        final Operation<String> operation, final Operation<String>... operations) {
      return stateOperations(collectValues(operation, operations));
    }

    public FlowNodeInstanceFilter.Builder types(final List<FlowNodeType> operations) {
      types = addValuesToList(types, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder types(final FlowNodeType... operations) {
      return types(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder flowNodeIds(final List<String> operations) {
      flowNodeIds = addValuesToList(flowNodeIds, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder flowNodeIds(final String... operations) {
      return flowNodeIds(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder flowNodeNames(final List<String> operations) {
      flowNodeNames = addValuesToList(flowNodeNames, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder flowNodeNames(final String... operations) {
      return flowNodeNames(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder treePaths(final List<String> operations) {
      treePaths = addValuesToList(treePaths, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder treePaths(final String... operations) {
      return treePaths(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder hasIncident(final Boolean value) {
      hasIncident = value;
      return this;
    }

    public FlowNodeInstanceFilter.Builder incidentKeys(final List<Long> operations) {
      incidentKeys = addValuesToList(incidentKeys, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder incidentKeys(final Long... operations) {
      return incidentKeys(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder tenantIds(final List<String> operations) {
      tenantIds = addValuesToList(tenantIds, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder tenantIds(final String... operations) {
      return tenantIds(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder startDates(final List<String> operations) {
      startDates = addValuesToList(startDates, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder startDates(final String... operations) {
      return startDates(collectValuesAsList(operations));
    }

    public FlowNodeInstanceFilter.Builder endDates(final List<String> operations) {
      endDates = addValuesToList(endDates, operations);
      return this;
    }

    public FlowNodeInstanceFilter.Builder endDates(final String... operations) {
      return endDates(collectValuesAsList(operations));
    }

    @Override
    public FlowNodeInstanceFilter build() {
      return new FlowNodeInstanceFilter(
          Objects.requireNonNullElse(flowNodeInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processDefinitionIds, Collections.emptyList()),
          Objects.requireNonNullElse(stateOperations, Collections.emptyList()),
          Objects.requireNonNullElse(types, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeIds, Collections.emptyList()),
          Objects.requireNonNullElse(flowNodeNames, Collections.emptyList()),
          Objects.requireNonNullElse(treePaths, Collections.emptyList()),
          hasIncident,
          Objects.requireNonNullElse(incidentKeys, Collections.emptyList()),
          Objects.requireNonNullElse(tenantIds, Collections.emptyList()),
          Objects.requireNonNullElse(startDates, Collections.emptyList()),
          Objects.requireNonNullElse(endDates, Collections.emptyList()));
    }
  }
}