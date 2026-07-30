package com.qizhifu.jiaoxuepeiyu.admin.score.repository;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.score.port.AdminSemesterScoreRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminSemesterScoreRepository implements AdminSemesterScoreRepository {

    private final AdminSemesterScoreMapper mapper;

    public MyBatisAdminSemesterScoreRepository(AdminSemesterScoreMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminSemesterScore> findScores(AdminSemesterScoreQuery query) {
        return mapper.findScores(likeQuery(query));
    }

    @Override
    public long countScores(AdminSemesterScoreQuery query) {
        return mapper.countScores(likeQuery(query));
    }

    @Override
    public AdminSemesterScoreStatistics calculateStatistics(AdminSemesterScoreQuery query) {
        return mapper.calculateStatistics(likeQuery(query));
    }

    @Override
    public List<AdminSemesterScore> findRanking(AdminSemesterScoreQuery query) {
        AdminSemesterScoreQuery normalized = likeQuery(query);
        normalized.setPage(1);
        normalized.setPageSize(query.getPageSize());
        return mapper.findRanking(normalized);
    }

    private AdminSemesterScoreQuery likeQuery(AdminSemesterScoreQuery source) {
        AdminSemesterScoreQuery query = new AdminSemesterScoreQuery();
        query.setSemesterId(source.getSemesterId());
        query.setClassId(source.getClassId());
        query.setMajorId(source.getMajorId());
        query.setStudentId(source.getStudentId());
        query.setKeyword(source.getKeyword() == null ? null : "%" + source.getKeyword() + "%");
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }
}
