/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.db.impl;

import static io.camunda.zeebe.db.impl.ZeebeDbConstants.ZB_DB_BYTE_ORDER;

import io.camunda.zeebe.db.DbKey;
import io.camunda.zeebe.db.DbValue;
import io.camunda.zeebe.util.buffer.BufferUtil;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public final class DbString implements DbKey, DbValue {

  private final DirectBuffer buffer = new UnsafeBuffer(0, 0);

  public void wrapString(final String string) {
    buffer.wrap(string.getBytes());
  }

  public void wrapBuffer(final DirectBuffer buffer) {
    this.buffer.wrap(buffer);
  }

  @Override
  public void wrap(final DirectBuffer directBuffer, int offset, final int length) {
    final int stringLen = directBuffer.getInt(offset, ZB_DB_BYTE_ORDER);
    offset += Integer.BYTES;

    final byte[] b = new byte[stringLen];
    directBuffer.getBytes(offset, b);
    buffer.wrap(b);
  }

  @Override
  public int getLength() {
    return Integer.BYTES // length of the string
        + buffer.capacity();
  }

  @Override
  public void write(final MutableDirectBuffer mutableDirectBuffer, int offset) {
    final int length = buffer.capacity();
    mutableDirectBuffer.putInt(offset, length, ZB_DB_BYTE_ORDER);
    offset += Integer.BYTES;

    mutableDirectBuffer.putBytes(offset, buffer, 0, buffer.capacity());
  }

  @Override
  public String toString() {
    return BufferUtil.bufferAsString(buffer);
  }

  public DirectBuffer getBuffer() {
    return buffer;
  }
}
