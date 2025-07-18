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
package io.camunda.zeebe.protocol.record.intent;

import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.management.CheckpointIntent;
import io.camunda.zeebe.protocol.record.intent.scaling.ScaleIntent;
import java.util.Arrays;
import java.util.Collection;

public interface Intent {
  Collection<Class<? extends Intent>> INTENT_CLASSES =
      Arrays.asList(
          DeploymentIntent.class,
          EscalationIntent.class,
          IncidentIntent.class,
          JobIntent.class,
          ProcessInstanceIntent.class,
          MessageIntent.class,
          MessageBatchIntent.class,
          MessageSubscriptionIntent.class,
          ProcessMessageSubscriptionIntent.class,
          JobBatchIntent.class,
          TimerIntent.class,
          VariableIntent.class,
          VariableDocumentIntent.class,
          ProcessInstanceCreationIntent.class,
          ErrorIntent.class,
          ProcessIntent.class,
          DeploymentDistributionIntent.class,
          ProcessEventIntent.class,
          DecisionIntent.class,
          DecisionRequirementsIntent.class,
          DecisionEvaluationIntent.class,
          MessageStartEventSubscriptionIntent.class,
          ProcessInstanceResultIntent.class,
          CheckpointIntent.class,
          ProcessInstanceModificationIntent.class,
          SignalIntent.class,
          SignalSubscriptionIntent.class,
          ResourceDeletionIntent.class,
          CommandDistributionIntent.class,
          ProcessInstanceBatchIntent.class,
          FormIntent.class,
          ResourceIntent.class,
          UserTaskIntent.class,
          ProcessInstanceMigrationIntent.class,
          CompensationSubscriptionIntent.class,
          MessageCorrelationIntent.class,
          UserIntent.class,
          ClockIntent.class,
          AuthorizationIntent.class,
          RoleIntent.class,
          TenantIntent.class,
          ScaleIntent.class,
          GroupIntent.class,
          MappingIntent.class,
          IdentitySetupIntent.class,
          BatchOperationIntent.class,
          BatchOperationChunkIntent.class,
          BatchOperationExecutionIntent.class,
          AdHocSubProcessActivityActivationIntent.class);
  short NULL_VAL = 255;
  Intent UNKNOWN = UnknownIntent.UNKNOWN;

  short value();

  String name();

  /**
   * @return true if this intent is used as an event, i.e. it's not a command or command rejection.
   */
  boolean isEvent();

  @SuppressWarnings("checkstyle:MissingSwitchDefault")
  static Intent fromProtocolValue(final ValueType valueType, final short intentValue) {
    switch (valueType) {
      case DEPLOYMENT:
        return DeploymentIntent.from(intentValue);
      case INCIDENT:
        return IncidentIntent.from(intentValue);
      case JOB:
        return JobIntent.from(intentValue);
      case PROCESS_INSTANCE:
        return ProcessInstanceIntent.from(intentValue);
      case MESSAGE:
        return MessageIntent.from(intentValue);
      case MESSAGE_BATCH:
        return MessageBatchIntent.from(intentValue);
      case MESSAGE_SUBSCRIPTION:
        return MessageSubscriptionIntent.from(intentValue);
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return MessageStartEventSubscriptionIntent.from(intentValue);
      case PROCESS_MESSAGE_SUBSCRIPTION:
        return ProcessMessageSubscriptionIntent.from(intentValue);
      case JOB_BATCH:
        return JobBatchIntent.from(intentValue);
      case TIMER:
        return TimerIntent.from(intentValue);
      case VARIABLE:
        return VariableIntent.from(intentValue);
      case VARIABLE_DOCUMENT:
        return VariableDocumentIntent.from(intentValue);
      case PROCESS_INSTANCE_CREATION:
        return ProcessInstanceCreationIntent.from(intentValue);
      case ERROR:
        return ErrorIntent.from(intentValue);
      case PROCESS_INSTANCE_RESULT:
        return ProcessInstanceResultIntent.from(intentValue);
      case PROCESS:
        return ProcessIntent.from(intentValue);
      case DEPLOYMENT_DISTRIBUTION:
        return DeploymentDistributionIntent.from(intentValue);
      case PROCESS_EVENT:
        return ProcessEventIntent.from(intentValue);
      case DECISION:
        return DecisionIntent.from(intentValue);
      case DECISION_REQUIREMENTS:
        return DecisionRequirementsIntent.from(intentValue);
      case DECISION_EVALUATION:
        return DecisionEvaluationIntent.from(intentValue);
      case CHECKPOINT:
        return CheckpointIntent.from(intentValue);
      case ESCALATION:
        return EscalationIntent.from(intentValue);
      case PROCESS_INSTANCE_MODIFICATION:
        return ProcessInstanceModificationIntent.from(intentValue);
      case SIGNAL:
        return SignalIntent.from(intentValue);
      case SIGNAL_SUBSCRIPTION:
        return SignalSubscriptionIntent.from(intentValue);
      case RESOURCE_DELETION:
        return ResourceDeletionIntent.from(intentValue);
      case COMMAND_DISTRIBUTION:
        return CommandDistributionIntent.from(intentValue);
      case PROCESS_INSTANCE_BATCH:
        return ProcessInstanceBatchIntent.from(intentValue);
      case AD_HOC_SUB_PROCESS_ACTIVITY_ACTIVATION:
        return AdHocSubProcessActivityActivationIntent.from(intentValue);
      case FORM:
        return FormIntent.from(intentValue);
      case RESOURCE:
        return ResourceIntent.from(intentValue);
      case USER_TASK:
        return UserTaskIntent.from(intentValue);
      case PROCESS_INSTANCE_MIGRATION:
        return ProcessInstanceMigrationIntent.from(intentValue);
      case COMPENSATION_SUBSCRIPTION:
        return CompensationSubscriptionIntent.from(intentValue);
      case MESSAGE_CORRELATION:
        return MessageCorrelationIntent.from(intentValue);
      case USER:
        return UserIntent.from(intentValue);
      case CLOCK:
        return ClockIntent.from(intentValue);
      case AUTHORIZATION:
        return AuthorizationIntent.from(intentValue);
      case ROLE:
        return RoleIntent.from(intentValue);
      case TENANT:
        return TenantIntent.from(intentValue);
      case SCALE:
        return ScaleIntent.from(intentValue);
      case GROUP:
        return GroupIntent.from(intentValue);
      case MAPPING:
        return MappingIntent.from(intentValue);
      case IDENTITY_SETUP:
        return IdentitySetupIntent.from(intentValue);
      case BATCH_OPERATION_CREATION:
        return BatchOperationIntent.from(intentValue);
      case BATCH_OPERATION_EXECUTION:
        return BatchOperationExecutionIntent.from(intentValue);
      case BATCH_OPERATION_CHUNK:
        return BatchOperationChunkIntent.from(intentValue);
      case BATCH_OPERATION_LIFECYCLE_MANAGEMENT:
        return BatchOperationIntent.from(intentValue);
      case BATCH_OPERATION_PARTITION_LIFECYCLE:
        return BatchOperationIntent.from(intentValue);
      case NULL_VAL:
      case SBE_UNKNOWN:
        return Intent.UNKNOWN;
    }

    throw new RuntimeException(
        String.format(
            "Expected to map value type %s to intent type, but did not recognize the value type",
            valueType.name()));
  }

  static Intent fromProtocolValue(final ValueType valueType, final String intentName) {
    switch (valueType) {
      case DEPLOYMENT:
        return DeploymentIntent.valueOf(intentName);
      case INCIDENT:
        return IncidentIntent.valueOf(intentName);
      case JOB:
        return JobIntent.valueOf(intentName);
      case PROCESS_INSTANCE:
        return ProcessInstanceIntent.valueOf(intentName);
      case MESSAGE:
        return MessageIntent.valueOf(intentName);
      case MESSAGE_BATCH:
        return MessageBatchIntent.valueOf(intentName);
      case MESSAGE_SUBSCRIPTION:
        return MessageSubscriptionIntent.valueOf(intentName);
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return MessageStartEventSubscriptionIntent.valueOf(intentName);
      case PROCESS_MESSAGE_SUBSCRIPTION:
        return ProcessMessageSubscriptionIntent.valueOf(intentName);
      case JOB_BATCH:
        return JobBatchIntent.valueOf(intentName);
      case TIMER:
        return TimerIntent.valueOf(intentName);
      case VARIABLE:
        return VariableIntent.valueOf(intentName);
      case VARIABLE_DOCUMENT:
        return VariableDocumentIntent.valueOf(intentName);
      case PROCESS_INSTANCE_CREATION:
        return ProcessInstanceCreationIntent.valueOf(intentName);
      case ERROR:
        return ErrorIntent.valueOf(intentName);
      case PROCESS_INSTANCE_RESULT:
        return ProcessInstanceResultIntent.valueOf(intentName);
      case PROCESS:
        return ProcessIntent.valueOf(intentName);
      case DEPLOYMENT_DISTRIBUTION:
        return DeploymentDistributionIntent.valueOf(intentName);
      case PROCESS_EVENT:
        return ProcessEventIntent.valueOf(intentName);
      case AD_HOC_SUB_PROCESS_ACTIVITY_ACTIVATION:
        return AdHocSubProcessActivityActivationIntent.valueOf(intentName);
      case DECISION:
        return DecisionIntent.valueOf(intentName);
      case DECISION_REQUIREMENTS:
        return DecisionRequirementsIntent.valueOf(intentName);
      case DECISION_EVALUATION:
        return DecisionEvaluationIntent.valueOf(intentName);
      case CHECKPOINT:
        return CheckpointIntent.valueOf(intentName);
      case ESCALATION:
        return EscalationIntent.valueOf(intentName);
      case SIGNAL:
        return SignalIntent.valueOf(intentName);
      case SIGNAL_SUBSCRIPTION:
        return SignalSubscriptionIntent.valueOf(intentName);
      case RESOURCE_DELETION:
        return ResourceDeletionIntent.valueOf(intentName);
      case FORM:
        return FormIntent.valueOf(intentName);
      case RESOURCE:
        return ResourceIntent.valueOf(intentName);
      case USER_TASK:
        return UserTaskIntent.valueOf(intentName);
      case PROCESS_INSTANCE_MIGRATION:
        return ProcessInstanceMigrationIntent.valueOf(intentName);
      case COMPENSATION_SUBSCRIPTION:
        return CompensationSubscriptionIntent.valueOf(intentName);
      case MESSAGE_CORRELATION:
        return MessageCorrelationIntent.valueOf(intentName);
      case USER:
        return UserIntent.valueOf(intentName);
      case CLOCK:
        return ClockIntent.valueOf(intentName);
      case AUTHORIZATION:
        return AuthorizationIntent.valueOf(intentName);
      case ROLE:
        return RoleIntent.valueOf(intentName);
      case TENANT:
        return TenantIntent.valueOf(intentName);
      case SCALE:
        return ScaleIntent.valueOf(intentName);
      case GROUP:
        return GroupIntent.valueOf(intentName);
      case MAPPING:
        return MappingIntent.valueOf(intentName);
      case IDENTITY_SETUP:
        return IdentitySetupIntent.valueOf(intentName);
      case BATCH_OPERATION_CREATION:
        return BatchOperationIntent.valueOf(intentName);
      case BATCH_OPERATION_EXECUTION:
        return BatchOperationExecutionIntent.valueOf(intentName);
      case BATCH_OPERATION_CHUNK:
        return BatchOperationChunkIntent.valueOf(intentName);
      case BATCH_OPERATION_LIFECYCLE_MANAGEMENT:
        return BatchOperationIntent.valueOf(intentName);
      case BATCH_OPERATION_PARTITION_LIFECYCLE:
        return BatchOperationIntent.valueOf(intentName);
      case NULL_VAL:
      case SBE_UNKNOWN:
        return Intent.UNKNOWN;
      default:
        throw new RuntimeException(
            String.format(
                "Expected to map value type %s to intent type, but did not recognize the value type",
                valueType.name()));
    }
  }

  static int maxCardinality() {
    return INTENT_CLASSES.stream()
        .mapToInt(clazz -> clazz.getEnumConstants().length)
        .max()
        .getAsInt();
  }

  enum UnknownIntent implements Intent {
    UNKNOWN;

    @Override
    public short value() {
      return NULL_VAL;
    }

    @Override
    public boolean isEvent() {
      return false;
    }
  }
}
