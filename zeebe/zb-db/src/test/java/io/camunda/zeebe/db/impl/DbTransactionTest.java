/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.zeebe.db.ColumnFamily;
import io.camunda.zeebe.db.TransactionContext;
import io.camunda.zeebe.db.ZeebeDb;
import io.camunda.zeebe.db.ZeebeDbFactory;
import io.camunda.zeebe.db.ZeebeDbTransaction;
import io.camunda.zeebe.protocol.ColumnFamilyScope;
import io.camunda.zeebe.protocol.EnumValue;
import io.camunda.zeebe.protocol.ScopedColumnFamily;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class DbTransactionTest {

  @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();
  private final ZeebeDbFactory<ColumnFamilies> dbFactory =
      DefaultZeebeDbFactory.getDefaultFactory();

  private TransactionContext transactionContext;

  private ColumnFamily<DbLong, DbLong> oneColumnFamily;
  private ColumnFamily<DbLong, DbLong> twoColumnFamily;
  private ColumnFamily<DbLong, DbLong> threeColumnFamily;

  private DbLong oneKey;
  private DbLong oneValue;
  private DbLong twoValue;
  private DbLong twoKey;
  private DbLong threeKey;
  private DbLong threeValue;

  @Before
  public void setup() throws Exception {
    final File pathName = temporaryFolder.newFolder();
    final ZeebeDb<ColumnFamilies> zeebeDb = dbFactory.createDb(pathName);
    transactionContext = zeebeDb.createContext();

    oneKey = new DbLong();
    oneValue = new DbLong();
    oneColumnFamily =
        zeebeDb.createColumnFamily(ColumnFamilies.ONE, transactionContext, oneKey, oneValue);

    twoKey = new DbLong();
    twoValue = new DbLong();
    twoColumnFamily =
        zeebeDb.createColumnFamily(ColumnFamilies.TWO, transactionContext, twoKey, twoValue);

    threeKey = new DbLong();
    threeValue = new DbLong();
    threeColumnFamily =
        zeebeDb.createColumnFamily(ColumnFamilies.THREE, transactionContext, threeKey, threeValue);
  }

  @Test
  public void shouldUseTransaction() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    // when
    transactionContext.runInTransaction(
        () -> {
          oneColumnFamily.recordEntry(oneKey, oneValue);
          twoColumnFamily.recordEntry(twoKey, twoValue);
          threeColumnFamily.recordEntry(threeKey, threeValue);
        });

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isTrue();
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    assertThat(threeColumnFamily.exists(threeKey)).isTrue();
  }

  @Test
  public void shouldStartNewTransaction() throws Exception {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    final ZeebeDbTransaction transaction = transactionContext.getCurrentTransaction();
    transaction.run(
        () -> {
          oneColumnFamily.recordEntry(oneKey, oneValue);
          twoColumnFamily.recordEntry(twoKey, twoValue);
          threeColumnFamily.recordEntry(threeKey, threeValue);
        });

    // when
    transaction.commit();

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isTrue();
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    assertThat(threeColumnFamily.exists(threeKey)).isTrue();
  }

  @Test
  public void shouldAccessOnOpenTransaction() throws Exception {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    final ZeebeDbTransaction transaction = transactionContext.getCurrentTransaction();
    transaction.run(
        () -> {
          oneColumnFamily.recordEntry(oneKey, oneValue);
          twoColumnFamily.recordEntry(twoKey, twoValue);
          threeColumnFamily.recordEntry(threeKey, threeValue);
        });

    // when
    // no commit

    // then
    // uses the same transaction
    assertThat(oneColumnFamily.exists(oneKey)).isTrue();
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    assertThat(threeColumnFamily.exists(threeKey)).isTrue();
  }

  @Test
  public void shouldNotReopenTransaction() throws Exception {
    // given
    final ZeebeDbTransaction transaction = transactionContext.getCurrentTransaction();

    transaction.run(
        () -> {

          // when
          final ZeebeDbTransaction sameTransaction = transactionContext.getCurrentTransaction();

          // then
          assertThat(transaction).isEqualTo(sameTransaction);
        });
  }

  @Test
  public void shouldNotReopenTransactionWithOperations() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    transactionContext.runInTransaction(
        () -> {

          // when
          final ZeebeDbTransaction sameTransaction = transactionContext.getCurrentTransaction();
          sameTransaction.run(
              () -> {
                oneColumnFamily.recordEntry(oneKey, oneValue);
                twoColumnFamily.recordEntry(twoKey, twoValue);
                threeColumnFamily.recordEntry(threeKey, threeValue);
              });
          sameTransaction.commit();

          // then it is committed but available in this transaction
          assertThat(oneColumnFamily.exists(oneKey)).isTrue();
          oneColumnFamily.deleteExisting(oneKey);

          assertThat(twoColumnFamily.exists(twoKey)).isTrue();
          assertThat(threeColumnFamily.exists(threeKey)).isTrue();
        });

    // then it is committed but available in this transaction
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    assertThat(threeColumnFamily.exists(threeKey)).isTrue();
  }

  @Test
  public void shouldRollbackTransaction() throws Exception {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    final ZeebeDbTransaction transaction = transactionContext.getCurrentTransaction();
    transaction.run(
        () -> {
          oneColumnFamily.recordEntry(oneKey, oneValue);
          twoColumnFamily.recordEntry(twoKey, twoValue);
          threeColumnFamily.recordEntry(threeKey, threeValue);
        });

    // when
    transaction.rollback();

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
    assertThat(twoColumnFamily.exists(twoKey)).isFalse();
    assertThat(threeColumnFamily.exists(threeKey)).isFalse();
  }

  @Test
  public void shouldGetValueInTransaction() {
    // given
    final AtomicLong actualValue = new AtomicLong(0);
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    // when
    transactionContext.runInTransaction(
        () -> {
          oneColumnFamily.recordEntry(oneKey, oneValue);
          final DbLong value = oneColumnFamily.get(oneKey);
          actualValue.set(value.getValue());
        });

    // then
    assertThat(actualValue.get()).isEqualTo(-1);
    assertThat(oneColumnFamily.get(oneKey).getValue()).isEqualTo(-1);
  }

  @Test
  public void shouldFindValueInTransaction() {
    // given
    final Map<Long, Long> actualValues = new HashMap<>();
    oneKey.recordValue(1);
    oneValue.recordValue(-1);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    // when
    transactionContext.runInTransaction(
        () -> {
          // update value
          oneKey.recordValue(1);
          oneValue.recordValue(-2);
          oneColumnFamily.updateEntry(oneKey, oneValue);

          // create new key-value pair
          oneKey.recordValue(2);
          oneValue.recordValue(-3);
          oneColumnFamily.recordEntry(oneKey, oneValue);

          actualValues.put(oneKey.getValue(), oneColumnFamily.get(oneKey).getValue());
          oneKey.recordValue(1);
          actualValues.put(oneKey.getValue(), oneColumnFamily.get(oneKey).getValue());
        });

    // then
    final Map<Long, Long> expectedValues = new HashMap<>();
    expectedValues.put(1L, -2L);
    expectedValues.put(2L, -3L);
    assertThat(actualValues).isEqualTo(expectedValues);
  }

  @Test
  public void shouldIterateAndFindValuesInTransaction() {
    // given
    final Map<Long, Long> actualValues = new HashMap<>();

    oneKey.recordValue(1);
    oneValue.recordValue(-1);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    oneKey.recordValue(2);
    oneValue.recordValue(-2);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    // when
    transactionContext.runInTransaction(
        () -> {
          // update old value
          oneKey.recordValue(2);
          oneValue.recordValue(-5);
          oneColumnFamily.updateEntry(oneKey, oneValue);

          // create new key-value pair
          oneKey.recordValue(3);
          oneValue.recordValue(-3);
          oneColumnFamily.recordEntry(oneKey, oneValue);

          oneColumnFamily.forEach((k, v) -> actualValues.put(k.getValue(), v.getValue()));
        });

    // then
    final Map<Long, Long> expectedValues = new HashMap<>();
    expectedValues.put(1L, -1L);
    expectedValues.put(2L, -5L);
    expectedValues.put(3L, -3L);
    assertThat(actualValues).isEqualTo(expectedValues);
  }

  @Test
  public void shouldIterateAndDeleteInTransaction() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    oneKey.recordValue(2);
    oneValue.recordValue(-2);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    // when
    transactionContext.runInTransaction(
        () -> oneColumnFamily.forEach((k, v) -> oneColumnFamily.deleteExisting(k)));

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
    oneKey.recordValue(2);
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
  }

  @Test
  public void shouldEndInSameTransaction() {
    // given
    final AtomicLong actualValue = new AtomicLong(0);
    oneKey.recordValue(1);
    oneValue.recordValue(-1);
    oneColumnFamily.recordEntry(oneKey, oneValue);

    twoValue.recordValue(192313);

    // when
    transactionContext.runInTransaction(
        () -> {
          transactionContext.runInTransaction(() -> oneColumnFamily.updateEntry(oneKey, twoValue));
          final DbLong value = oneColumnFamily.get(oneKey);
          actualValue.set(value.getValue());
        });

    // then
    assertThat(actualValue.get()).isEqualTo(192313);
    assertThat(oneColumnFamily.get(oneKey).getValue()).isEqualTo(192313);
  }

  @Test
  public void shouldWriteAndDeleteInTransaction() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);
    twoColumnFamily.recordEntry(twoKey, twoValue);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);
    threeColumnFamily.recordEntry(threeKey, threeValue);

    // when
    transactionContext.runInTransaction(
        () -> {
          // create
          oneColumnFamily.recordEntry(oneKey, oneValue);

          // delete
          twoColumnFamily.deleteExisting(twoKey);

          // update
          threeValue.recordValue(Integer.MIN_VALUE);
          threeColumnFamily.updateEntry(threeKey, threeValue);
        });

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isTrue();
    assertThat(oneColumnFamily.get(oneKey).getValue()).isEqualTo(-1);

    assertThat(twoColumnFamily.exists(twoKey)).isFalse();

    assertThat(threeColumnFamily.exists(threeKey)).isTrue();
    assertThat(threeColumnFamily.get(threeKey).getValue()).isEqualTo(Integer.MIN_VALUE);
  }

  @Test
  public void shouldWriteAndDeleteSameKeyValuePairInTransaction() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    // when
    transactionContext.runInTransaction(
        () -> {
          // create
          oneColumnFamily.recordEntry(oneKey, oneValue);

          // delete
          oneColumnFamily.deleteExisting(oneKey);
        });

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
  }

  @Test
  public void shouldNotCommitOnError() {
    // given
    oneKey.recordValue(1);
    oneValue.recordValue(-1);

    twoKey.recordValue(52000);
    twoValue.recordValue(192313);
    twoColumnFamily.recordEntry(twoKey, twoValue);

    threeKey.recordValue(Short.MAX_VALUE);
    threeValue.recordValue(Integer.MAX_VALUE);

    // when
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    try {
      transactionContext.runInTransaction(
          () -> {
            oneColumnFamily.recordEntry(oneKey, oneValue);
            twoColumnFamily.deleteExisting(twoKey);
            threeColumnFamily.recordEntry(threeKey, threeValue);
            throw new RuntimeException();
          });
    } catch (final Exception e) {
      // ignore
    }

    // then
    assertThat(oneColumnFamily.exists(oneKey)).isFalse();
    assertThat(twoColumnFamily.exists(twoKey)).isTrue();
    assertThat(threeColumnFamily.exists(threeKey)).isFalse();
  }

  @Test
  // See https://github.com/camunda/camunda/issues/11681, this test is to ensure that we don't
  // hide exceptions from the `ProcessingStateMachine`.
  public void shouldNotWrapRuntimeExceptions() {
    // given
    final var exception = new RuntimeException("expected");

    // then
    assertThatThrownBy(
            () ->
                transactionContext.runInTransaction(
                    () -> {
                      throw exception;
                    }))
        .isSameAs(exception);
  }

  private enum ColumnFamilies implements EnumValue, ScopedColumnFamily {
    DEFAULT, // rocksDB needs a default column family
    ONE,
    TWO,
    THREE;

    @Override
    public int getValue() {
      return ordinal();
    }

    @Override
    public ColumnFamilyScope partitionScope() {
      return ColumnFamilyScope.PARTITION_LOCAL;
    }
  }
}
