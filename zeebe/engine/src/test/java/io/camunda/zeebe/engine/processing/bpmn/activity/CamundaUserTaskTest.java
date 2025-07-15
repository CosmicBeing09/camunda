@Test
public void shouldAssignUserTask() {
    // given
    ENGINE.deployment().withXmlResource(process()).deploy();

    // when
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("foo").assign();

    // then
    final UserTaskRecordValue createdUserTask =
        RecordingExporter.userTaskRecords(UserTaskIntent.CREATED)
            .withProcessInstanceKey(processInstanceKey)
            .getFirst()
            .getValue();

    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == UserTaskIntent.ASSIGNED))
        .extracting(Record::getValueType, Record::getIntent)
        .containsSubsequence(
            tuple(ValueType.USER_TASK, UserTaskIntent.ASSIGNING),
            tuple(ValueType.USER_TASK, UserTaskIntent.ASSIGNED));

    Assertions.assertThat(
            RecordingExporter.userTaskRecords(UserTaskIntent.ASSIGNED)
                .withProcessInstanceKey(processInstanceKey)
                .getFirst()
                .getValue())
        .hasAssignee("foo");

    Assertions.assertThat(userTaskState.getUserTask(createdUserTask.getUserTaskKey()))
        .hasAssignee("foo");
} 

@Test
public void shouldClaimUserTask() {
    // given
    ENGINE.deployment().withXmlResource(process()).deploy();

    // when
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    ENGINE.userTask().ofInstance(processInstanceKey).withAssignee("foo").claim();

    // then
    final UserTaskRecordValue createdUserTask =
        RecordingExporter.userTaskRecords(UserTaskIntent.CREATED)
            .withProcessInstanceKey(processInstanceKey)
            .getFirst()
            .getValue();

    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == UserTaskIntent.ASSIGNED))
        .extracting(Record::getValueType, Record::getIntent)
        .containsSubsequence(
            tuple(ValueType.USER_TASK, UserTaskIntent.CLAIMING),
            tuple(ValueType.USER_TASK, UserTaskIntent.ASSIGNED));

    Assertions.assertThat(
            RecordingExporter.userTaskRecords(UserTaskIntent.ASSIGNED)
                .withProcessInstanceKey(processInstanceKey)
                .getFirst()
                .getValue())
        .hasAssignee("foo");

    Assertions.assertThat(userTaskState.getUserTask(createdUserTask.getUserTaskKey()))
        .hasAssignee("foo");
} 

@Test
public void shouldUpdateUserTaskAttributes() {
    // given
    ENGINE
        .deployment()
        .withXmlResource(
            process(
                t ->
                    t.zeebeCandidateGroups("foo, bar")
                        .zeebeCandidateUsers("oof, rab")
                        .zeebeFollowUpDate("2023-03-02T15:35+02:00")
                        .zeebeDueDate("2023-03-02T16:35+02:00")
                        .zeebeTaskPriority("90")))
        .deploy();
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    ENGINE.userTask().ofInstance(processInstanceKey).withAllAttributesChanged().update();

    // then
    final UserTaskRecordValue createdUserTask =
        RecordingExporter.userTaskRecords(UserTaskIntent.CREATED)
            .withProcessInstanceKey(processInstanceKey)
            .getFirst()
            .getValue();

    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == UserTaskIntent.UPDATED))
        .extracting(Record::getValueType, Record::getIntent)
        .containsSubsequence(
            tuple(ValueType.USER_TASK, UserTaskIntent.UPDATING),
            tuple(ValueType.USER_TASK, UserTaskIntent.UPDATED));

    Assertions.assertThat(createdUserTask)
        .hasCandidateGroupsList("foo", "bar")
        .hasCandidateUsersList("oof", "rab")
        .hasDueDate("2023-03-02T16:35+02:00")
        .hasFollowUpDate("2023-03-02T15:35+02:00")
        .hasNoChangedAttributes()
        .hasPriority(90);

    Assertions.assertThat(
            RecordingExporter.userTaskRecords(UserTaskIntent.UPDATED)
                .withProcessInstanceKey(processInstanceKey)
                .getFirst()
                .getValue())
        .hasNoCandidateGroupsList()
        .hasNoCandidateUsersList()
        .hasDueDate("")
        .hasFollowUpDate("")
        .hasPriority(50)
        .hasChangedAttributes(
            UserTaskRecord.CANDIDATE_GROUPS,
            UserTaskRecord.CANDIDATE_USERS,
            UserTaskRecord.DUE_DATE,
            UserTaskRecord.FOLLOW_UP_DATE,
            UserTaskRecord.PRIORITY);

    Assertions.assertThat(userTaskState.getUserTask(createdUserTask.getUserTaskKey()))
        .hasNoCandidateGroupsList()
        .hasNoCandidateUsersList()
        .hasDueDate("")
        .hasFollowUpDate("")
        .hasPriority(50)
        .hasNoChangedAttributes();
} 

@Test
public void
      shouldUpdateLocalVariablesAndPassUserTaskUpdateTransitionWhenUserTaskHasNoUpdatingListeners() {
    // given
    ENGINE.deployment().withXmlResource(process()).deploy();
    final long processInstanceKey =
        ENGINE
            .processInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariables(Map.of("approvalStatus", "PENDING"))
            .create();

    final var createdUserTaskRecord =
        RecordingExporter.userTaskRecords(UserTaskIntent.CREATED)
            .withProcessInstanceKey(processInstanceKey)
            .getFirst();

    // when: updating task-scoped variables
    final var variableUpdateRecord =
        ENGINE
            .variables()
            .ofScope(createdUserTaskRecord.getValue().getElementInstanceKey())
            .withDocument(Map.of("approvalStatus", "SUBMITTED"))
            .withLocalSemantic()
            .update();

    // then: variable update should be successful and trigger the user task update transition
    Assertions.assertThat(variableUpdateRecord)
        .describedAs("Expect variables to be successfully updated for a user task")
        .hasRecordType(RecordType.EVENT)
        .hasIntent(VariableDocumentIntent.UPDATED)
        .hasValueType(ValueType.VARIABLE_DOCUMENT);

    Assertions.assertThat(
            RecordingExporter.variableRecords(VariableIntent.CREATED)
                .withProcessInstanceKey(processInstanceKey)
                .withScopeKey(createdUserTaskRecord.getValue().getElementInstanceKey())
                .getFirst()
                .getValue())
        .describedAs("Expect the variable to be created at the local scope of user task element")
        .hasName("approvalStatus")
        .hasValue("\"SUBMITTED\"");

    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == UserTaskIntent.UPDATED))
        .extracting(Record::getIntent, r -> r.getValue().getChangedAttributes())
        .describedAs(
            "Expect the user task to pass the update transition with variables as a changed attribute")
        .containsSequence(
            Tuple.tuple(UserTaskIntent.UPDATING, List.of(UserTaskRecord.VARIABLES)),
            Tuple.tuple(UserTaskIntent.UPDATED, List.of(UserTaskRecord.VARIABLES)));
} 

@Test
public void
      shouldPropagateVariableUpdatesAndPassUserTaskUpdateTransitionWhenUserTaskHasNoUpdatingListeners() {
    // given: a process with a user task and one process-level variable
    ENGINE.deployment().withXmlResource(process()).deploy();
    final long processInstanceKey =
        ENGINE
            .processInstance()
            .ofBpmnProcessId(PROCESS_ID)
            .withVariables(Map.of("approvalStatus", "PENDING"))
            .create();

    final var createdUserTaskRecord =
        RecordingExporter.userTaskRecords(UserTaskIntent.CREATED)
            .withProcessInstanceKey(processInstanceKey)
            .getFirst();

    // when: updating a process-level variable and creating a new one using `PROPAGATE` semantic
    ENGINE
        .variables()
        .ofScope(createdUserTaskRecord.getValue().getElementInstanceKey())
        .withDocument(
            Map.of(
                "approvalStatus", "SUBMITTED",
                "reviewerComment", "LGTM"))
        .withPropagateSemantic()
        .update();

    // then: process-level variables should be updated/created accordingly
    assertThat(
            RecordingExporter.variableRecords().withProcessInstanceKey(processInstanceKey).limit(3))
        .extracting(
            Record::getIntent,
            r -> r.getValue().getScopeKey(),
            r -> r.getValue().getName(),
            r -> r.getValue().getValue())
        .contains(
            tuple(VariableIntent.CREATED, processInstanceKey, "approvalStatus", "\"PENDING\""),
            tuple(VariableIntent.UPDATED, processInstanceKey, "approvalStatus", "\"SUBMITTED\""),
            tuple(VariableIntent.CREATED, processInstanceKey, "reviewerComment", "\"LGTM\""));

    // and: user task should pass update transition with VARIABLES in changedAttributes
    assertThat(
            RecordingExporter.userTaskRecords()
                .withProcessInstanceKey(processInstanceKey)
                .limit(r -> r.getIntent() == UserTaskIntent.UPDATED))
        .extracting(Record::getIntent, r -> r.getValue().getChangedAttributes())
        .describedAs(
            "Expect the user task to pass the update transition with variables as a changed attribute")
        .containsSequence(
            Tuple.tuple(UserTaskIntent.UPDATING, List.of(UserTaskRecord.VARIABLES)),
            Tuple.tuple(UserTaskIntent.UPDATED, List.of(UserTaskRecord.VARIABLES)));
}