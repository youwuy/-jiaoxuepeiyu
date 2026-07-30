package com.qizhifu.jiaoxuepeiyu.admin.assignment.port;

import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewLog;
import java.util.Collections;
import java.util.List;

public interface AdminAssignmentReviewRepository {

    List<AdminAssignmentAttempt> findAttempts(AdminAssignmentAttemptQuery query);

    long countAttempts(AdminAssignmentAttemptQuery query);

    AdminAssignmentAttempt findAttempt(Long attemptId);

    void updateAnswerScore(Long attemptId, Long questionId, Integer score, String comment);

    void markReviewed(Long attemptId, Integer score, String reviewComment, Long reviewerId);

    void refreshCourseProgress(Long attemptId);

    void appendReviewLog(Long attemptId, Long reviewerId, String action, String content);

    default List<AdminAssignmentReviewLog> findReviewLogs(Long attemptId) {
        return Collections.emptyList();
    }
}
