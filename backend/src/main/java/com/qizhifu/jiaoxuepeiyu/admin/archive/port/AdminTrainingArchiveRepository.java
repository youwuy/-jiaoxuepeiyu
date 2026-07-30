package com.qizhifu.jiaoxuepeiyu.admin.archive.port;

import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStep;
import java.util.List;

public interface AdminTrainingArchiveRepository {

    List<AdminTrainingArchive> findArchives(AdminTrainingArchiveQuery query);

    long countArchives(AdminTrainingArchiveQuery query);

    AdminTrainingArchiveDetail findArchiveDetail(Long archiveId);

    List<AdminTrainingArchiveStep> findArchiveSteps(Long archiveId);

    AdminTrainingArchiveStatistics calculateStatistics(AdminTrainingArchiveQuery query);
}
