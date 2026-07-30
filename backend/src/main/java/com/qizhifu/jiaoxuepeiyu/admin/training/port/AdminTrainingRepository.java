package com.qizhifu.jiaoxuepeiyu.admin.training.port;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import java.util.List;

public interface AdminTrainingRepository {

    List<AdminTraining> findTrainings(AdminTrainingQuery query);

    long countTrainings(AdminTrainingQuery query);

    AdminTraining findTraining(Long trainingId);

    Long createTraining(AdminTrainingCommand command, Long creatorId);

    void updateTraining(Long trainingId, AdminTrainingCommand command);

    int countEnabledStudentsByTrainingClasses(Long trainingId);

    void syncParticipants(Long trainingId);

    void updatePublishStatus(Long trainingId, String publishStatus);

    void deleteTraining(Long trainingId);

    void notifyParticipants(Long trainingId, String title, String content);

    AdminTrainingStatistics calculateStatistics(Long trainingId);

    AdminTrainingMonitorSnapshot getMonitorSnapshot(Long trainingId);

    void appendTrainingLog(Long trainingId, Long operatorId, String action, String content);

    List<AdminTrainingLog> findTrainingLogs(Long trainingId);
}
