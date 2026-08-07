package com.qizhifu.jiaoxuepeiyu.admin.exam.repository;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminQuestionRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminQuestionRepository implements AdminQuestionRepository {

    private final AdminQuestionMapper mapper;

    public MyBatisAdminQuestionRepository(AdminQuestionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminQuestion> findQuestions(AdminQuestionQuery query) {
        return mapper.findQuestions(likeQuery(query));
    }

    @Override
    public long countQuestions(AdminQuestionQuery query) {
        return mapper.countQuestions(likeQuery(query));
    }

    @Override
    public AdminQuestion findQuestion(Long questionId) {
        return mapper.findQuestion(questionId);
    }

    @Override
    public Long createQuestion(AdminQuestionCommand command, Long creatorId) {
        AdminQuestion question = toQuestion(null, command, creatorId, true);
        mapper.insertQuestion(question);
        replaceOptions(question.getQuestionId(), command.getOptions());
        return question.getQuestionId();
    }

    @Override
    public void updateQuestion(Long questionId, AdminQuestionCommand command) {
        AdminQuestion existing = mapper.findQuestion(questionId);
        Long creatorId = existing == null ? null : existing.getCreatorId();
        Boolean enabled = existing == null ? Boolean.TRUE : existing.getEnabled();
        mapper.updateQuestion(toQuestion(questionId, command, creatorId, Boolean.TRUE.equals(enabled)));
        replaceOptions(questionId, command.getOptions());
    }

    @Override
    public void updateQuestionStatus(Long questionId, boolean enabled) {
        mapper.updateQuestionStatus(questionId, enabled);
    }

    @Override
    public void appendQuestionLog(Long questionId, Long operatorId, String action, String content) {
        mapper.insertQuestionLog(questionId, operatorId, action, content);
    }

    @Override
    public List<AdminQuestionLog> findQuestionLogs(Long questionId) {
        return mapper.findQuestionLogs(questionId);
    }

    private void replaceOptions(Long questionId, List<AdminQuestionOption> options) {
        mapper.deleteQuestionOptions(questionId);
        List<AdminQuestionOption> safeOptions = options == null ? Collections.<AdminQuestionOption>emptyList() : options;
        for (AdminQuestionOption option : safeOptions) {
            mapper.insertQuestionOption(questionId, option);
        }
    }

    private AdminQuestion toQuestion(Long questionId, AdminQuestionCommand command, Long creatorId, boolean enabled) {
        AdminQuestion question = new AdminQuestion();
        question.setQuestionId(questionId);
        question.setQuestionType(command.getQuestionType());
        question.setTitle(command.getTitle());
        question.setStandardAnswer(command.getStandardAnswer());
        question.setExplanation(command.getExplanation());
        question.setScore(command.getScore());
        question.setEnabled(Boolean.valueOf(enabled));
        question.setCreatorId(creatorId);
        return question;
    }

    private AdminQuestionQuery likeQuery(AdminQuestionQuery source) {
        AdminQuestionQuery query = new AdminQuestionQuery();
        query.setKeyword(like(source.getKeyword()));
        query.setQuestionType(source.getQuestionType());
        query.setEnabled(source.getEnabled());
        query.setCreatorId(source.getCreatorId());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
