package com.qizhifu.jiaoxuepeiyu.admin.training.port;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingWeakStep;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingWeakTopic;
import java.util.List;
import java.util.Map;

public interface AdminTrainingRepository {

    List<AdminTraining> findTrainings(AdminTrainingQuery query);

    long countTrainings(AdminTrainingQuery query);

    AdminTraining findTraining(Long trainingId);

    boolean roleBelongsToTopic(Long topicId, String roleName);

    Long createTraining(AdminTrainingCommand command, Long creatorId);

    void updateTraining(Long trainingId, AdminTrainingCommand command);

    int countEnabledStudentsByTrainingClasses(Long trainingId);

    void syncParticipants(Long trainingId);

    void updatePublishStatus(Long trainingId, String publishStatus);

    void markExamStarted(Long trainingId);

    void deleteTraining(Long trainingId);

    void notifyParticipants(Long trainingId, String title, String content);

    AdminTrainingStatistics calculateStatistics(Long trainingId);

    List<AdminTrainingWeakStep> findWeakSteps(Long trainingId, String className);

    List<AdminTrainingWeakTopic> findWeakTopics(Long trainingId, String className);

    AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId);

    boolean dissolveRoom(Long trainingId, Long roomId);

    List<Map<String, Object>> findReviewRows(Long trainingId);

    List<Map<String, Object>> findReviewAttempts(Long trainingId, Long studentId, Long topicId);

    Double findAttemptMaxScore(Long trainingId, Long attemptId);

    boolean reviewAttempt(Long trainingId, Long attemptId, Double manualScore, String comment, Long reviewerId);

    void appendTrainingLog(Long trainingId, Long operatorId, String action, String content);

    List<AdminTrainingLog> findTrainingLogs(Long trainingId);
}
