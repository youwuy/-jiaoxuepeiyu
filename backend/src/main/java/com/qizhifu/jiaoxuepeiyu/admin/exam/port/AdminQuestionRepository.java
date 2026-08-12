package com.qizhifu.jiaoxuepeiyu.admin.exam.port;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
import java.util.Collections;
import java.util.List;

public interface AdminQuestionRepository {

    List<AdminQuestion> findQuestions(AdminQuestionQuery query);

    long countQuestions(AdminQuestionQuery query);

    AdminQuestion findQuestion(Long questionId);

    Long createQuestion(AdminQuestionCommand command, Long creatorId);

    void updateQuestion(Long questionId, AdminQuestionCommand command);

    void updateQuestionStatus(Long questionId, boolean enabled);

    default void deleteQuestion(Long questionId) {
    }

    void appendQuestionLog(Long questionId, Long operatorId, String action, String content);

    default List<AdminQuestionLog> findQuestionLogs(Long questionId) {
        return Collections.emptyList();
    }
}
