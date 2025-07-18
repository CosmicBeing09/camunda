/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.protocol.impl.record.value.message;

import static io.camunda.zeebe.util.buffer.BufferUtil.bufferAsString;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.camunda.zeebe.msgpack.property.DocumentProperty;
import io.camunda.zeebe.msgpack.property.LongProperty;
import io.camunda.zeebe.msgpack.property.StringProperty;
import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.camunda.zeebe.protocol.record.value.MessageRecordValue;
import io.camunda.zeebe.protocol.record.value.TenantOwned;
import java.util.Map;
import org.agrona.DirectBuffer;

public final class MessageRecord extends UnifiedRecordValue implements MessageRecordValue {

  private final StringProperty nameProp = new StringProperty("name");
  private final StringProperty correlationKeyProp = new StringProperty("correlationKey");
  // TTL in milliseconds
  private final LongProperty timeToLiveProp = new LongProperty("timeToLive");
  private final LongProperty deadlineProp = new LongProperty("deadline", -1);

  private final DocumentProperty variablesProp = new DocumentProperty("variables");
  private final StringProperty messageIdProp = new StringProperty("messageId", "");
  private final StringProperty tenantIdProp =
      new StringProperty("tenantId", TenantOwned.DEFAULT_TENANT_IDENTIFIER);

  public MessageRecord() {
    super(7);
    declareProperty(nameProp)
        .declareProperty(correlationKeyProp)
        .declareProperty(timeToLiveProp)
        .declareProperty(variablesProp)
        .declareProperty(messageIdProp)
        .declareProperty(deadlineProp)
        .declareProperty(tenantIdProp);
  }

  public void wrap(final MessageRecord record) {
    setName(record.getNameBuffer());
    setCorrelationKey(record.getCorrelationKeyBuffer());
    setTimeToLive(record.getTimeToLive());
    setDeadline(record.getDeadline());
    setVariables(record.getVariablesBuffer());
    setMessageId(record.getMessageIdBuffer());
    setTenantId(record.getTenantId());
  }

  public boolean hasMessageId() {
    return messageIdProp.getBuffer().capacity() > 0;
  }

  @JsonIgnore
  public DirectBuffer getCorrelationKeyBuffer() {
    return correlationKeyProp.getBuffer();
  }

  @JsonIgnore
  public DirectBuffer getMessageIdBuffer() {
    return messageIdProp.getBuffer();
  }

  @Override
  public String getName() {
    return bufferAsString(nameProp.getBuffer());
  }

  @Override
  public String getCorrelationKey() {
    return bufferAsString(correlationKeyProp.getBuffer());
  }

  @Override
  public String getMessageId() {
    return bufferAsString(messageIdProp.getBuffer());
  }

  @Override
  public long getTimeToLive() {
    return timeToLiveProp.getValue();
  }

  public MessageRecord setTimeToLive(final long timeToLive) {
    timeToLiveProp.setValue(timeToLive);
    return this;
  }

  @Override
  public long getDeadline() {
    return deadlineProp.getValue();
  }

  public MessageRecord setDeadline(final long deadline) {
    deadlineProp.setValue(deadline);
    return this;
  }

  public MessageRecord setMessageId(final String messageId) {
    messageIdProp.setValue(messageId);
    return this;
  }

  public MessageRecord setMessageId(final DirectBuffer messageId) {
    messageIdProp.setValue(messageId);
    return this;
  }

  public MessageRecord setCorrelationKey(final String correlationKey) {
    correlationKeyProp.setValue(correlationKey);
    return this;
  }

  public MessageRecord setCorrelationKey(final DirectBuffer correlationKey) {
    correlationKeyProp.setValue(correlationKey);
    return this;
  }

  public MessageRecord setName(final String name) {
    nameProp.setValue(name);
    return this;
  }

  public MessageRecord setName(final DirectBuffer name) {
    nameProp.setValue(name);
    return this;
  }

  @JsonIgnore
  public DirectBuffer getNameBuffer() {
    return nameProp.getBuffer();
  }

  @Override
  public Map<String, Object> getVariables() {
    return MsgPackConverter.convertToMap(variablesProp.getValue());
  }

  public MessageRecord setVariables(final DirectBuffer variables) {
    variablesProp.setValue(variables);
    return this;
  }

  @JsonIgnore
  public DirectBuffer getVariablesBuffer() {
    return variablesProp.getValue();
  }

  @Override
  public String getTenantId() {
    return bufferAsString(tenantIdProp.getBuffer());
  }

  public MessageRecord setTenantId(final String tenantId) {
    tenantIdProp.setValue(tenantId);
    return this;
  }
}
