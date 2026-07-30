package com.qizhifu.jiaoxuepeiyu.ue;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingStatusCommand;
import com.qizhifu.jiaoxuepeiyu.ue.port.UeTrainingCallbackRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UeTrainingCallbackService {

    private static final Set<String> DESK_STATUSES = new HashSet<String>(Arrays.asList("OFFLINE", "ONLINE", "FAULT"));
    private static final Set<String> PROGRESS_STATUSES = new HashSet<String>(Arrays.asList("NOT_STARTED", "RUNNING", "SUBMITTED", "ABNORMAL"));
    private static final Set<String> SUBMIT_TYPES = new HashSet<String>(Arrays.asList("NORMAL", "ABNORMAL_EXIT", "ROOM_DISSOLVED"));
    private static final BigDecimal MIN_SCORE = new BigDecimal("0");
    private static final BigDecimal MAX_SCORE = new BigDecimal("100");

    private final UeTrainingCallbackRepository repository;
    private final Clock clock;

    public UeTrainingCallbackService(UeTrainingCallbackRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    UeTrainingCallbackService(UeTrainingCallbackRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public TrainingLaunchTask getTask(Long studentId, Long trainingId) {
        requireId(studentId, "Student id is required");
        requireId(trainingId, "Training id is required");
        return repository.findTask(trainingId, studentId)
                .orElseThrow(() -> new BusinessException(404, "Training task not found"));
    }

    @Transactional
    public void reportStatus(Long studentId, Long trainingId, TrainingStatusCommand command) {
        getTask(studentId, trainingId);
        TrainingStatusCommand normalized = command == null ? new TrainingStatusCommand() : command;
        TrainingMonitorSnapshotCommand snapshot = snapshot(studentId, trainingId,
                normalized.getClassroomId(),
                normalizeStatus(normalized.getDeskStatus(), "ONLINE", DESK_STATUSES, "Desk status is invalid"),
                normalizeStatus(normalized.getProgressStatus(), "RUNNING", PROGRESS_STATUSES, "Progress status is invalid"),
                normalized.getScore(),
                normalized.getTeamScore(),
                normalized.getEventTime());
        repository.upsertMonitorSnapshot(snapshot);
    }

    @Transactional
    public Long submitAttempt(Long studentId, Long trainingId, TrainingAttemptCommand command) {
        TrainingLaunchTask task = getTask(studentId, trainingId);
        TrainingAttemptCommand normalized = command == null ? new TrainingAttemptCommand() : command;
        String submitType = normalizeStatus(normalized.getSubmitType(), "NORMAL", SUBMIT_TYPES, "Submit type is invalid");
        assertNonNegative(normalized.getDurationSeconds(), "Duration seconds is invalid");
        assertScore(normalized.getPersonalScore(), "Personal score is invalid");
        assertScore(normalized.getTeamScore(), "Team score is invalid");
        validateSteps(normalized.getSteps());

        TrainingAttemptSubmission submission = new TrainingAttemptSubmission();
        submission.setStudentId(studentId);
        submission.setTrainingId(trainingId);
        submission.setTrainingName(task.getTrainingName());
        submission.setTrainingMode(task.getTrainingMode());
        submission.setRoleName(task.getRoleName());
        submission.setSubmittedAt(normalized.getSubmittedAt() == null ? now() : normalized.getSubmittedAt());
        submission.setSubmitType(submitType);
        submission.setDurationSeconds(defaultInteger(normalized.getDurationSeconds()));
        submission.setPersonalScore(normalized.getPersonalScore());
        submission.setTeamScore(normalized.getTeamScore());
        submission.setRecordingUrl(trimToNull(normalized.getRecordingUrl()));

        Long attemptId = repository.insertAttempt(submission);
        int sortOrder = 1;
        for (TrainingAttemptStepCommand step : normalized.getSteps()) {
            repository.insertAttemptStep(attemptId, step, sortOrder);
            sortOrder++;
        }
        repository.upsertMonitorSnapshot(snapshot(studentId, trainingId, null, "ONLINE",
                "NORMAL".equals(submitType) ? "SUBMITTED" : "ABNORMAL",
                normalized.getPersonalScore(), normalized.getTeamScore(), submission.getSubmittedAt()));
        return attemptId;
    }

    private TrainingMonitorSnapshotCommand snapshot(Long studentId,
                                                    Long trainingId,
                                                    Long classroomId,
                                                    String deskStatus,
                                                    String progressStatus,
                                                    BigDecimal score,
                                                    BigDecimal teamScore,
                                                    LocalDateTime eventTime) {
        assertScore(score, "Score is invalid");
        assertScore(teamScore, "Team score is invalid");
        TrainingMonitorSnapshotCommand snapshot = new TrainingMonitorSnapshotCommand();
        snapshot.setTrainingId(trainingId);
        snapshot.setStudentId(studentId);
        snapshot.setClassroomId(classroomId);
        snapshot.setDeskStatus(deskStatus);
        snapshot.setProgressStatus(progressStatus);
        snapshot.setScore(score);
        snapshot.setTeamScore(teamScore);
        snapshot.setLastEventAt(eventTime == null ? now() : eventTime);
        return snapshot;
    }

    private void validateSteps(List<TrainingAttemptStepCommand> steps) {
        if (steps == null) {
            return;
        }
        for (TrainingAttemptStepCommand step : steps) {
            if (step == null || !InputValidator.hasText(step.getStepName())) {
                throw new BusinessException(400, "Step name is required");
            }
            assertScore(step.getScore(), "Step score is invalid");
            assertNonNegative(step.getDurationSeconds(), "Step duration seconds is invalid");
            assertNonNegative(step.getVideoStartSecond(), "Step video start second is invalid");
        }
    }

    private String normalizeStatus(String value, String defaultValue, Set<String> allowed, String message) {
        String normalized = InputValidator.hasText(value) ? value.trim().toUpperCase(Locale.ENGLISH) : defaultValue;
        if (!allowed.contains(normalized)) {
            throw new BusinessException(400, message);
        }
        return normalized;
    }

    private void assertScore(BigDecimal score, String message) {
        if (score != null && (score.compareTo(MIN_SCORE) < 0 || score.compareTo(MAX_SCORE) > 0)) {
            throw new BusinessException(400, message);
        }
    }

    private void assertNonNegative(Integer value, String message) {
        if (value != null && value.intValue() < 0) {
            throw new BusinessException(400, message);
        }
    }

    private void requireId(Long value, String message) {
        if (value == null || value.longValue() <= 0L) {
            throw new BusinessException(400, message);
        }
    }

    private Integer defaultInteger(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
