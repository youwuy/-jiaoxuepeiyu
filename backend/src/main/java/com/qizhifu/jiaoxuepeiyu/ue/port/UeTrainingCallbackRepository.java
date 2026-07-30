package com.qizhifu.jiaoxuepeiyu.ue.port;

import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import java.util.Optional;

public interface UeTrainingCallbackRepository {

    Optional<TrainingLaunchTask> findTask(Long trainingId, Long studentId);

    void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command);

    Long insertAttempt(TrainingAttemptSubmission submission);

    void insertAttemptStep(Long attemptId, TrainingAttemptStepCommand step, int sortOrder);
}
