package com.qizhifu.jiaoxuepeiyu.ue.repository;

import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptStepCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptSubmission;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingMonitorSnapshotCommand;
import com.qizhifu.jiaoxuepeiyu.ue.port.UeTrainingCallbackRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisUeTrainingCallbackRepository implements UeTrainingCallbackRepository {

    private final UeTrainingCallbackMapper mapper;

    public MyBatisUeTrainingCallbackRepository(UeTrainingCallbackMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<TrainingLaunchTask> findTask(Long trainingId, Long studentId) {
        TrainingLaunchTask task = mapper.findTask(trainingId, studentId);
        if (task != null && task.getRoomId() != null) {
            task.setAiRoleNames(mapper.findUnclaimedRoleNames(task.getRoomId()));
        }
        return Optional.ofNullable(task);
    }

    @Override
    public void upsertMonitorSnapshot(TrainingMonitorSnapshotCommand command) {
        mapper.upsertMonitorSnapshot(command);
    }

    @Override
    public Long insertAttempt(TrainingAttemptSubmission submission) {
        mapper.insertAttempt(submission);
        return submission.getAttemptId();
    }

    @Override
    public void insertAttemptStep(Long attemptId, TrainingAttemptStepCommand step, int sortOrder) {
        mapper.insertAttemptStep(attemptId, step, defaultInteger(step.getDurationSeconds()),
                defaultInteger(step.getVideoStartSecond()), sortOrder);
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value.intValue();
    }
}
