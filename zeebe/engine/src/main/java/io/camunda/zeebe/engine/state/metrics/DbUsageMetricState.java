/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state.metrics;

import io.camunda.zeebe.db.ColumnFamily;
import io.camunda.zeebe.db.TransactionContext;
import io.camunda.zeebe.db.ZeebeDb;
import io.camunda.zeebe.db.impl.DbCompositeKey;
import io.camunda.zeebe.db.impl.DbLong;
import io.camunda.zeebe.db.impl.DbString;
import io.camunda.zeebe.engine.state.mutable.MutableUsageMetricState;
import io.camunda.zeebe.protocol.ZbColumnFamilies;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbUsageMetricState implements MutableUsageMetricState {

  private final ColumnFamily<DbCompositeKey<DbLong, DbLong>, DbString> usageMetricsCf;
  private final DbCompositeKey<DbLong, DbLong> timeInstanceKey;
  private final DbLong eventTimeKey;
  private final DbLong piKey;
  private final DbString tenantIdVal;

  public DbUsageMetricState(
      final ZeebeDb<ZbColumnFamilies> zeebeDb, final TransactionContext transactionContext) {

    eventTimeKey = new DbLong();
    piKey = new DbLong();
    tenantIdVal = new DbString();
    timeInstanceKey = new DbCompositeKey<>(eventTimeKey, piKey);

    usageMetricsCf =
        zeebeDb.createColumnFamily(
            ZbColumnFamilies.RPI_USAGE_METRICS, transactionContext, timeInstanceKey, tenantIdVal);
  }

  @Override
  public Map<String, List<Long>> getBuckets(final long eventTime) {
    final var tenantIdPIsMap = new HashMap<String, List<Long>>();
    eventTimeKey.setValue(eventTime);
    usageMetricsCf.whileEqualPrefix(
        eventTimeKey,
        (eventTimePiKey, tenantIdVal) -> {
          tenantIdPIsMap
              .computeIfAbsent(tenantIdVal.toString(), ignored -> new ArrayList<>())
              .add(eventTimePiKey.second().getValue());
        });
    return tenantIdPIsMap;
  }

  @Override
  public void createRPIMetric(
      final long eventTime, final long processInstanceKey, final String tenantId) {
    eventTimeKey.setValue(eventTime);
    piKey.setValue(processInstanceKey);
    tenantIdVal.wrapString(tenantId);
    usageMetricsCf.insert(timeInstanceKey, tenantIdVal);
  }

  @Override
  public void deleteByEventTime(final long eventTime) {
    eventTimeKey.setValue(eventTime);
    usageMetricsCf.whileEqualPrefix(
        eventTimeKey,
        (eventTimePiKey, tenantIdVal) -> {
          usageMetricsCf.deleteExisting(eventTimePiKey);
        });
  }
}
