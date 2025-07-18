/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.bpmn.activity.listeners.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.camunda.zeebe.engine.util.EngineRule;
import io.camunda.zeebe.model.bpmn.builder.UserTaskBuilder;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskListenerEventType;
import io.camunda.zeebe.protocol.impl.record.value.job.JobResult;
import io.camunda.zeebe.protocol.impl.record.value.job.JobResultCorrections;
import io.camunda.zeebe.protocol.impl.record.value.usertask.UserTaskRecord;
import io.camunda.zeebe.protocol.record.Assertions;
import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.TaskIntent;
import io.camunda.zeebe.protocol.record.intent.VariableDocumentIntent;
import io.camunda.zeebe.protocol.record.value.JobKind;
import io.camunda.zeebe.protocol.record.value.JobListenerEventType;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.UserTaskRecordValue;
import io.camunda.zeebe.test.util.record.RecordingExporter;
import io.camunda.zeebe.test.util.record.RecordingExporterTestWatcher;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/** Tests verifying that a task listener may deny the event. */
public class TaskListenerDenialsTest {
  @ClassRule public static final EngineRule ENGINE = EngineRule.singlePartition();
  @Rule public final TestWatcher watcher = new RecordingExporterTestWatcher();
  private final TaskListenerTestHelper helper = new TaskListenerTestHelper(ENGINE);

  private String listenerType;

  @Before
  public void setup() {
    listenerType = "my_listener_" + UUID.randomUUID();
  }

  @Test
  public void shouldRejectUserTaskAssignmentWhenTaskListenerDeniesTheTransition() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithAssigningTaskListeners(listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("new_assignee").assign();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // then: ensure that `DENY_TASK_LISTENER` and `ASSIGNMENT_DENIED`
    // are written after `ASSIGNING` event
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.ASSIGNING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.ASSIGNMENT_DENIED);

    // validate the assignee
    assertThat(
            RecordingExporter.userTaskRecords().withProcessInstanceKey(processInstanceKey).limit(1))
        .extracting(io.camunda.zeebe.protocol.record.Record::getValue)
        .extracting(UserTaskRecordValue::getAssignee)
        .describedAs(
            "The assignee should remain unchanged as assignment was denied by Task Listener")
        .containsExactly("");
  }

  @Test
  public void
      shouldCompleteAllAssignmentTaskListenersWhenFirstTaskListenerAcceptTransitionAfterDenial() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createProcessWithAssigningTaskListeners(
                listenerType, listenerType + "_2", listenerType + "_3"));
    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("new_assignee").assign();
    // assignment fails
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("new_assignee").assign();
    // assignment is successful
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);
    helper.completeJobs(processInstanceKey, listenerType + "_2", listenerType + "_3");

    // then: ensure that all three `COMPLETE_TASK_LISTENER` events were triggered
    // correct assignee value is present at all stages
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.ASSIGNED))
        .extracting(
            io.camunda.zeebe.protocol.record.Record::getIntent, r -> r.getValue().getAssignee())
        .describedAs("Verify that all task listeners were completed with the correct assignee")
        .containsSequence(
            tuple(TaskIntent.ASSIGNING, "new_assignee"),
            tuple(TaskIntent.DENY_TASK_LISTENER, "new_assignee"),
            tuple(TaskIntent.ASSIGNMENT_DENIED, ""),
            tuple(TaskIntent.ASSIGNING, "new_assignee"),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, "new_assignee"),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, "new_assignee"),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, "new_assignee"),
            tuple(TaskIntent.ASSIGNED, "new_assignee"));
  }

  @Test
  public void shouldUpdateTaskWhenUpdatingTaskListenerAcceptsTransitionAfterDenial() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(
                ZeebeTaskListenerEventType.updating, listenerType));

    // when: attempting to update the user task priority for the first time
    ENGINE.userTask().ofInstance(processInstanceKey).withPriority(80).update();

    // and: task listener denies the first update attempt
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // when: retrying the update operation with a new priority value
    ENGINE.userTask().ofInstance(processInstanceKey).withPriority(100).update();

    // and: completing the re-created task listener job
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);

    // then
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.UPDATED))
        .extracting(
            io.camunda.zeebe.protocol.record.Record::getIntent, r -> r.getValue().getPriority())
        .describedAs(
            "Verify intents sequence and state of the `priority` property through the user task transitions")
        .containsSequence(
            // Initial state of the user task
            tuple(TaskIntent.CREATED, 50),
            // First update attempt and rejection by the listener
            tuple(TaskIntent.UPDATING, 80),
            tuple(TaskIntent.DENY_TASK_LISTENER, 80),
            // Priority reverts after rejection
            tuple(TaskIntent.UPDATE_DENIED, 50),
            // Second update attempt and successful completion
            tuple(TaskIntent.UPDATING, 100),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, 100),
            // Update was performed successfully
            tuple(TaskIntent.UPDATED, 100));
  }

  @Test
  public void
      shouldRevertToPreviousAssigneeWhenRejectingAssignmentFromTaskListenerAfterPreviouslySuccessfulAssignment() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListenersAndAssignee(
                ZeebeTaskListenerEventType.assigning, "first_assignee", listenerType));

    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(false))
        .complete();

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("second_assignee").assign();

    helper.completeRecreatedJobWithTypeAndResult(
        processInstanceKey, listenerType, new JobResult().setDenied(true));

    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.ASSIGNMENT_DENIED))
        .extracting(
            io.camunda.zeebe.protocol.record.Record::getIntent, r -> r.getValue().getAssignee())
        .describedAs(
            "Verify that the assignee changes. The assignment of the second assignee should be rejected.")
        .containsSequence(
            tuple(TaskIntent.CREATING, "first_assignee"),
            tuple(TaskIntent.CREATED, ""),
            tuple(TaskIntent.ASSIGNING, "first_assignee"),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, "first_assignee"),
            tuple(TaskIntent.ASSIGNED, "first_assignee"),
            tuple(TaskIntent.ASSIGNING, "second_assignee"),
            tuple(TaskIntent.DENY_TASK_LISTENER, "second_assignee"),
            // second assignee was not persisted
            tuple(TaskIntent.ASSIGNMENT_DENIED, "first_assignee"));

    // then: ensure that the assignee value is rolled back to the first successfully assigned
    // assignee
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .filter(r -> r.getIntent() == TaskIntent.ASSIGNMENT_DENIED)
                .limit(1))
        .extracting(Record::getValue)
        .extracting(UserTaskRecordValue::getAssignee)
        .describedAs(
            "The assignee should remain unchanged as assignment was denied by Task Listener and the original value is provided")
        .containsExactly("first_assignee");
  }

  @Test
  public void shouldCompleteTaskWithTaskListenerWhenJobResultDeniedIsFalse() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createProcessWithCompletingTaskListeners(
                listenerType, listenerType + "_2", listenerType + "_3"));

    // when
    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE.job().ofInstance(processInstanceKey).withType(listenerType).complete();

    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType + "_2")
        .withResult(new JobResult())
        .complete();

    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType + "_3")
        .withResult(new JobResult().setDenied(false))
        .complete();

    // then
    assertThat(
            RecordingExporter.jobRecords()
                .withProcessInstanceKey(processInstanceKey)
                .withJobKind(JobKind.TASK_LISTENER)
                .withJobListenerEventType(JobListenerEventType.COMPLETING)
                .withIntent(JobIntent.COMPLETED)
                .limit(3))
        .extracting(Record::getValue)
        .extracting(JobRecordValue::getType, v -> v.getResult().isDenied())
        .describedAs("Verify that all task listeners were completed with `denied=false`")
        .containsExactly(
            tuple(listenerType, false),
            tuple(listenerType + "_2", false),
            tuple(listenerType + "_3", false));
  }

  @Test
  public void shouldRejectUserTaskUpdateWhenUpdatingTaskListenerDeniesTheTransition() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(
                ZeebeTaskListenerEventType.updating, listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).withPriority(99).update();

    // when: complete `updating` a listener job with a denied result
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // then: ensure that `DENY_TASK_LISTENER` and `UPDATE_DENIED`
    // are written right after `UPDATING` event
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.UPDATING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.UPDATE_DENIED);
  }

  @Test
  public void shouldDenyUserTaskUpdateWhenUpdatingListenerDeniesTransitionOnTaskVariablesUpdate() {
    // given: a process instance with a user task that has an `updating` task listener
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(
                ZeebeTaskListenerEventType.updating, listenerType));

    // when: updating task-scoped variables
    final var userTaskInstanceKey = helper.getUserTaskElementInstanceKey(processInstanceKey);
    ENGINE
        .variables()
        .ofScope(userTaskInstanceKey)
        .withDocument(Map.of("status", "APPROVED"))
        .withLocalSemantic()
        .expectUpdating()
        .update();

    // then: deny the update transition from the listener job
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // verify that `DENY_TASK_LISTENER` and `UPDATE_DENIED` are written after `UPDATING`
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.UPDATING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.UPDATE_DENIED);

    assertThat(
            RecordingExporter.variableDocumentRecords(VariableDocumentIntent.UPDATE_DENIED)
                .withScopeKey(userTaskInstanceKey)
                .exists())
        .describedAs(
            "Expect a VariableDocument.UPDATE_DENIED event when variables update was denied")
        .isTrue();
  }

  @Test
  public void shouldRejectUserTaskCompletionWhenCompletingTaskListenerDeniesTheTransition() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithCompletingTaskListeners(listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // then: ensure that `DENY_TASK_LISTENER` and `COMPLETION_DENIED`
    // are written after `COMPLETING` event
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.COMPLETING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.COMPLETION_DENIED);
  }

  @Test
  public void shouldAcceptUserTaskCompletionAfterTaskListenerRejectsTheOperationWithDeniedReason() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithCompletingTaskListeners(listenerType));

    // deny the completion
    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(
            new JobResult().setDenied(true).setDeniedReason("Reason to deny lifecycle transition"))
        .complete();

    // completion is successful
    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);
    helper.completeJobs(processInstanceKey);

    // validate reason to deny lifecycle transition in events
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.COMPLETED))
        .extracting(Record::getIntent, r -> getDeniedReason(r.getValue()))
        .describedAs(
            "The reason to deny lifecycle transition should be present when task listener denies the work")
        .containsExactly(
            tuple(TaskIntent.CREATING, ""),
            tuple(TaskIntent.CREATED, ""),
            tuple(TaskIntent.COMPLETING, ""),
            tuple(TaskIntent.DENY_TASK_LISTENER, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.COMPLETION_DENIED, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.COMPLETING, ""),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, ""),
            tuple(TaskIntent.COMPLETED, ""));
  }

  @Test
  public void shouldAcceptUserTaskAssignmentAfterTaskListenerRejectsTheOperationWithDeniedReason() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithAssigningTaskListeners(listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("new_assignee").assign();

    // deny the assignment
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(
            new JobResult().setDenied(true).setDeniedReason("Reason to deny lifecycle transition"))
        .complete();

    // accept the assignment
    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("new_assignee").assign();
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);
    helper.completeJobs(processInstanceKey);

    // validate the reason to deny lifecycle transition
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.ASSIGNED))
        .extracting(Record::getIntent, r -> getDeniedReason(r.getValue()))
        .describedAs(
            "The reason to deny lifecycle transition should be present when task listener denies the work")
        .containsExactly(
            tuple(TaskIntent.CREATING, ""),
            tuple(TaskIntent.CREATED, ""),
            tuple(TaskIntent.ASSIGNING, ""),
            tuple(TaskIntent.DENY_TASK_LISTENER, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.ASSIGNMENT_DENIED, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.ASSIGNING, ""),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, ""),
            tuple(TaskIntent.ASSIGNED, ""));
  }

  @Test
  public void shouldAcceptUserTaskUpdateAfterTaskListenerRejectsTheOperationWithDeniedReason() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(
                ZeebeTaskListenerEventType.updating, listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).withPriority(80).update();

    // deny the update
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(
            new JobResult().setDenied(true).setDeniedReason("Reason to deny lifecycle transition"))
        .complete();

    // accept the update
    ENGINE.userTask().ofInstance(processInstanceKey).withPriority(80).update();
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);
    helper.completeJobs(processInstanceKey);

    // validate the reason to deny lifecycle transition
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == TaskIntent.UPDATED))
        .extracting(Record::getIntent, r -> getDeniedReason(r.getValue()))
        .describedAs(
            "The reason to deny lifecycle transition should be present when task listener denies the work")
        .containsExactly(
            tuple(TaskIntent.CREATING, ""),
            tuple(TaskIntent.CREATED, ""),
            tuple(TaskIntent.UPDATING, ""),
            tuple(TaskIntent.DENY_TASK_LISTENER, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.UPDATE_DENIED, "Reason to deny lifecycle transition"),
            tuple(TaskIntent.UPDATING, ""),
            tuple(TaskIntent.COMPLETE_TASK_LISTENER, ""),
            tuple(TaskIntent.UPDATED, ""));
  }

  @Test
  public void
      shouldAcceptUserTaskUpdateAfterTaskListenerRejectsTheOperationWithDeniedReasonOnTaskVariablesUpdate() {
    // given: a process instance with a user task that has an `updating` task listener
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(
                ZeebeTaskListenerEventType.updating, listenerType));

    final var userTaskInstanceKey = helper.getUserTaskElementInstanceKey(processInstanceKey);

    // when: first update attempt with variable update
    ENGINE
        .variables()
        .ofScope(userTaskInstanceKey)
        .withDocument(Map.of("status", "APPROVED"))
        .withLocalSemantic()
        .expectUpdating()
        .update();

    // first attempt is denied
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    // when: retry the variable update
    ENGINE
        .variables()
        .ofScope(userTaskInstanceKey)
        .withDocument(Map.of("status", "CONFIRMED"))
        .withLocalSemantic()
        .expectUpdating()
        .update();

    // accept the transition this time
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);

    // then: verify that `UPDATING`, `COMPLETE_TASK_LISTENER`, and `UPDATED` events are present
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.UPDATING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.UPDATE_DENIED,
        TaskIntent.UPDATING,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.UPDATED);

    assertThat(
            RecordingExporter.variableDocumentRecords(VariableDocumentIntent.UPDATE_DENIED)
                .withScopeKey(userTaskInstanceKey)
                .exists())
        .describedAs(
            "Expect a VariableDocument.UPDATE_DENIED event when variables update was denied")
        .isTrue();
  }

  private String getDeniedReason(final UserTaskRecordValue record) {
    // This is to be removed when denied reason is exposed in the UserTaskRecordValue interface.
    // Currently added in order to separate processing implementation.
    return ((UserTaskRecord) record).getDeniedReason();
  }

  @Test
  public void shouldCompleteTaskWhenCompletingTaskListenerAcceptsTransitionAfterDenial() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithCompletingTaskListeners(listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    ENGINE.userTask().ofInstance(processInstanceKey).complete();

    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);

    // then: ensure that `COMPLETING` `COMPLETE_TASK_LISTENER` and `COMPLETED events
    // are present after `DENY_TASK_LISTENER` and `COMPLETION_DENIED` events
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.COMPLETING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.COMPLETION_DENIED,
        TaskIntent.COMPLETING,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.COMPLETED);
  }

  @Test
  public void shouldCompleteAllTaskListenersWhenFirstTaskListenerAcceptTransitionAfterDenial() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createProcessWithCompletingTaskListeners(
                listenerType, listenerType + "_2", listenerType + "_3"));

    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);
    helper.completeJobs(processInstanceKey, listenerType + "_2", listenerType + "_3");

    // then: ensure that all three `COMPLETE_TASK_LISTENER` events were triggered after the
    // rejection from the first Task Listener
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.COMPLETING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.COMPLETION_DENIED,
        TaskIntent.COMPLETING,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.COMPLETED);
  }

  @Test
  public void shouldAssignAndCompleteTaskAfterTaskListenerDeniesTheCompletion() {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(helper.createProcessWithCompletingTaskListeners(listenerType));

    ENGINE.userTask().ofInstance(processInstanceKey).complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(new JobResult().setDenied(true))
        .complete();

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("Test Assignee").assign();
    ENGINE.userTask().ofInstance(processInstanceKey).complete();

    helper.completeRecreatedJobWithType(processInstanceKey, listenerType);

    // then: ensure that user task could be assigned after completion was rejected from the
    // `COMPLETE` Task Listener. Ensure that user task could be completed after assignment
    // and `COMPLETE_TASK_LISTENER` event was triggered successfully
    helper.assertUserTaskIntentsSequence(
        processInstanceKey,
        TaskIntent.COMPLETING,
        TaskIntent.DENY_TASK_LISTENER,
        TaskIntent.COMPLETION_DENIED,
        TaskIntent.ASSIGNING,
        TaskIntent.ASSIGNED,
        TaskIntent.COMPLETING,
        TaskIntent.COMPLETE_TASK_LISTENER,
        TaskIntent.COMPLETED);
  }

  @Test
  public void shouldRevertCorrectedUserTaskDataWhenAssigningOnCreationTaskListenerDenies() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.assigning,
        u -> u.zeebeAssignee("initial_assignee"),
        userTask -> {},
        TaskIntent.ASSIGNMENT_DENIED);
  }

  @Test
  public void shouldRevertCorrectedUserTaskDataWhenAssigningTaskListenerDenies() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.assigning,
        u -> u,
        pik -> ENGINE.userTask().ofInstance(pik).withAssignee("initial_assignee").assign(),
        TaskIntent.ASSIGNMENT_DENIED);
  }

  @Test
  public void shouldRevertCorrectedUserTaskDataWhenClaimingTaskListenerDenies() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.assigning,
        u -> u,
        pik -> ENGINE.userTask().ofInstance(pik).withAssignee("initial_assignee").claim(),
        TaskIntent.ASSIGNMENT_DENIED);
  }

  @Test
  public void shouldRevertCorrectedUserTaskDataWhenUpdatingTaskListenerDenies() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.updating,
        u -> u,
        pik -> ENGINE.userTask().ofInstance(pik).update(),
        TaskIntent.UPDATE_DENIED);
  }

  @Test
  public void
      shouldRevertCorrectedUserTaskDataWhenUpdatingTaskListenerDeniesOnTaskVariablesUpdate() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.updating,
        u -> u,
        pik ->
            ENGINE
                .variables()
                .ofScope(helper.getUserTaskElementInstanceKey(pik))
                .withDocument(Map.of("status", "APPROVED"))
                .withLocalSemantic()
                .expectUpdating()
                .update(),
        TaskIntent.UPDATE_DENIED);
  }

  @Test
  public void shouldRevertCorrectedUserTaskDataWhenCompletingTaskListenerDenies() {
    testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
        ZeebeTaskListenerEventType.completing,
        u -> u,
        pik -> ENGINE.userTask().ofInstance(pik).complete(),
        TaskIntent.COMPLETION_DENIED);
  }

  private void testRevertCorrectedUserTaskDataWhenTaskListenerDenies(
      final ZeebeTaskListenerEventType eventType,
      final UnaryOperator<UserTaskBuilder> userTaskBuilder,
      final Consumer<Long> transitionTrigger,
      final TaskIntent expectedUserTaskIntent) {
    // given
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createProcessWithZeebeUserTask(
                t ->
                    userTaskBuilder
                        .apply(t)
                        .zeebeTaskListener(l -> l.eventType(eventType).type(listenerType))
                        .zeebeTaskListener(l -> l.eventType(eventType).type(listenerType + "_2"))));

    transitionTrigger.accept(processInstanceKey);

    // when
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType)
        .withResult(
            new JobResult()
                .setCorrections(
                    new JobResultCorrections()
                        .setAssignee("new_assignee")
                        .setCandidateUsersList(List.of("new_candidate_user"))
                        .setCandidateGroupsList(List.of("new_candidate_group"))
                        .setDueDate("new_due_date")
                        .setFollowUpDate("new_follow_up_date")
                        .setPriority(100))
                .setCorrectedAttributes(
                    List.of(
                        "assignee",
                        "candidateUsersList",
                        "candidateGroupsList",
                        "dueDate",
                        "followUpDate",
                        "priority")))
        .complete();
    ENGINE
        .job()
        .ofInstance(processInstanceKey)
        .withType(listenerType + "_2")
        .withResult(new JobResult().setDenied(true))
        .complete();

    // then
    helper.assertUserTaskRecordWithIntent(
        processInstanceKey,
        expectedUserTaskIntent,
        userTaskRecord ->
            Assertions.assertThat(userTaskRecord)
                .describedAs("Expect that user task data is reverted to before the event")
                .hasNoChangedAttributes()
                .hasNoCandidateUsersList()
                .hasNoCandidateGroupsList()
                .hasDueDate("")
                .hasFollowUpDate("")
                .hasPriority(50)
                .hasAssignee(""));
  }

  @Test
  public void shouldRejectCreatingTaskListenerCompletionWithDeniedResult() {
    assertDenyResultIsRejectedForNonDeniableListenerType(
        ZeebeTaskListenerEventType.creating, ignored -> {});
  }

  @Test
  public void shouldRejectCancelingTaskListenerCompletionWithDeniedResult() {
    assertDenyResultIsRejectedForNonDeniableListenerType(
        ZeebeTaskListenerEventType.canceling,
        pik -> ENGINE.processInstance().withInstanceKey(pik).expectTerminating().cancel());
  }

  private void assertDenyResultIsRejectedForNonDeniableListenerType(
      final ZeebeTaskListenerEventType listenerEventType, final Consumer<Long> triggerTransition) {

    // given: a process instance with a task listener that doesn't support denying
    final long processInstanceKey =
        helper.createProcessInstance(
            helper.createUserTaskWithTaskListeners(listenerEventType, listenerType));

    // trigger transition
    triggerTransition.accept(processInstanceKey);

    // when: attempting to complete the task listener job with `denied=true`
    final var result =
        ENGINE
            .job()
            .ofInstance(processInstanceKey)
            .withType(listenerType)
            .withResult(new JobResult().setDenied(true))
            .expectRejection()
            .complete();

    // then: job completion should be rejected
    final var expectedJobListenerType = helper.mapToJobListenerEventType(listenerEventType);
    Assertions.assertThat(result)
        .describedAs(
            "Task listener of type '%s' should reject job completion with 'denied=true' result",
            expectedJobListenerType)
        .hasIntent(JobIntent.COMPLETE)
        .hasRejectionType(RejectionType.INVALID_ARGUMENT)
        .hasRejectionReason(
            """
                Denying result is not supported for '%s' task listener jobs \
                (job key '%d', type '%s', processInstanceKey '%d'). \
                Only the following listener event types support denying: [ASSIGNING, COMPLETING, UPDATING].
                """
                .formatted(
                    expectedJobListenerType,
                    result.getKey(),
                    result.getValue().getType(),
                    processInstanceKey));
  }
}
