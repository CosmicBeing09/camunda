/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.stream.impl;

import static io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent.ACTIVATE_ELEMENT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import io.camunda.zeebe.db.ColumnFamily;
import io.camunda.zeebe.db.DbKey;
import io.camunda.zeebe.db.DbValue;
import io.camunda.zeebe.db.GenericDb;
import io.camunda.zeebe.db.TransactionContext;
import io.camunda.zeebe.db.TransactionOperation;
import io.camunda.zeebe.db.ZeebeDbFactory;
import io.camunda.zeebe.db.ZeebeDbTransaction;
import io.camunda.zeebe.protocol.ColumnFamilies;
import io.camunda.zeebe.stream.util.DefaultZeebeDbFactory;
import io.camunda.zeebe.stream.util.RecordToWrite;
import io.camunda.zeebe.stream.util.Records;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.File;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

final class StreamProcessorTransactionErrorTest {

  @RegisterExtension
  private final StreamPlatformExtension streamPlatformExtension =
      new StreamPlatformExtension(
          new ErrorProneDbFactory(new RuntimeException("Unexpected exception")));

  @SuppressWarnings("unused") // injected by the extension
  private StreamPlatform streamPlatform;

  private StreamProcessor streamProcessor;

  @Test
  void shouldShutDownStreamProcessorOnUncommittedStateException() {
    // given
    streamProcessor = streamPlatform.startStreamProcessorNotAwaitOpening();

    // when -- processing something to trigger a commit
    streamPlatform.writeBatch(
        RecordToWrite.command().processInstance(ACTIVATE_ELEMENT, Records.processInstance(1)));

    // then
    Awaitility.await("wait to become unhealthy")
        .until(() -> streamProcessor.getHealthReport().isUnhealthy());

    Assertions.assertThat(streamProcessor.isFailed()).isTrue();
  }

  private static final class ErrorProneTransactionConext implements TransactionContext {
    final TransactionContext delegate;
    final Exception commitException;

    public ErrorProneTransactionConext(
        final TransactionContext delegate, final Exception commitException) {
      this.delegate = delegate;
      this.commitException = commitException;
    }

    @Override
    public void runInTransaction(final TransactionOperation operations) {
      delegate.runInTransaction(operations);
    }

    @Override
    public ZeebeDbTransaction getCurrentTransaction() {
      // we need to use a spy here, as the transaction class and column families depend on
      // internal implementations (and casting) which doesn't work to override here
      final ZeebeDbTransaction spy = spy(delegate.getCurrentTransaction());
      try {
        doThrow(commitException).when(spy).commit();
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
      return spy;
    }
  }

  private static final class ErrorProneDbFactory implements ZeebeDbFactory<ColumnFamilies> {

    final ZeebeDbFactory<ColumnFamilies> delegate = DefaultZeebeDbFactory.defaultFactory();

    final Exception commitException;

    private ErrorProneDbFactory(final Exception commitException) {
      this.commitException = commitException;
    }

    @Override
    public GenericDb<ColumnFamilies> createDb(final File pathName) {
      return new ErrorProneZeebeDb(delegate.createDb(pathName), commitException);
    }

    @Override
    public GenericDb<ColumnFamilies> openSnapshotOnlyDb(final File path) {
      return delegate.openSnapshotOnlyDb(path);
    }
  }

  private static final class ErrorProneZeebeDb implements GenericDb<ColumnFamilies> {

    private final GenericDb<ColumnFamilies> delegate;
    private final Exception commitException;

    private ErrorProneZeebeDb(
        final GenericDb<ColumnFamilies> delegate, final Exception commitException) {
      this.delegate = delegate;
      this.commitException = commitException;
    }

    @Override
    public <KeyType extends DbKey, ValueType extends DbValue>
        ColumnFamily<KeyType, ValueType> createColumnFamily(
            final ColumnFamilies columnFamily,
            final TransactionContext context,
            final KeyType keyInstance,
            final ValueType valueInstance) {
      return delegate.createColumnFamily(columnFamily, context, keyInstance, valueInstance);
    }

    @Override
    public void createSnapshot(final File snapshotDir) {
      delegate.createSnapshot(snapshotDir);
    }

    @Override
    public Optional<String> getProperty(final String propertyName) {
      return delegate.getProperty(propertyName);
    }

    @Override
    public TransactionContext createContext() {
      return new ErrorProneTransactionConext(delegate.createContext(), commitException);
    }

    @Override
    public boolean isEmpty(final ColumnFamilies column, final TransactionContext context) {
      return delegate.isEmpty(column, context);
    }

    @Override
    public MeterRegistry getMeterRegistry() {
      return delegate.getMeterRegistry();
    }

    @Override
    public void exportMetrics() {
      delegate.exportMetrics();
    }

    @Override
    public void close() throws Exception {
      delegate.close();
    }
  }
}
