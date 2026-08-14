package com.qizhifu.jiaoxuepeiyu.admin.exam;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperAutoRule;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportError;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminPaperRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPaperService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> QUESTION_TYPES = new HashSet<String>(
            Arrays.asList("SINGLE", "MULTIPLE", "JUDGE", "FILL_BLANK", "SHORT_ANSWER"));

    private final AdminPaperRepository repository;

    public AdminPaperService(AdminPaperRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminPaper> listPapers(AdminPaperQuery query) {
        AdminPaperQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminPaper>(
                repository.findPapers(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countPapers(normalized));
    }

    public AdminPaper getPaper(Long paperId) {
        AdminPaper paper = repository.findPaper(paperId);
        if (paper == null) {
            throw new BusinessException(404, "Paper not found");
        }
        return paper;
    }

    public AdminPaperPreview previewPaper(AdminPaperCommand command) {
        AdminPaperCommand normalized = normalizedPaper(command);
        List<Long> questionIds = new ArrayList<Long>();
        for (AdminPaperQuestionCommand question : normalized.getQuestions()) {
            questionIds.add(question.getQuestionId());
        }
        Map<Long, AdminQuestion> sources = new LinkedHashMap<Long, AdminQuestion>();
        for (AdminQuestion question : repository.findQuestionsByIds(questionIds)) {
            sources.put(question.getQuestionId(), question);
        }
        List<AdminPaperQuestion> questions = new ArrayList<AdminPaperQuestion>();
        int sortOrder = 1;
        for (AdminPaperQuestionCommand commandQuestion : normalized.getQuestions()) {
            AdminQuestion source = sources.get(commandQuestion.getQuestionId());
            if (source == null) {
                throw new BusinessException(400, "Paper contains disabled or missing questions");
            }
            AdminPaperQuestion question = new AdminPaperQuestion();
            question.setQuestionId(source.getQuestionId());
            question.setQuestionType(source.getQuestionType());
            question.setTitle(source.getTitle());
            question.setStandardAnswer(source.getStandardAnswer());
            question.setOptions(source.getOptions());
            question.setScore(commandQuestion.getScore());
            question.setSortOrder(Integer.valueOf(sortOrder++));
            questions.add(question);
        }
        AdminPaperPreview preview = new AdminPaperPreview();
        preview.setPaperName(normalized.getPaperName());
        preview.setCourseName(normalized.getCourseName());
        preview.setComposeMode(normalized.getComposeMode());
        preview.setTotalScore(Integer.valueOf(totalScore(normalized.getQuestions())));
        preview.setQuestions(questions);
        return preview;
    }

    @Transactional
    public Long createPaper(AdminPaperCommand command, Long creatorId) {
        requireOperator(creatorId);
        AdminPaperCommand normalized = normalizedPaper(command);
        Integer totalScore = Integer.valueOf(totalScore(normalized.getQuestions()));
        Long paperId = repository.createPaper(normalized, creatorId, totalScore);
        repository.appendPaperLog(paperId, creatorId, "CREATE", "Create paper");
        return paperId;
    }

    @Transactional
    public void updatePaper(Long paperId, AdminPaperCommand command, Long operatorId) {
        requireOperator(operatorId);
        getPaper(paperId);
        AdminPaperCommand normalized = normalizedPaper(command);
        Integer totalScore = Integer.valueOf(totalScore(normalized.getQuestions()));
        repository.updatePaper(paperId, normalized, totalScore);
        repository.appendPaperLog(paperId, operatorId, "UPDATE", "Update paper");
    }

    @Transactional
    public void publishPaper(Long paperId, Long operatorId) {
        requireOperator(operatorId);
        AdminPaper paper = getPaper(paperId);
        if (paper.getQuestionCount() == null || paper.getQuestionCount().intValue() <= 0) {
            throw new BusinessException(400, "Paper must contain questions before publishing");
        }
        repository.updatePaperPublishStatus(paperId, "PUBLISHED");
        repository.appendPaperLog(paperId, operatorId, "PUBLISH", "Publish paper");
    }

    @Transactional
    public void cancelPublishPaper(Long paperId, Long operatorId) {
        requireOperator(operatorId);
        getPaper(paperId);
        repository.updatePaperPublishStatus(paperId, "OFFLINE");
        repository.appendPaperLog(paperId, operatorId, "CANCEL_PUBLISH", "Cancel paper publish");
    }

    public List<AdminPaperLog> listPaperLogs(Long paperId) {
        getPaper(paperId);
        return repository.findPaperLogs(paperId);
    }

    public AdminPaperImportPreview previewImport(AdminPaperImportCommand command) {
        AdminPaperImportCommand normalized = normalizedImport(command);
        AdminPaperImportPreview preview = new AdminPaperImportPreview();
        preview.setFileName(normalized.getFileName());
        preview.setFileSize(normalized.getFileSize());
        List<AdminPaperImportRow> validRows = new ArrayList<AdminPaperImportRow>();
        List<AdminPaperImportError> errors = new ArrayList<AdminPaperImportError>();
        for (AdminPaperImportRow row : normalized.getRows()) {
            try {
                normalizedPaper(commandFromRow(row));
                validRows.add(row);
            } catch (BusinessException exception) {
                errors.add(new AdminPaperImportError(row == null ? null : row.getRowNumber(), exception.getMessage()));
            }
        }
        preview.setValidRows(validRows);
        preview.setErrors(errors);
        preview.setValidCount(Integer.valueOf(validRows.size()));
        preview.setErrorCount(Integer.valueOf(errors.size()));
        return preview;
    }

    private AdminPaperCommand normalizedPaper(AdminPaperCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Paper data is required");
        }
        String paperName = trimToNull(command.getPaperName());
        if (paperName == null) {
            throw new BusinessException(400, "Paper name is required");
        }
        if (paperName.length() > 30) {
            throw new BusinessException(400, "Paper name cannot exceed 30 characters");
        }
        String courseName = trimToNull(command.getCourseName());
        if (courseName != null && courseName.length() > 30) {
            throw new BusinessException(400, "Paper course name cannot exceed 30 characters");
        }
        String composeMode = upper(trimToNull(command.getComposeMode()));
        if (!"MANUAL".equals(composeMode) && !"AUTO".equals(composeMode)) {
            throw new BusinessException(400, "Paper compose mode is invalid");
        }
        AdminPaperCommand normalized = new AdminPaperCommand();
        normalized.setPaperName(paperName);
        normalized.setCourseName(courseName);
        normalized.setComposeMode(composeMode);
        if ("AUTO".equals(composeMode)) {
            List<AdminPaperAutoRule> autoRules = validatedAutoRules(command.getAutoRules());
            normalized.setAutoRules(autoRules);
            normalized.setQuestions(command.getQuestions() == null || command.getQuestions().isEmpty()
                    ? buildAutoQuestions(autoRules)
                    : normalizedManualQuestions(command.getQuestions()));
        } else {
            normalized.setQuestions(normalizedManualQuestions(command.getQuestions()));
        }
        return normalized;
    }

    private List<AdminPaperQuestionCommand> normalizedManualQuestions(List<AdminPaperQuestionCommand> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException(400, "Paper must contain questions before publishing");
        }
        List<AdminPaperQuestionCommand> copiedQuestions = copyPaperQuestions(questions);
        List<Long> questionIds = new ArrayList<Long>();
        Set<Long> idSet = new HashSet<Long>();
        for (AdminPaperQuestionCommand question : copiedQuestions) {
            Long questionId = question == null ? null : question.getQuestionId();
            if (questionId == null || questionId.longValue() <= 0) {
                throw new BusinessException(400, "Paper question id is required");
            }
            if (!idSet.add(questionId)) {
                throw new BusinessException(400, "Paper questions cannot repeat");
            }
            questionIds.add(questionId);
        }
        List<AdminQuestion> existingQuestions = repository.findQuestionsByIds(questionIds);
        if (!allEnabledQuestionsExist(questionIds, existingQuestions)) {
            throw new BusinessException(400, "Paper contains disabled or missing questions");
        }
        return copiedQuestions;
    }

    private List<AdminPaperQuestionCommand> buildAutoQuestions(List<AdminPaperAutoRule> rules) {
        List<AdminPaperQuestionCommand> questions = new ArrayList<AdminPaperQuestionCommand>();
        Set<Long> selectedIds = new HashSet<Long>();
        for (AdminPaperAutoRule rule : rules) {
            String questionType = upper(trimToNull(rule.getQuestionType()));
            List<AdminQuestion> pool = repository.findEnabledQuestionsByType(questionType, rule.getQuestionCount().intValue());
            List<AdminQuestion> selected = selectUniqueEnabled(pool, selectedIds, rule.getQuestionCount().intValue());
            if (selected.size() < rule.getQuestionCount().intValue()) {
                throw new BusinessException(400, "Not enough enabled questions for auto paper");
            }
            for (AdminQuestion question : selected) {
                AdminPaperQuestionCommand questionCommand = new AdminPaperQuestionCommand();
                questionCommand.setQuestionId(question.getQuestionId());
                questionCommand.setScore(rule.getScorePerQuestion());
                questions.add(questionCommand);
            }
        }
        return questions;
    }

    private List<AdminPaperAutoRule> validatedAutoRules(List<AdminPaperAutoRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new BusinessException(400, "Auto paper rules are required");
        }
        List<AdminPaperAutoRule> normalized = new ArrayList<AdminPaperAutoRule>();
        for (AdminPaperAutoRule rule : rules) {
            String questionType = upper(trimToNull(rule == null ? null : rule.getQuestionType()));
            if (!QUESTION_TYPES.contains(questionType)) {
                throw new BusinessException(400, "Auto rule question type is invalid");
            }
            if (rule.getQuestionCount() == null || rule.getQuestionCount().intValue() <= 0) {
                throw new BusinessException(400, "Auto rule question count must be greater than 0");
            }
            if (rule.getScorePerQuestion() == null || rule.getScorePerQuestion().intValue() <= 0) {
                throw new BusinessException(400, "Auto rule score must be greater than 0");
            }
            AdminPaperAutoRule normalizedRule = new AdminPaperAutoRule();
            normalizedRule.setQuestionType(questionType);
            normalizedRule.setQuestionCount(rule.getQuestionCount());
            normalizedRule.setScorePerQuestion(rule.getScorePerQuestion());
            normalized.add(normalizedRule);
        }
        return normalized;
    }

    private List<AdminQuestion> selectUniqueEnabled(List<AdminQuestion> pool, Set<Long> selectedIds, int count) {
        List<AdminQuestion> selected = new ArrayList<AdminQuestion>();
        if (pool == null) {
            return selected;
        }
        for (AdminQuestion question : pool) {
            if (question == null || question.getQuestionId() == null || !Boolean.TRUE.equals(question.getEnabled())) {
                continue;
            }
            if (selectedIds.add(question.getQuestionId())) {
                selected.add(question);
            }
            if (selected.size() == count) {
                return selected;
            }
        }
        return selected;
    }

    private boolean allEnabledQuestionsExist(List<Long> questionIds, List<AdminQuestion> questions) {
        if (questions == null || questions.size() != questionIds.size()) {
            return false;
        }
        Set<Long> enabledIds = new HashSet<Long>();
        for (AdminQuestion question : questions) {
            if (question != null && Boolean.TRUE.equals(question.getEnabled())) {
                enabledIds.add(question.getQuestionId());
            }
        }
        return enabledIds.containsAll(questionIds);
    }

    private List<AdminPaperQuestionCommand> copyPaperQuestions(List<AdminPaperQuestionCommand> questions) {
        List<AdminPaperQuestionCommand> copied = new ArrayList<AdminPaperQuestionCommand>();
        for (AdminPaperQuestionCommand question : questions) {
            if (question == null || question.getScore() == null || question.getScore().intValue() <= 0) {
                throw new BusinessException(400, "Paper question score must be greater than 0");
            }
            AdminPaperQuestionCommand copiedQuestion = new AdminPaperQuestionCommand();
            copiedQuestion.setQuestionId(question.getQuestionId());
            copiedQuestion.setScore(question.getScore());
            copied.add(copiedQuestion);
        }
        return copied;
    }

    private int totalScore(List<AdminPaperQuestionCommand> questions) {
        int total = 0;
        for (AdminPaperQuestionCommand question : questions) {
            total += question.getScore().intValue();
        }
        return total;
    }

    private AdminPaperQuery normalizedQuery(AdminPaperQuery query) {
        AdminPaperQuery normalized = new AdminPaperQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setCourseName(trimToNull(query.getCourseName()));
            normalized.setComposeMode(upper(trimToNull(query.getComposeMode())));
            normalized.setPublishStatus(upper(trimToNull(query.getPublishStatus())));
            normalized.setCreatorId(query.getCreatorId());
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private AdminPaperImportCommand normalizedImport(AdminPaperImportCommand command) {
        if (command == null || !InputValidator.hasText(command.getFileName())) {
            throw new BusinessException(400, "Import file name is required");
        }
        String fileName = command.getFileName().trim();
        String lowerFileName = fileName.toLowerCase(Locale.ENGLISH);
        if (!lowerFileName.endsWith(".xls") && !lowerFileName.endsWith(".xlsx")
                && !lowerFileName.endsWith(".excel")) {
            throw new BusinessException(400, "Import file must be an Excel file");
        }
        if (command.getFileSize() == null || command.getFileSize().longValue() <= 0) {
            throw new BusinessException(400, "Import file size is required");
        }
        if (command.getFileSize().longValue() > 200L * 1024L * 1024L) {
            throw new BusinessException(400, "Import file cannot exceed 200MB");
        }
        if (command.getRows() == null || command.getRows().isEmpty()) {
            throw new BusinessException(400, "Import rows are required");
        }
        AdminPaperImportCommand normalized = new AdminPaperImportCommand();
        normalized.setFileName(fileName);
        normalized.setFileSize(command.getFileSize());
        normalized.setRows(command.getRows());
        return normalized;
    }

    private AdminPaperCommand commandFromRow(AdminPaperImportRow row) {
        AdminPaperCommand command = new AdminPaperCommand();
        command.setPaperName(row == null ? null : row.getPaperName());
        command.setComposeMode(row == null ? null : row.getComposeMode());
        command.setQuestions(row == null ? null : row.getQuestions());
        command.setAutoRules(row == null ? null : row.getAutoRules());
        return command;
    }

    private void requireOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ENGLISH);
    }
}
