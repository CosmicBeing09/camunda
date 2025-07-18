/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.impl.state;

import io.camunda.zeebe.db.GenericDb;
import io.camunda.zeebe.db.TransactionContext;

public class StreamProcessorDbState {

  private final DbLastProcessedPositionState lastProcessedPositionState;

  public StreamProcessorDbState(
      final GenericDb zeebeDb, final TransactionContext transactionContext) {
    lastProcessedPositionState = new DbLastProcessedPositionState(zeebeDb, transactionContext);
  }

  public DbLastProcessedPositionState getLastProcessedPositionState() {
    return lastProcessedPositionState;
  }
}
