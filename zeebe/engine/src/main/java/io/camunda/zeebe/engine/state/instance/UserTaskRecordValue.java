/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.instance;

import io.camunda.zeebe.db.DbValue;
import io.camunda.zeebe.msgpack.UnpackedObject;
import io.camunda.zeebe.msgpack.property.ObjectProperty;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskEntity;

public class UserTaskRecordValue extends UnpackedObject implements DbValue {

  private final ObjectProperty<UserTaskEntity> recordProp =
      new ObjectProperty<>("userTaskRecord", new UserTaskEntity());

  public UserTaskRecordValue() {
    super(1);
    declareProperty(recordProp);
  }

  public UserTaskEntity getRecord() {
    return recordProp.getValue();
  }

  public void setRecord(final UserTaskEntity record) {
    recordProp.getValue().wrap(record);
  }

  public void setRecordWithoutVariables(final UserTaskEntity record) {
    recordProp.getValue().wrapWithoutVariables(record);
  }
}
