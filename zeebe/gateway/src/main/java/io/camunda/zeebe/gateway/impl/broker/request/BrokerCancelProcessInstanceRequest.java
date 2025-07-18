/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.gateway.impl.broker.request;

import io.camunda.zeebe.broker.client.api.dto.BrokerExecuteCommand;
import io.camunda.zeebe.protocol.impl.record.value.processinstance.WorkflowInstanceRecord;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import org.agrona.DirectBuffer;

public class BrokerCancelProcessInstanceRequest
    extends BrokerExecuteCommand<WorkflowInstanceRecord> {

  private final WorkflowInstanceRecord requestDto = new WorkflowInstanceRecord();

  public BrokerCancelProcessInstanceRequest() {
    super(ValueType.PROCESS_INSTANCE, ProcessInstanceIntent.CANCEL);
  }

  public BrokerCancelProcessInstanceRequest setProcessInstanceKey(final long processInstanceKey) {
    request.setKey(processInstanceKey);
    return this;
  }

  @Override
  public WorkflowInstanceRecord getRequestWriter() {
    return requestDto;
  }

  @Override
  protected WorkflowInstanceRecord toResponseDto(final DirectBuffer buffer) {
    final WorkflowInstanceRecord responseDto = new WorkflowInstanceRecord();
    responseDto.wrap(buffer);
    return responseDto;
  }
}
