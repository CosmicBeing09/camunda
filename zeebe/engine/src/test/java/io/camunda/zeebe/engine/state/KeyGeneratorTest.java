/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.db.ZeebeDb;
import io.camunda.zeebe.engine.util.ProcessingStateRule;
import io.camunda.zeebe.protocol.Protocol;
import io.camunda.zeebe.protocol.ZbColumnFamilies;
import io.camunda.zeebe.stream.api.state.VariableDocKeyGenerator;
import io.camunda.zeebe.stream.impl.state.DbKeyGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public final class KeyGeneratorTest {

  @Rule
  public final ProcessingStateRule stateRule = new ProcessingStateRule();

  private VariableDocKeyGenerator keyGenerator;

  @Before
  public void setUp() throws Exception {
    keyGenerator = stateRule.getProcessingState().getKeyGenerator();
  }

  @Test
  public void shouldGetFirstValue() {
    // given

    // when
    final long firstKey = keyGenerator.nextVariableDocKey();

    // then
    assertThat(firstKey).isEqualTo(Protocol.encodePartitionId(Protocol.DEPLOYMENT_PARTITION, 1));
  }

  @Test
  public void shouldGetNextValue() {
    // given
    final long key = keyGenerator.nextVariableDocKey();

    // when
    final long nextKey = keyGenerator.nextVariableDocKey();

    // then
    assertThat(nextKey).isGreaterThan(key);
  }

  @Test
  public void shouldGetUniqueValuesOverPartitions() throws Exception {
    // given
    final ZeebeDb<ZbColumnFamilies> newDb = stateRule.createNewDb();
    final int secondPartitionId = Protocol.DEPLOYMENT_PARTITION + 1;
    final VariableDocKeyGenerator keyGenerator2 =
        new DbKeyGenerator(secondPartitionId, newDb, newDb.createContext());

    final long keyOfFirstPartition = keyGenerator.nextVariableDocKey();

    // when
    final long keyOfSecondPartition = keyGenerator2.nextVariableDocKey();

    // then
    assertThat(keyOfFirstPartition).isNotEqualTo(keyOfSecondPartition);

    assertThat(Protocol.decodePartitionId(keyOfFirstPartition))
        .isEqualTo(Protocol.DEPLOYMENT_PARTITION);
    assertThat(Protocol.decodePartitionId(keyOfSecondPartition)).isEqualTo(secondPartitionId);

    newDb.close();
  }
}
