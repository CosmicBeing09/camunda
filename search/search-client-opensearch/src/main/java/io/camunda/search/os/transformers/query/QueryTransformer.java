/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.os.transformers.query;

import io.camunda.search.clients.query.Query;
import io.camunda.search.clients.query.QueryOption;
import io.camunda.search.clients.transformers.SearchTransfomer;
import io.camunda.search.os.transformers.OpensearchTransformer;
import io.camunda.search.os.transformers.OpensearchTransformers;
import org.opensearch.client.opensearch._types.query_dsl.QueryVariant;

public final class QueryTransformer extends OpensearchTransformer<Query, org.opensearch.client.opensearch._types.query_dsl.Query> {

  public QueryTransformer(final OpensearchTransformers transformers) {
    super(transformers);
  }

  @Override
  public org.opensearch.client.opensearch._types.query_dsl.Query apply(final Query value) {
    final var queryOption = value.queryOption();

    if (queryOption == null) {
      return null;
    }

    final var queryOptionCls = queryOption.getClass();
    final var transformer = getQueryOptionTransformer(queryOptionCls);
    final var transformedQueryOption = transformer.apply(queryOption);
    final var query = transformedQueryOption._toQuery();

    return query;
  }

  public <T extends QueryOption, R extends QueryVariant>
      SearchTransfomer<T, R> getQueryOptionTransformer(final Class<?> cls) {
    return getTransformer(cls);
  }
}
