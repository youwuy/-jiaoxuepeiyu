package com.qizhifu.jiaoxuepeiyu.ue.port;

import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeScoreWeight;
import java.math.BigDecimal;
import java.util.Optional;

public interface UeTrainingCallbackRepository {

    Optional<TrainingLaunchTask> findTask(Long trainingId, Long studentId, Long topicId);

    default Optional<Long> findAttemptId(Long studentId, Long trainingId, String clientAttemptId) {
        return Optional.empty();
    }

    default boolean topicBelongsToTraining(Long trainingId, Long topicId) { return false; }

    void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command);

    Long insertAttempt(TrainingAttemptSubmission submission);

    void insertAttemptStep(Long attemptId, TrainingAttemptStepCommand step, int sortOrder);

    Optional<Long> findCurrentSemesterId();

    UeScoreWeight findLatestScoreWeight(Long semesterId);

    void upsertTrainingPracticeScore(Long studentId,
                                     Long semesterId,
                                     BigDecimal trainingPracticeScore,
                                     UeScoreWeight weight);
}
