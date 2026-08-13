package com.qizhifu.jiaoxuepeiyu.ue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingStatusCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeScoreWeight;
import com.qizhifu.jiaoxuepeiyu.ue.port.UeTrainingCallbackRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UeTrainingCallbackServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-30T10:15:30Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void rejectsTaskOutsideCurrentStudentAssignments() {
        FakeCallbacks repository = new FakeCallbacks();
        UeTrainingCallbackService service = new UeTrainingCallbackService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.getTask(7L, 99L, 31L));

        assertEquals("Training task not found", exception.getMessage());
    }

    @Test
    void reportsRunningStatusIntoMonitorSnapshot() {
        FakeCallbacks repository = new FakeCallbacks();
        repository.task = task();
        UeTrainingCallbackService service = new UeTrainingCallbackService(repository, CLOCK);
        TrainingStatusCommand command = new TrainingStatusCommand();
        command.setClassroomId(3L);
        command.setDeskStatus("ONLINE");
        command.setProgressStatus("RUNNING");
        command.setScore(new BigDecimal("37.5"));

        service.reportStatus(7L, 15L, 31L, command);

        assertEquals(Long.valueOf(15L), repository.snapshot.getTrainingId());
        assertEquals(Long.valueOf(7L), repository.snapshot.getStudentId());
        assertEquals(Long.valueOf(3L), repository.snapshot.getClassroomId());
        assertEquals("ONLINE", repository.snapshot.getDeskStatus());
        assertEquals("RUNNING", repository.snapshot.getProgressStatus());
        assertEquals(new BigDecimal("37.5"), repository.snapshot.getScore());
        assertNotNull(repository.snapshot.getLastEventAt());
    }

    @Test
    void submitsAttemptArchiveAndMarksMonitorSubmitted() {
        FakeCallbacks repository = new FakeCallbacks();
        repository.task = task();
        UeTrainingCallbackService service = new UeTrainingCallbackService(repository, CLOCK);
        TrainingAttemptCommand command = new TrainingAttemptCommand();
        command.setSubmitType("NORMAL");
        command.setClientAttemptId("attempt-20260805-001");
        command.setDurationSeconds(480);
        command.setPersonalScore(new BigDecimal("92.5"));
        command.setTeamScore(new BigDecimal("88.0"));
        command.setRecordingUrl("/uploads/recordings/run.mp4");
        TrainingAttemptStepCommand firstStep = new TrainingAttemptStepCommand();
        firstStep.setStepName("Power on");
        firstStep.setStandardOperation("Turn on the simulator");
        firstStep.setActualOperation("Completed");
        firstStep.setScore(new BigDecimal("10"));
        firstStep.setDurationSeconds(40);
        firstStep.setVideoStartSecond(5);
        command.setSteps(Arrays.asList(firstStep));

        Long attemptId = service.submitAttempt(7L, 15L, 31L, command);

        assertEquals(Long.valueOf(101L), attemptId);
        assertEquals("Crane Practice", repository.submission.getTrainingName());
        assertEquals("TEAM", repository.submission.getTrainingMode());
        assertEquals("Operator", repository.submission.getRoleName());
        assertEquals(new BigDecimal("92.5"), repository.submission.getPersonalScore());
        assertEquals(1, repository.steps.size());
        assertEquals(Long.valueOf(101L), repository.steps.get(0).getAttemptId());
        assertEquals("Power on", repository.steps.get(0).getStepName());
        assertEquals("SUBMITTED", repository.snapshot.getProgressStatus());
        assertEquals(new BigDecimal("92.5"), repository.snapshot.getScore());
        assertEquals(new BigDecimal("88.0"), repository.snapshot.getTeamScore());
        assertEquals(Long.valueOf(202601L), repository.syncedSemesterId);
        assertEquals(new BigDecimal("92.5"), repository.syncedTrainingPracticeScore);
    }

    @Test
    void returnsExistingAttemptForRepeatedClientAttemptId() {
        FakeCallbacks repository = new FakeCallbacks();
        repository.task = task();
        UeTrainingCallbackService service = new UeTrainingCallbackService(repository, CLOCK);
        TrainingAttemptCommand command = new TrainingAttemptCommand();
        command.setClientAttemptId("attempt-20260805-001");
        command.setPersonalScore(new BigDecimal("92.5"));

        Long firstAttemptId = service.submitAttempt(7L, 15L, 31L, command);
        Long repeatedAttemptId = service.submitAttempt(7L, 15L, 31L, command);

        assertEquals(firstAttemptId, repeatedAttemptId);
        assertEquals(1, repository.insertCount);
    }

    @Test
    void rejectsAttemptForTopicOutsideLaunchSession() {
        FakeCallbacks repository = new FakeCallbacks();
        repository.task = task();
        UeTrainingCallbackService service = new UeTrainingCallbackService(repository, CLOCK);
        TrainingAttemptCommand command = new TrainingAttemptCommand();
        command.setTopicId(32L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.submitAttempt(7L, 15L, 31L, command));

        assertEquals("Submitted training topic does not match launch session", exception.getMessage());
    }

    private TrainingLaunchTask task() {
        TrainingLaunchTask task = new TrainingLaunchTask();
        task.setTrainingId(15L);
        task.setTrainingName("Crane Practice");
        task.setTrainingType("PRACTICE");
        task.setTrainingMode("TEAM");
        task.setTopicId(31L);
        task.setTopicName("Platform emergency handling");
        task.setStudentId(7L);
        task.setStudentName("Student Seven");
        task.setRoomId(22L);
        task.setRoomCode("ROOM-22");
        task.setRoomStatus("STARTED");
        task.setRoleId(5L);
        task.setRoleName("Operator");
        task.setTeamSize(2);
        task.setAiRoleNames(Collections.singletonList("Safety Officer"));
        return task;
    }

    private static class FakeCallbacks implements UeTrainingCallbackRepository {
        private TrainingLaunchTask task;
        private TrainingMonitorSnapshotCommand snapshot;
        private TrainingAttemptSubmission submission;
        private Long syncedSemesterId;
        private BigDecimal syncedTrainingPracticeScore;
        private int insertCount;
        private final List<TrainingAttemptStepCommand> steps = new ArrayList<TrainingAttemptStepCommand>();

        @Override
        public Optional<TrainingLaunchTask> findTask(Long trainingId, Long studentId, Long topicId) {
            if (task == null) {
                return Optional.empty();
            }
            return Optional.of(task);
        }

        @Override
        public void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command) {
            this.snapshot = command;
        }

        @Override
        public Long insertAttempt(TrainingAttemptSubmission submission) {
            this.submission = submission;
            insertCount++;
            submission.setAttemptId(101L);
            return 101L;
        }

        @Override
        public Optional<Long> findAttemptId(Long studentId, Long trainingId, String clientAttemptId) {
            if (submission != null && clientAttemptId.equals(submission.getClientAttemptId())) {
                return Optional.of(101L);
            }
            return Optional.empty();
        }

        @Override
        public void insertAttemptStep(Long attemptId, TrainingAttemptStepCommand step, int sortOrder) {
            step.setAttemptId(attemptId);
            step.setSortOrder(sortOrder);
            steps.add(step);
        }

        @Override
        public Optional<Long> findCurrentSemesterId() {
            return Optional.of(202601L);
        }

        @Override
        public UeScoreWeight findLatestScoreWeight(Long semesterId) {
            return new UeScoreWeight(20, 35, 15, 30);
        }

        @Override
        public void upsertTrainingPracticeScore(Long studentId,
                                                Long semesterId,
                                                BigDecimal trainingPracticeScore,
                                                UeScoreWeight weight) {
            this.syncedSemesterId = semesterId;
            this.syncedTrainingPracticeScore = trainingPracticeScore;
        }
    }
}
