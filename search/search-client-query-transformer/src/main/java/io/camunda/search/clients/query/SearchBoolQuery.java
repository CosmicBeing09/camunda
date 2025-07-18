/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.clients.query;

import static io.camunda.util.CollectionUtil.addValuesToList;

import io.camunda.util.ObjectBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record SearchBoolQuery(
    List<Query> filter,
    List<Query> must,
    List<Query> mustNot,
    List<Query> should)
    implements QueryOption {

  public static final class Builder implements ObjectBuilder<SearchBoolQuery> {

    private List<Query> filter;
    private List<Query> must;
    private List<Query> mustNot;
    private List<Query> should;

    public Builder filter(final List<Query> queries) {
      filter = addValuesToList(filter, queries);
      return this;
    }

    public Builder must(final List<Query> queries) {
      must = addValuesToList(must, queries);
      return this;
    }

    public Builder mustNot(final List<Query> queries) {
      mustNot = addValuesToList(mustNot, queries);
      return this;
    }

    public Builder should(final List<Query> queries) {
      should = addValuesToList(should, queries);
      return this;
    }

    @Override
    public SearchBoolQuery build() {
      return new SearchBoolQuery(
          Objects.requireNonNullElse(filter, Collections.emptyList()),
          Objects.requireNonNullElse(must, Collections.emptyList()),
          Objects.requireNonNullElse(mustNot, Collections.emptyList()),
          Objects.requireNonNullElse(should, Collections.emptyList()));
    }
  }
}
