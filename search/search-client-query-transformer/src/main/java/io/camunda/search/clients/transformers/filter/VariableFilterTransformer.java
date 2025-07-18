/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.clients.transformers.filter;

import static io.camunda.search.clients.query.SearchQueryBuilders.and;
import static io.camunda.search.clients.query.SearchQueryBuilders.longOperations;
import static io.camunda.search.clients.query.SearchQueryBuilders.stringOperations;
import static io.camunda.search.clients.query.SearchQueryBuilders.stringTerms;
import static io.camunda.search.clients.query.SearchQueryBuilders.term;
import static io.camunda.search.clients.query.SearchQueryBuilders.variableOperations;
import static io.camunda.webapps.schema.descriptors.IndexDescriptor.TENANT_ID;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.IS_PREVIEW;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.KEY;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.NAME;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.PROCESS_INSTANCE_KEY;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.SCOPE_KEY;
import static io.camunda.webapps.schema.descriptors.template.VariableTemplate.VALUE;
import static java.util.Optional.ofNullable;

import io.camunda.search.clients.query.Query;
import io.camunda.search.filter.Operation;
import io.camunda.search.filter.UntypedOperation;
import io.camunda.search.filter.VariableFilter;
import io.camunda.webapps.schema.descriptors.IndexDescriptor;
import java.util.ArrayList;
import java.util.List;

public class VariableFilterTransformer extends IndexFilterTransformer<VariableFilter> {

  public VariableFilterTransformer(final IndexDescriptor indexDescriptor) {
    super(indexDescriptor);
  }

  @Override
  public Query toSearchQuery(final VariableFilter filter) {
    final var queries = new ArrayList<Query>();
    queries.addAll(stringOperations(NAME, filter.nameOperations()));
    queries.addAll(getVariablesQuery(filter.valueOperations()));
    queries.addAll(getScopeKeyQuery(filter.scopeKeyOperations()));
    queries.addAll(getProcessInstanceKeyQuery(filter.processInstanceKeyOperations()));
    queries.addAll(getVariableKeyQuery(filter.variableKeyOperations()));
    ofNullable(getTenantIdQuery(filter.tenantIds())).ifPresent(queries::add);
    ofNullable(getIsTruncatedQuery(filter.isTruncated())).ifPresent(queries::add);
    return and(queries);
  }

  private List<Query> getVariablesQuery(final List<UntypedOperation> variableFilters) {
    return variableOperations(VALUE, variableFilters);
  }

  private List<Query> getScopeKeyQuery(final List<Operation<Long>> scopeKey) {
    return longOperations(SCOPE_KEY, scopeKey);
  }

  private List<Query> getProcessInstanceKeyQuery(
      final List<Operation<Long>> processInstanceKey) {
    return longOperations(PROCESS_INSTANCE_KEY, processInstanceKey);
  }

  private List<Query> getVariableKeyQuery(final List<Operation<Long>> variableKeys) {
    return longOperations(KEY, variableKeys);
  }

  private Query getTenantIdQuery(final List<String> tenant) {
    return stringTerms(TENANT_ID, tenant);
  }

  private Query getIsTruncatedQuery(final Boolean isTruncated) {
    if (isTruncated == null) {
      return null;
    }
    return term(IS_PREVIEW, isTruncated);
  }
}
