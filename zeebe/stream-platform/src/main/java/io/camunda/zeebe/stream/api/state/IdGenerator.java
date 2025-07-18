/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.api.state;

/** Generate unique IDs for any entity in the stream. */
@FunctionalInterface
public interface IdGenerator {

  /**
   * Returns the next key of a record and updates the key generator.
   *
   * @return the next key for a new record
   */
  long nextKey();
}
