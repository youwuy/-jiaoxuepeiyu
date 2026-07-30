package com.qizhifu.jiaoxuepeiyu.admin.exam.port;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import java.util.Collections;
import java.util.List;

public interface AdminPaperRepository {

    List<AdminPaper> findPapers(AdminPaperQuery query);

    long countPapers(AdminPaperQuery query);

    AdminPaper findPaper(Long paperId);

    List<AdminQuestion> findQuestionsByIds(List<Long> questionIds);

    List<AdminQuestion> findEnabledQuestionsByType(String questionType, int limit);

    Long createPaper(AdminPaperCommand command, Long creatorId, Integer totalScore);

    void updatePaper(Long paperId, AdminPaperCommand command, Integer totalScore);

    void updatePaperPublishStatus(Long paperId, String publishStatus);

    void appendPaperLog(Long paperId, Long operatorId, String action, String content);

    default List<AdminPaperLog> findPaperLogs(Long paperId) {
        return Collections.emptyList();
    }
}
