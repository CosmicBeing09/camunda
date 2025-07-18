/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.impl;

import io.camunda.zeebe.logstreams.log.LoggedEvent;
import io.camunda.zeebe.protocol.impl.record.RecordRequest;
import io.camunda.zeebe.stream.api.EventFilter;
import io.camunda.zeebe.stream.api.MetadataFilter;

public class RequestEventFilter implements EventFilter {

  protected final RecordRequest metadata = new RecordRequest();
  protected final MetadataFilter metadataFilter;

  public RequestEventFilter(final MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

  @Override
  public boolean applies(final LoggedEvent event) {
    event.readMetadata(metadata);
    return metadataFilter.applies(metadata);
  }
}
