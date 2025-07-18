/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.es.transformers.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.search.clients.query.Query;
import io.camunda.search.clients.query.SearchQueryBuilders;
import io.camunda.search.clients.transformers.SearchTransfomer;
import io.camunda.search.es.transformers.ElasticsearchTransformers;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WildcardQueryTransformerTest {

  private final ElasticsearchTransformers transformers = new ElasticsearchTransformers();
  private SearchTransfomer<Query, co.elastic.clients.elasticsearch._types.query_dsl.Query> transformer;

  @BeforeEach
  public void before() {
    transformer = transformers.getTransformer(Query.class);
  }

  private static Stream<Arguments> provideRangeQueries() {
    return Stream.of(
        Arguments.arguments(
            SearchQueryBuilders.wildcardQuery("foo", "*"),
            "Query: {'wildcard':{'foo':{'value':'*'}}}"),
        Arguments.arguments(
            SearchQueryBuilders.wildcard().field("foo").value("*").build().toSearchQuery(),
            "Query: {'wildcard':{'foo':{'value':'*'}}}"),
        Arguments.arguments(
            SearchQueryBuilders.wildcard().field("foo").value(null).build().toSearchQuery(),
            "Query: {'wildcard':{'foo':{}}}"),
        Arguments.arguments(
            SearchQueryBuilders.wildcardQuery("foo", null), "Query: {'wildcard':{'foo':{}}}"));
  }

  @ParameterizedTest
  @MethodSource("provideRangeQueries")
  public void shouldApplyTransformer(final Query query, final String expectedResultQuery) {
    // given
    final var expectedQuery = expectedResultQuery.replace("'", "\"");

    // when
    final var result = transformer.apply(query);

    // then
    assertThat(result).isNotNull();
    assertThat(result.toString()).isEqualTo(expectedQuery);
  }

  @Test
  public void shouldThrowErrorOnNullField() {
    // given

    // when - throw
    assertThatThrownBy(() -> SearchQueryBuilders.wildcard().build())
        .isInstanceOf(NullPointerException.class);
  }
}
