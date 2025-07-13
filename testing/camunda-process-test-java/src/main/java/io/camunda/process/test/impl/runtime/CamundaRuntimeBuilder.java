/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.process.test.impl.runtime;

import io.camunda.process.test.impl.containers.ContainerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamundaRuntimeBuilder {

  private ContainerFactory containerFactory = new ContainerFactory();

  private String camundaDockerImageName = ContainerRuntimeDefaults.CAMUNDA_DOCKER_IMAGE_NAME;
  private String camundaDockerImageVersion = ContainerRuntimeDefaults.CAMUNDA_DOCKER_IMAGE_VERSION;

  private String elasticsearchDockerImageName =
      ContainerRuntimeDefaults.ELASTICSEARCH_DOCKER_IMAGE_NAME;
  private String elasticsearchDockerImageVersion =
      ContainerRuntimeDefaults.ELASTICSEARCH_DOCKER_IMAGE_VERSION;

  private String connectorsDockerImageName = ContainerRuntimeDefaults.CONNECTORS_DOCKER_IMAGE_NAME;
  private String connectorsDockerImageVersion =
      ContainerRuntimeDefaults.CONNECTORS_DOCKER_IMAGE_VERSION;

  private final Map<String, String> camundaEnvVars = new HashMap<>();
  private final Map<String, String> elasticsearchEnvVars = new HashMap<>();
  private final Map<String, String> connectorsEnvVars = new HashMap<>();

  private final List<Integer> camundaExposedPorts = new ArrayList<>();
  private final List<Integer> elasticsearchExposedPorts = new ArrayList<>();
  private final List<Integer> connectorsExposedPorts = new ArrayList<>();

  private String camundaLoggerName = ContainerRuntimeDefaults.CAMUNDA_LOGGER_NAME;
  private String elasticsearchLoggerName = ContainerRuntimeDefaults.ELASTICSEARCH_LOGGER_NAME;
  private String connectorsLoggerName = ContainerRuntimeDefaults.CONNECTORS_LOGGER_NAME;

  private boolean connectorsEnabled = false;
  private final Map<String, String> connectorsSecrets = new HashMap<>();

  // ============ For testing =================

  CamundaRuntimeBuilder withContainerFactory(final ContainerFactory containerFactory) {
    this.containerFactory = containerFactory;
    return this;
  }

  // ============ Configuration options =================

  public CamundaRuntimeBuilder withCamundaDockerImageName(final String dockerImageName) {
    camundaDockerImageName = dockerImageName;
    return this;
  }

  public CamundaRuntimeBuilder withCamundaDockerImageVersion(
      final String dockerImageVersion) {
    camundaDockerImageVersion = dockerImageVersion;
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchDockerImageName(
      final String dockerImageName) {
    elasticsearchDockerImageName = dockerImageName;
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchDockerImageVersion(
      final String dockerImageVersion) {
    elasticsearchDockerImageVersion = dockerImageVersion;
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsDockerImageName(
      final String dockerImageName) {
    connectorsDockerImageName = dockerImageName;
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsDockerImageVersion(
      final String dockerImageVersion) {
    connectorsDockerImageVersion = dockerImageVersion;
    return this;
  }

  public CamundaRuntimeBuilder withCamundaEnv(final Map<String, String> envVars) {
    camundaEnvVars.putAll(envVars);
    return this;
  }

  public CamundaRuntimeBuilder withCamundaEnv(final String name, final String value) {
    camundaEnvVars.put(name, value);
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchEnv(final Map<String, String> envVars) {
    elasticsearchEnvVars.putAll(envVars);
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchEnv(
      final String name, final String value) {
    elasticsearchEnvVars.put(name, value);
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsEnv(final Map<String, String> envVars) {
    connectorsEnvVars.putAll(envVars);
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsEnv(final String name, final String value) {
    connectorsEnvVars.put(name, value);
    return this;
  }

  public CamundaRuntimeBuilder withCamundaExposedPort(final int port) {
    camundaExposedPorts.add(port);
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchExposedPort(final int port) {
    elasticsearchExposedPorts.add(port);
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsExposedPort(final int port) {
    connectorsExposedPorts.add(port);
    return this;
  }

  public CamundaRuntimeBuilder withCamundaLogger(final String loggerName) {
    camundaLoggerName = loggerName;
    return this;
  }

  public CamundaRuntimeBuilder withElasticsearchLogger(final String loggerName) {
    elasticsearchLoggerName = loggerName;
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsLogger(final String loggerName) {
    connectorsLoggerName = loggerName;
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsEnabled(final boolean enabled) {
    connectorsEnabled = enabled;
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsSecret(
      final String name, final String value) {
    connectorsSecrets.put(name, value);
    return this;
  }

  public CamundaRuntimeBuilder withConnectorsSecrets(final Map<String, String> secrets) {
    connectorsSecrets.putAll(secrets);
    return this;
  }

  // ============ Build =================

  public CamundaRuntime build() {
    return new CamundaRuntime(this, containerFactory);
  }

  // ============ Getters =================

  public String getCamundaDockerImageName() {
    return camundaDockerImageName;
  }

  public String getCamundaDockerImageVersion() {
    return camundaDockerImageVersion;
  }

  public String getElasticsearchDockerImageName() {
    return elasticsearchDockerImageName;
  }

  public String getElasticsearchDockerImageVersion() {
    return elasticsearchDockerImageVersion;
  }

  public String getConnectorsDockerImageName() {
    return connectorsDockerImageName;
  }

  public String getConnectorsDockerImageVersion() {
    return connectorsDockerImageVersion;
  }

  public Map<String, String> getCamundaEnvVars() {
    return camundaEnvVars;
  }

  public Map<String, String> getElasticsearchEnvVars() {
    return elasticsearchEnvVars;
  }

  public Map<String, String> getConnectorsEnvVars() {
    return connectorsEnvVars;
  }

  public List<Integer> getCamundaExposedPorts() {
    return camundaExposedPorts;
  }

  public List<Integer> getElasticsearchExposedPorts() {
    return elasticsearchExposedPorts;
  }

  public List<Integer> getConnectorsExposedPorts() {
    return connectorsExposedPorts;
  }

  public String getCamundaLoggerName() {
    return camundaLoggerName;
  }

  public String getElasticsearchLoggerName() {
    return elasticsearchLoggerName;
  }

  public String getConnectorsLoggerName() {
    return connectorsLoggerName;
  }

  public boolean isConnectorsEnabled() {
    return connectorsEnabled;
  }

  public Map<String, String> getConnectorsSecrets() {
    return connectorsSecrets;
  }
}
