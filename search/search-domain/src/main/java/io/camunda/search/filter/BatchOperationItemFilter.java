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

import io.camunda.util.ObjectBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

FilterBase {

  public static final class Builder implements ObjectBuilder<BatchOperationItemFilter> {

    private List<String> batchOperationIds;
    private List<Long> itemKeys;
    private List<Long> processInstanceKeys;
    private List<String> states;

    public Builder batchOperationIds(final String value, final String... values) {
      return batchOperationIds(collectValues(value, values));
    }

    public Builder batchOperationIds(final List<String> values) {
      batchOperationIds = addValuesToList(batchOperationIds, values);
      return this;
    }

    public Builder itemKeys(final Long value, final Long... values) {
      return itemKeys(collectValues(value, values));
    }

    public Builder itemKeys(final List<Long> values) {
      itemKeys = addValuesToList(itemKeys, values);
      return this;
    }

    public Builder processInstanceKeys(final Long value, final Long... values) {
      return processInstanceKeys(collectValues(value, values));
    }

    public Builder processInstanceKeys(final List<Long> values) {
      processInstanceKeys = addValuesToList(processInstanceKeys, values);
      return this;
    }

    public Builder states(final String value, final String... values) {
      return states(collectValues(value, values));
    }

    public Builder states(final List<String> values) {
      states = addValuesToList(states, values);
      return this;
    }

    @Override
    public BatchOperationItemFilter build() {
      return new BatchOperationItemFilter(
          Objects.requireNonNullElse(batchOperationIds, Collections.emptyList()),
          Objects.requireNonNullElse(itemKeys, Collections.emptyList()),
          Objects.requireNonNullElse(processInstanceKeys, Collections.emptyList()),
          Objects.requireNonNullElse(states, Collections.emptyList()));
    }
  }
}
    implements
public record BatchOperationItemFilter(
    List<String> batchOperationIds,
    List<Long> itemKeys,
    List<Long> processInstanceKeys,
    List<String> states)
