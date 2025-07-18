/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.search.sort;

import java.util.ArrayList;
import java.util.List;

public interface SortOption {

  public List<FieldSorting> getFieldSortings();

  public abstract static class AbstractBuilder<T> {

    protected final List<FieldSorting> fieldSortings = new ArrayList<>();
    protected FieldSorting currentFieldSorting;

    protected abstract T self();

    protected T addOrdering(final SortOrder value) {
      if (currentFieldSorting != null) {
        final var field = currentFieldSorting.field();
        final var newOrdering = new FieldSorting(field, value);
        fieldSortings.add(newOrdering);
        currentFieldSorting = null;
      }
      // else if not set, then noop

      return self();
    }

    public T asc() {
      return addOrdering(SortOrder.ASC);
    }

    public T desc() {
      return addOrdering(SortOrder.DESC);
    }
  }

  public final record FieldSorting(String field, SortOrder order) {}
}
