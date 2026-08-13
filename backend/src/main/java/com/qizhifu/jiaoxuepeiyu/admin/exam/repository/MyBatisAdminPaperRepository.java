package com.qizhifu.jiaoxuepeiyu.admin.exam.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminPaperRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminPaperRepository implements AdminPaperRepository {

    private final AdminPaperMapper mapper;
    private final ObjectMapper objectMapper;

    public MyBatisAdminPaperRepository(AdminPaperMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AdminPaper> findPapers(AdminPaperQuery query) {
        List<AdminPaper> papers = mapper.findPapers(likeQuery(query));
        for (AdminPaper paper : papers) {
            hydrateQuestionOptions(paper);
        }
        return papers;
    }

    @Override
    public long countPapers(AdminPaperQuery query) {
        return mapper.countPapers(likeQuery(query));
    }

    @Override
    public AdminPaper findPaper(Long paperId) {
        AdminPaper paper = mapper.findPaper(paperId);
        hydrateQuestionOptions(paper);
        return paper;
    }

    @Override
    public List<AdminQuestion> findQuestionsByIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.findQuestionsByIds(questionIds);
    }

    @Override
    public List<AdminQuestion> findEnabledQuestionsByType(String questionType, int limit) {
        return mapper.findEnabledQuestionsByType(questionType, limit);
    }

    @Override
    public Long createPaper(AdminPaperCommand command, Long creatorId, Integer totalScore) {
        AdminPaper paper = toPaper(null, command, creatorId, totalScore, "DRAFT");
        mapper.insertPaper(paper);
        replacePaperQuestions(paper.getPaperId(), command.getQuestions());
        return paper.getPaperId();
    }

    @Override
    public void updatePaper(Long paperId, AdminPaperCommand command, Integer totalScore) {
        AdminPaper existing = mapper.findPaper(paperId);
        Long creatorId = existing == null ? null : existing.getCreatorId();
        String publishStatus = existing == null ? "DRAFT" : existing.getPublishStatus();
        mapper.updatePaper(toPaper(paperId, command, creatorId, totalScore, publishStatus));
        replacePaperQuestions(paperId, command.getQuestions());
    }

    @Override
    public void updatePaperPublishStatus(Long paperId, String publishStatus) {
        mapper.updatePaperPublishStatus(paperId, publishStatus);
    }

    @Override
    public void appendPaperLog(Long paperId, Long operatorId, String action, String content) {
        mapper.insertPaperLog(paperId, operatorId, action, content);
    }

    @Override
    public List<AdminPaperLog> findPaperLogs(Long paperId) {
        return mapper.findPaperLogs(paperId);
    }

    private void replacePaperQuestions(Long paperId, List<AdminPaperQuestionCommand> questionCommands) {
        mapper.deletePaperQuestions(paperId);
        List<Long> ids = questionIds(questionCommands);
        Map<Long, AdminQuestion> questions = questionMap(mapper.findQuestionsByIds(ids));
        int sortOrder = 1;
        for (AdminPaperQuestionCommand command : questionCommands) {
            AdminQuestion source = questions.get(command.getQuestionId());
            AdminPaperQuestion question = new AdminPaperQuestion();
            question.setQuestionId(command.getQuestionId());
            question.setQuestionType(source == null ? null : source.getQuestionType());
            question.setTitle(source == null ? null : source.getTitle());
            question.setStandardAnswer(source == null ? null : source.getStandardAnswer());
            question.setOptionsJson(optionsJson(source == null ? null : source.getOptions()));
            question.setScore(command.getScore());
            question.setSortOrder(Integer.valueOf(sortOrder++));
            mapper.insertPaperQuestion(paperId, question);
        }
    }

    private AdminPaper toPaper(Long paperId, AdminPaperCommand command, Long creatorId,
                               Integer totalScore, String publishStatus) {
        AdminPaper paper = new AdminPaper();
        paper.setPaperId(paperId);
        paper.setPaperName(command.getPaperName());
        paper.setCourseName(command.getCourseName());
        paper.setComposeMode(command.getComposeMode());
        paper.setTotalScore(totalScore);
        paper.setQuestionCount(Integer.valueOf(command.getQuestions() == null ? 0 : command.getQuestions().size()));
        paper.setPublishStatus(publishStatus);
        paper.setCreatorId(creatorId);
        return paper;
    }

    private List<Long> questionIds(List<AdminPaperQuestionCommand> questionCommands) {
        List<Long> ids = new ArrayList<Long>();
        if (questionCommands == null) {
            return ids;
        }
        for (AdminPaperQuestionCommand command : questionCommands) {
            ids.add(command.getQuestionId());
        }
        return ids;
    }

    private Map<Long, AdminQuestion> questionMap(List<AdminQuestion> questions) {
        Map<Long, AdminQuestion> byId = new HashMap<Long, AdminQuestion>();
        if (questions == null) {
            return byId;
        }
        for (AdminQuestion question : questions) {
            byId.put(question.getQuestionId(), question);
        }
        return byId;
    }

    private String optionsJson(List<AdminQuestionOption> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(500, "Question options cannot be serialized");
        }
    }

    private void hydrateQuestionOptions(AdminPaper paper) {
        if (paper == null || paper.getQuestions() == null) {
            return;
        }
        for (AdminPaperQuestion question : paper.getQuestions()) {
            question.setOptions(optionsFromJson(question.getOptionsJson()));
        }
    }

    private List<AdminQuestionOption> optionsFromJson(String optionsJson) {
        if (optionsJson == null || optionsJson.length() == 0) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<AdminQuestionOption>>() {
            });
        } catch (IOException exception) {
            return Collections.emptyList();
        }
    }

    private AdminPaperQuery likeQuery(AdminPaperQuery source) {
        AdminPaperQuery query = new AdminPaperQuery();
        query.setKeyword(like(source.getKeyword()));
        query.setCourseName(like(source.getCourseName()));
        query.setComposeMode(source.getComposeMode());
        query.setPublishStatus(source.getPublishStatus());
        query.setCreatorId(source.getCreatorId());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
