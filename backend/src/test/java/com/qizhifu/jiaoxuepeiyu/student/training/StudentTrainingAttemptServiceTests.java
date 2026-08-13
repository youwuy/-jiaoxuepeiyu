package com.qizhifu.jiaoxuepeiyu.student.training;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.student.archive.StudentArchiveService;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveStep;
import com.qizhifu.jiaoxuepeiyu.student.archive.port.StudentArchiveRepository;
import com.qizhifu.jiaoxuepeiyu.ue.UeTrainingCallbackService;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeScoreWeight;
import com.qizhifu.jiaoxuepeiyu.ue.port.UeTrainingCallbackRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentTrainingAttemptServiceTests {

    @Test
    void submitsStudentTrainingAttemptThroughArchivePipeline() {
        FakeCallbacks callbacks = new FakeCallbacks();
        StudentTrainingAttemptService service = new StudentTrainingAttemptService(
                new UeTrainingCallbackService(callbacks),
                new StudentArchiveService(new FakeArchives()));
        StudentTrainingAttemptRequest request = new StudentTrainingAttemptRequest();
        request.setTrainingId(15L);
        request.setTopicId(31L);
        request.setSubmitType("NORMAL");
        request.setDurationSeconds(480);
        request.setPersonalScore(new BigDecimal("92.5"));
        TrainingAttemptStepCommand step = new TrainingAttemptStepCommand();
        step.setStepName("Power on");
        step.setScore(new BigDecimal("10"));
        request.setSteps(Arrays.asList(step));

        Long attemptId = service.submitAttempt(7L, request);

        assertEquals(Long.valueOf(101L), attemptId);
        assertEquals(Long.valueOf(7L), callbacks.submission.getStudentId());
        assertEquals(Long.valueOf(15L), callbacks.submission.getTrainingId());
        assertEquals(Long.valueOf(31L), callbacks.submission.getTopicId());
        assertEquals(new BigDecimal("92.5"), callbacks.submission.getPersonalScore());
        assertEquals("SUBMITTED", callbacks.snapshot.getProgressStatus());
    }

    @Test
    void returnsScoreSheetFromStudentArchiveDetail() {
        StudentTrainingAttemptService service = new StudentTrainingAttemptService(
                new UeTrainingCallbackService(new FakeCallbacks()),
                new StudentArchiveService(new FakeArchives()));

        StudentTrainingArchiveDetail detail = service.getScoreSheet(7L, 101L);

        assertEquals("Crane Practice", detail.getTrainingName());
        assertEquals("Operator", detail.getRoleName());
        assertEquals(1, detail.getSteps().size());
    }

    private static class FakeCallbacks implements UeTrainingCallbackRepository {
        private TrainingAttemptSubmission submission;
        private TrainingMonitorSnapshotCommand snapshot;

        @Override
        public Optional<TrainingLaunchTask> findTask(Long trainingId, Long studentId, Long topicId) {
            TrainingLaunchTask task = new TrainingLaunchTask();
            task.setTrainingId(trainingId);
            task.setTrainingName("Crane Practice");
            task.setTrainingMode("TEAM");
            task.setStudentId(studentId);
            task.setRoleName("Operator");
            return Optional.of(task);
        }

        @Override
        public void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command) {
            snapshot = command;
        }

        @Override
        public Long insertAttempt(TrainingAttemptSubmission submission) {
            this.submission = submission;
            submission.setAttemptId(101L);
            return 101L;
        }

        @Override
        public void insertAttemptStep(Long attemptId, TrainingAttemptStepCommand step, int sortOrder) {
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
        }
    }

    private static class FakeArchives implements StudentArchiveRepository {
        @Override
        public List<StudentTrainingArchive> findArchives(Long studentId, String mode, String keyword) {
            return Collections.emptyList();
        }

        @Override
        public Optional<StudentTrainingArchiveDetail> findArchiveDetail(Long studentId, Long archiveId) {
            StudentTrainingArchiveDetail detail = new StudentTrainingArchiveDetail();
            detail.setArchiveId(archiveId);
            detail.setTrainingName("Crane Practice");
            detail.setTrainingMode("TEAM");
            detail.setRoleName("Operator");
            return Optional.of(detail);
        }

        @Override
        public List<StudentTrainingArchiveStep> findArchiveSteps(Long archiveId) {
            StudentTrainingArchiveStep step = new StudentTrainingArchiveStep();
            step.setStepId(1L);
            step.setStepName("Power on");
            return Arrays.asList(step);
        }
    }
}
