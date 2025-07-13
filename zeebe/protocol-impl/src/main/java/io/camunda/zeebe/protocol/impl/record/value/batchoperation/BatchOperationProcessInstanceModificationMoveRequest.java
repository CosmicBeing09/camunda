/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.protocol.impl.record.value.batchoperation;

import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.msgpack.value.ObjectValue;
import io.camunda.zeebe.protocol.record.value.BatchOperationCreationRecordValue.ProcessInstanceModificationMoveRequestValue;
import io.camunda.zeebe.util.buffer.BufferUtil;

public final class BatchOperationProcessInstanceModificationMoveRequest extends ObjectValue
    implements ProcessInstanceModificationMoveRequestValue {

  private final StringProperty sourceElementIdProperty = new StringProperty("sourceElementId", "");
  private final StringProperty targetElementIdProperty = new StringProperty("targetElementId", "");

  public BatchOperationProcessInstanceModificationMoveRequest() {
    super(2);
    declareProperty(sourceElementIdProperty).declareProperty(targetElementIdProperty);
  }

  @Override
  public String getSourceElementId() {
    return BufferUtil.bufferAsString(sourceElementIdProperty.getValue());
  }

  public BatchOperationProcessInstanceModificationMoveRequest setSourceElementId(
      final String sourceElementId) {
    sourceElementIdProperty.setValue(sourceElementId);
    return this;
  }

  @Override
  public String getTargetElementId() {
    return BufferUtil.bufferAsString(targetElementIdProperty.getValue());
  }

  public BatchOperationProcessInstanceModificationMoveRequest setTargetElementId(
      final String targetElementId) {
    targetElementIdProperty.setValue(targetElementId);
    return this;
  }

  public BatchOperationProcessInstanceModificationMoveRequest copy(
      final BatchOperationProcessInstanceModificationMoveRequest other) {
    sourceElementIdProperty.setValue(other.getSourceElementId());
    targetElementIdProperty.setValue(other.getTargetElementId());

    return this;
  }
}
