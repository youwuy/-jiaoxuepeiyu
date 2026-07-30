package com.qizhifu.jiaoxuepeiyu.admin.assignment.repository;

import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewLog;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.port.AdminAssignmentReviewRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminAssignmentReviewRepository implements AdminAssignmentReviewRepository {

    private final AdminAssignmentReviewMapper mapper;

    public MyBatisAdminAssignmentReviewRepository(AdminAssignmentReviewMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminAssignmentAttempt> findAttempts(AdminAssignmentAttemptQuery query) {
        return mapper.findAttempts(likeQuery(query));
    }

    @Override
    public long countAttempts(AdminAssignmentAttemptQuery query) {
        return mapper.countAttempts(likeQuery(query));
    }

    @Override
    public AdminAssignmentAttempt findAttempt(Long attemptId) {
        return mapper.findAttempt(attemptId);
    }

    @Override
    public void updateAnswerScore(Long attemptId, Long questionId, Integer score, String comment) {
        mapper.updateAnswerScore(attemptId, questionId, score, comment);
    }

    @Override
    public void markReviewed(Long attemptId, Integer score, String reviewComment, Long reviewerId) {
        mapper.markReviewed(attemptId, score, reviewComment, reviewerId);
    }

    @Override
    public void refreshCourseProgress(Long attemptId) {
        mapper.refreshCourseProgress(attemptId);
    }

    @Override
    public void appendReviewLog(Long attemptId, Long reviewerId, String action, String content) {
        mapper.insertReviewLog(attemptId, reviewerId, action, content);
    }

    @Override
    public List<AdminAssignmentReviewLog> findReviewLogs(Long attemptId) {
        return mapper.findReviewLogs(attemptId);
    }

    private AdminAssignmentAttemptQuery likeQuery(AdminAssignmentAttemptQuery source) {
        AdminAssignmentAttemptQuery query = new AdminAssignmentAttemptQuery();
        query.setCourseId(source.getCourseId());
        query.setAssignmentId(source.getAssignmentId());
        query.setClassId(source.getClassId());
        query.setStudentId(source.getStudentId());
        query.setStatus(source.getStatus());
        query.setKeyword(like(source.getKeyword()));
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
