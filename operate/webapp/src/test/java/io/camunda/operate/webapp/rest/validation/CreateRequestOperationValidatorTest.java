/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.operate.webapp.rest.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.camunda.operate.webapp.reader.OperationReader;
import io.camunda.operate.webapp.reader.VariableReader;
import io.camunda.operate.webapp.rest.dto.operation.CreateOperationRequestDto;
import io.camunda.operate.webapp.rest.exception.InvalidRequestException;
import io.camunda.webapps.schema.entities.operation.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateRequestOperationValidatorTest {
  private CreateRequestOperationValidator underTest;

  @Mock private VariableReader mockVariableReader;

  @Mock private OperationReader mockOperationReader;

  @BeforeEach
  public void setup() {
    underTest = new CreateRequestOperationValidator(mockVariableReader, mockOperationReader);
  }

  @Test
  public void testValidateWithNullOperationType() {
    final CreateOperationRequestDto request = new CreateOperationRequestDto(null);

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage()).isEqualTo("Operation type must be defined.");
  }

  @Test
  public void testValidateUpdateVariableWithNullScopeId() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.UPDATE_VARIABLE);

    request.setVariableScopeId(null);
    request.setVariableName("var");
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateUpdateVariableWithNullVariableName() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.UPDATE_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName(null);
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateUpdateVariableWithEmptyVariableName() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.UPDATE_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("");
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateUpdateVariableWithNullVariableValue() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.UPDATE_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("var");
    request.setVariableValue(null);

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateUpdateVariable() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.UPDATE_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("var");
    request.setVariableValue("val");

    assertDoesNotThrow(() -> underTest.validate(request, "123"));
  }

  @Test
  public void testValidateAddVariableWithNullScopeId() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.ADD_VARIABLE);

    request.setVariableScopeId(null);
    request.setVariableName("var");
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateAddVariableWithNullVariableName() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.ADD_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName(null);
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateAddVariableWithEmptyVariableName() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.ADD_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("");
    request.setVariableValue("val");

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateAddVariableWithNullVariableValue() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.ADD_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("var");
    request.setVariableValue(null);

    final InvalidRequestException exception =
        assertThrows(
            InvalidRequestException.class, () -> underTest.validate(request, "123"));

    assertThat(exception.getMessage())
        .isEqualTo("ScopeId, name and value must be defined for UPDATE_VARIABLE operation.");
  }

  @Test
  public void testValidateAddVariable() {
    final CreateOperationRequestDto request =
        new CreateOperationRequestDto(OperationType.ADD_VARIABLE);

    request.setVariableScopeId("abc");
    request.setVariableName("var");
    request.setVariableValue("val");

    assertDoesNotThrow(() -> underTest.validate(request, "123"));
  }
}
