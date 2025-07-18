/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.optimize.dto.zeebe.definition;

import static io.camunda.optimize.service.util.importing.ZeebeConstants.ZEEBE_DEFAULT_TENANT_ID;

import io.camunda.zeebe.protocol.record.value.deployment.ProcessMetadataValue;
import org.apache.commons.lang3.StringUtils;

public class ZeebeProcessDefinitionDataDto implements ProcessMetadataValue {

  private byte[] resource;
  private long processDefinitionKey;
  private int version;
  private byte[] checksum;
  private String resourceName;
  private String bpmnProcessId;
  private String tenantId;
  private String versionTag;

  public ZeebeProcessDefinitionDataDto() {}

  @Override
  public String toJson() {
    throw new UnsupportedOperationException("Operation not supported");
  }

  @Override
  public String getTenantIdentifier() {
    return StringUtils.isEmpty(tenantId) ? ZEEBE_DEFAULT_TENANT_ID : tenantId;
  }

  public byte[] getResource() {
    return resource;
  }

  public void setResource(final byte[] resource) {
    this.resource = resource;
  }

  @Override
  public String getBpmnProcessId() {
    return bpmnProcessId;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public String getVersionTag() {
    return versionTag;
  }

  @Override
  public long getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  @Override
  public String getResourceName() {
    return resourceName;
  }

  @Override
  public byte[] getChecksum() {
    return checksum;
  }

  @Override
  public boolean isDuplicate() {
    // Process Records should never be duplicate in Zeebe
    return false;
  }

  @Override
  public long getDeploymentKey() {
    throw new UnsupportedOperationException("Operation not supported");
  }

  public void setChecksum(final byte[] checksum) {
    this.checksum = checksum;
  }

  public void setResourceName(final String resourceName) {
    this.resourceName = resourceName;
  }

  public void setProcessDefinitionKey(final long processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void setVersionTag(final String versionTag) {
    this.versionTag = versionTag;
  }

  public void setVersion(final int version) {
    this.version = version;
  }

  public void setBpmnProcessId(final String bpmnProcessId) {
    this.bpmnProcessId = bpmnProcessId;
  }

  public void setTenantId(final String tenantId) {
    this.tenantId = tenantId;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ZeebeProcessDefinitionDataDto;
  }

  @Override
  public int hashCode() {
    return org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(final Object o) {
    return org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public String toString() {
    return "ZeebeProcessDefinitionDataDto(resource="
        + java.util.Arrays.toString(getResource())
        + ", processDefinitionKey="
        + getProcessDefinitionKey()
        + ", version="
        + getVersion()
        + ", checksum="
        + java.util.Arrays.toString(getChecksum())
        + ", resourceName="
        + getResourceName()
        + ", bpmnProcessId="
        + getBpmnProcessId()
        + ", tenantId="
        + getTenantIdentifier()
        + ", versionTag="
        + getVersionTag()
        + ")";
  }
}
