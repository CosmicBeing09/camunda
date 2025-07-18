/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.impl.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.zeebe.logstreams.log.LoggedEvent;
import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.impl.record.RecordRequest;
import io.camunda.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.stream.api.records.TypedRecord;
import io.camunda.zeebe.util.StringUtil;
import java.util.Map;

public final class TypedRecordImpl implements TypedRecord {
  private final int partitionId;
  private LoggedEvent rawEvent;
  private RecordRequest request;
  private UnifiedRecordValue value;

  public TypedRecordImpl(final int partitionId) {
    this.partitionId = partitionId;
  }

  public void wrap(
      final LoggedEvent rawEvent, final RecordRequest request, final UnifiedRecordValue value) {
    this.rawEvent = rawEvent;
    this.request = request;
    this.value = value;
  }

  @JsonIgnore
  public RecordRequest getMetadata() {
    return request;
  }

  @Override
  public long getPosition() {
    return rawEvent.getPosition();
  }

  @Override
  public long getSourceRecordPosition() {
    return rawEvent.getSourceEventPosition();
  }

  @Override
  public long getTimestamp() {
    return rawEvent.getTimestamp();
  }

  @Override
  public Intent getIntent() {
    return request.getIntent();
  }

  @Override
  public int getPartitionId() {
    return partitionId;
  }

  @Override
  public RecordType getRecordType() {
    return request.getRecordType();
  }

  @Override
  public RejectionType getRejectionType() {
    return request.getRejectionType();
  }

  @Override
  public String getRejectionReason() {
    return request.getRejectionReason();
  }

  @Override
  public String getBrokerVersion() {
    return request.getBrokerVersion().toString();
  }

  @Override
  public Map<String, Object> getAuthorizations() {
    return request.getAuthorization().toDecodedMap();
  }

  @Override
  public int getRecordVersion() {
    return request.getRecordVersion();
  }

  @Override
  public ValueType getValueType() {
    return request.getValueType();
  }

  @Override
  public long getOperationReference() {
    return request.getOperationReference();
  }

  @Override
  public Record copyOf() {
    return CopiedRecords.createCopiedRecord(getPartitionId(), rawEvent);
  }

  @Override
  public long getKey() {
    return rawEvent.getKey();
  }

  @Override
  public UnifiedRecordValue getValue() {
    return value;
  }

  @Override
  @JsonIgnore
  public int getRequestStreamId() {
    return request.getRequestStreamId();
  }

  @Override
  @JsonIgnore
  public long getRequestId() {
    return request.getRequestId();
  }

  @Override
  @JsonIgnore
  public int getLength() {
    return request.getLength() + value.getLength();
  }

  @Override
  public String toJson() {
    return MsgPackConverter.convertJsonSerializableObjectToJson(this);
  }

  @Override
  public String toString() {
    return "TypedRecordImpl{"
        + "metadata="
        + request
        + ", value="
        + StringUtil.limitString(value.toString(), 1024)
        + '}';
  }
}
