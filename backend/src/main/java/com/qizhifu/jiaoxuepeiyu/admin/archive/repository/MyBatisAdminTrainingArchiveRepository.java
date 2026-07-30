package com.qizhifu.jiaoxuepeiyu.admin.archive.repository;

import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStep;
import com.qizhifu.jiaoxuepeiyu.admin.archive.port.AdminTrainingArchiveRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminTrainingArchiveRepository implements AdminTrainingArchiveRepository {

    private final AdminTrainingArchiveMapper mapper;

    public MyBatisAdminTrainingArchiveRepository(AdminTrainingArchiveMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminTrainingArchive> findArchives(AdminTrainingArchiveQuery query) {
        return mapper.findArchives(likeQuery(query));
    }

    @Override
    public long countArchives(AdminTrainingArchiveQuery query) {
        return mapper.countArchives(likeQuery(query));
    }

    @Override
    public AdminTrainingArchiveDetail findArchiveDetail(Long archiveId) {
        return mapper.findArchiveDetail(archiveId);
    }

    @Override
    public List<AdminTrainingArchiveStep> findArchiveSteps(Long archiveId) {
        return mapper.findArchiveSteps(archiveId);
    }

    @Override
    public AdminTrainingArchiveStatistics calculateStatistics(AdminTrainingArchiveQuery query) {
        return mapper.calculateStatistics(likeQuery(query));
    }

    private AdminTrainingArchiveQuery likeQuery(AdminTrainingArchiveQuery source) {
        AdminTrainingArchiveQuery query = new AdminTrainingArchiveQuery();
        query.setTrainingId(source.getTrainingId());
        query.setStudentId(source.getStudentId());
        query.setClassId(source.getClassId());
        query.setTrainingMode(source.getTrainingMode());
        query.setSubmitType(source.getSubmitType());
        query.setKeyword(source.getKeyword() == null ? null : "%" + source.getKeyword() + "%");
        query.setSubmittedStartTime(source.getSubmittedStartTime());
        query.setSubmittedEndExclusiveTime(source.getSubmittedEndExclusiveTime());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }
}
