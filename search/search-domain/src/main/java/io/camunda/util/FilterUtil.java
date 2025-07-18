/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.util;

import io.camunda.search.filter.Operation;
import java.util.List;

public final class FilterUtil {

  private FilterUtil() {}

  public static <T> Operation<T> mapDefaultToOperation(final List<T> valuesOperations) {
    if (valuesOperations.isEmpty()) {
      return null;
    }
    return valuesOperations.size() == 1 ? Operation.eq(valuesOperations.getFirst()) : Operation.in(
        valuesOperations);
  }

  @SafeVarargs
  public static <T> Operation<T> mapDefaultToOperation(final T value, final T... valuesOperations) {
    return mapDefaultToOperation(CollectionUtil.collectValues(value, valuesOperations));
  }
}
