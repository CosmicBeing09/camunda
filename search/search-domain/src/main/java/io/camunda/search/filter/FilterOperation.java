/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.filter;

import io.camunda.util.CollectionUtil;
import java.util.List;

public record FilterOperation<T>(Operator operator, List<T> values) {

  public FilterOperation(final Operator operator, final T value) {
    this(operator, CollectionUtil.collectValuesAsList(value));
  }

  public static <T> FilterOperation<T> of(final Operator operator, final T value) {
    return new FilterOperation<>(operator, value);
  }

  public static <T> FilterOperation<T> eq(final T value) {
    return new FilterOperation<>(Operator.EQUALS, value);
  }

  public static <T> FilterOperation<T> neq(final T value) {
    return new FilterOperation<>(Operator.NOT_EQUALS, value);
  }

  public static <T> FilterOperation<T> exists(final boolean value) {
    return new FilterOperation<>(value ? Operator.EXISTS : Operator.NOT_EXISTS, null);
  }

  public static <T> FilterOperation<T> gt(final T value) {
    return new FilterOperation<>(Operator.GREATER_THAN, value);
  }

  public static <T> FilterOperation<T> gte(final T value) {
    return new FilterOperation<>(Operator.GREATER_THAN_EQUALS, value);
  }

  public static <T> FilterOperation<T> lt(final T value) {
    return new FilterOperation<>(Operator.LOWER_THAN, value);
  }

  public static <T> FilterOperation<T> lte(final T value) {
    return new FilterOperation<>(Operator.LOWER_THAN_EQUALS, value);
  }

  @SafeVarargs
  public static <T> FilterOperation<T> in(final T... values) {
    return new FilterOperation<>(Operator.IN, List.of(values));
  }

  public static <T> FilterOperation<T> in(final List<T> values) {
    return new FilterOperation<>(Operator.IN, values);
  }

  public static <T> FilterOperation<T> like(final T value) {
    return new FilterOperation<>(Operator.LIKE, value);
  }

  public T value() {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.getFirst();
  }

  @Override
  public List<T> values() {
    return values;
  }
}
