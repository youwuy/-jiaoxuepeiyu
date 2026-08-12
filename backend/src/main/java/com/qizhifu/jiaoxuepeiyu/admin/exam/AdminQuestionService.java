package com.qizhifu.jiaoxuepeiyu.admin.exam;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportError;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminQuestionRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminQuestionService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> QUESTION_TYPES = new HashSet<String>(
            Arrays.asList("SINGLE", "MULTIPLE", "JUDGE", "FILL_BLANK", "SHORT_ANSWER"));

    private final AdminQuestionRepository repository;

    public AdminQuestionService(AdminQuestionRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminQuestion> listQuestions(AdminQuestionQuery query) {
        AdminQuestionQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminQuestion>(
                repository.findQuestions(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countQuestions(normalized));
    }

    public AdminQuestion getQuestion(Long questionId) {
        AdminQuestion question = repository.findQuestion(questionId);
        if (question == null) {
            throw new BusinessException(404, "Question not found");
        }
        return question;
    }

    @Transactional
    public Long createQuestion(AdminQuestionCommand command, Long creatorId) {
        requireOperator(creatorId);
        AdminQuestionCommand normalized = normalizedQuestion(command);
        Long questionId = repository.createQuestion(normalized, creatorId);
        repository.appendQuestionLog(questionId, creatorId, "CREATE", "Create question");
        return questionId;
    }

    @Transactional
    public void updateQuestion(Long questionId, AdminQuestionCommand command, Long operatorId) {
        requireOperator(operatorId);
        getQuestion(questionId);
        repository.updateQuestion(questionId, normalizedQuestion(command));
        repository.appendQuestionLog(questionId, operatorId, "UPDATE", "Update question");
    }

    @Transactional
    public void enableQuestion(Long questionId, Long operatorId) {
        updateStatus(questionId, true, operatorId, "ENABLE", "Enable question");
    }

    @Transactional
    public void disableQuestion(Long questionId, Long operatorId) {
        updateStatus(questionId, false, operatorId, "DISABLE", "Disable question");
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long operatorId) {
        requireOperator(operatorId);
        getQuestion(questionId);
        repository.deleteQuestion(questionId);
        repository.appendQuestionLog(questionId, operatorId, "DELETE", "Delete question");
    }

    public List<AdminQuestionLog> listQuestionLogs(Long questionId) {
        getQuestion(questionId);
        return repository.findQuestionLogs(questionId);
    }

    public AdminQuestionImportPreview previewImport(AdminQuestionImportCommand command) {
        AdminQuestionImportCommand normalized = normalizedImport(command);
        AdminQuestionImportPreview preview = new AdminQuestionImportPreview();
        preview.setFileName(normalized.getFileName());
        preview.setFileSize(normalized.getFileSize());
        List<AdminQuestionImportRow> validRows = new ArrayList<AdminQuestionImportRow>();
        List<AdminQuestionImportError> errors = new ArrayList<AdminQuestionImportError>();
        for (AdminQuestionImportRow row : normalized.getRows()) {
            try {
                normalizedQuestion(commandFromRow(row, normalized.getCourseName()));
                validRows.add(row);
            } catch (BusinessException exception) {
                errors.add(new AdminQuestionImportError(row == null ? null : row.getRowNumber(), exception.getMessage()));
            }
        }
        preview.setValidRows(validRows);
        preview.setErrors(errors);
        preview.setValidCount(Integer.valueOf(validRows.size()));
        preview.setErrorCount(Integer.valueOf(errors.size()));
        return preview;
    }

    @Transactional
    public int importQuestions(AdminQuestionImportCommand command, Long operatorId) {
        return importQuestionIds(command, operatorId).size();
    }

    @Transactional
    public List<Long> importQuestionIds(AdminQuestionImportCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminQuestionImportCommand normalized = normalizedImport(command);
        List<AdminQuestionCommand> questions = new ArrayList<AdminQuestionCommand>();
        List<AdminQuestionImportError> errors = new ArrayList<AdminQuestionImportError>();
        for (AdminQuestionImportRow row : normalized.getRows()) {
            try {
                questions.add(normalizedQuestion(commandFromRow(row, normalized.getCourseName())));
            } catch (BusinessException exception) {
                errors.add(new AdminQuestionImportError(row == null ? null : row.getRowNumber(), exception.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(400, "Import rows contain invalid questions");
        }
        List<Long> questionIds = new ArrayList<Long>();
        for (AdminQuestionCommand question : questions) {
            Long questionId = repository.createQuestion(question, operatorId);
            questionIds.add(questionId);
            repository.appendQuestionLog(questionId, operatorId, "IMPORT", "Import question from " + normalized.getFileName());
        }
        return questionIds;
    }

    private void updateStatus(Long questionId, boolean enabled, Long operatorId, String action, String content) {
        requireOperator(operatorId);
        getQuestion(questionId);
        repository.updateQuestionStatus(questionId, enabled);
        repository.appendQuestionLog(questionId, operatorId, action, content);
    }

    private AdminQuestionCommand normalizedQuestion(AdminQuestionCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Question data is required");
        }
        String questionType = upper(trimToNull(command.getQuestionType()));
        if (!QUESTION_TYPES.contains(questionType)) {
            throw new BusinessException(400, "Question type is invalid");
        }
        String title = trimToNull(command.getTitle());
        if (title == null) {
            throw new BusinessException(400, "Question title is required");
        }
        if (command.getScore() == null || command.getScore().intValue() <= 0) {
            throw new BusinessException(400, "Question score must be greater than 0");
        }
        String courseName = trimToNull(command.getCourseName());
        if (courseName == null) {
            throw new BusinessException(400, "Course name is required");
        }
        if (courseName.length() > 30) {
            throw new BusinessException(400, "Course name cannot exceed 30 characters");
        }
        String explanation = trimToNull(command.getExplanation());
        if (explanation == null) {
            throw new BusinessException(400, "Question explanation is required");
        }
        AdminQuestionCommand normalized = new AdminQuestionCommand();
        normalized.setQuestionType(questionType);
        normalized.setCourseName(courseName);
        normalized.setTitle(title);
        normalized.setScore(command.getScore());
        normalized.setExplanation(explanation);
        if ("SINGLE".equals(questionType) || "MULTIPLE".equals(questionType)) {
            List<AdminQuestionOption> options = normalizedOptions(command.getOptions());
            normalized.setOptions(options);
            normalized.setStandardAnswer(deriveChoiceAnswer(questionType, options));
            return normalized;
        }
        if ("JUDGE".equals(questionType)) {
            String answer = upper(trimToNull(command.getStandardAnswer()));
            if (!"TRUE".equals(answer) && !"FALSE".equals(answer)) {
                throw new BusinessException(400, "Judgment answer must be TRUE or FALSE");
            }
            normalized.setStandardAnswer(answer);
            return normalized;
        }
        String answer = trimToNull(command.getStandardAnswer());
        if (answer == null) {
            throw new BusinessException(400, "Standard answer is required");
        }
        if ("FILL_BLANK".equals(questionType)) {
            validateFillBlank(title, answer);
        }
        normalized.setStandardAnswer(answer);
        return normalized;
    }

    private void validateFillBlank(String title, String answer) {
        int blankCount = 0;
        boolean inBlank = false;
        for (int i = 0; i < title.length(); i++) {
            boolean underscore = title.charAt(i) == '_' || title.charAt(i) == '＿';
            if (underscore && !inBlank) {
                blankCount++;
            }
            inBlank = underscore;
        }
        if (blankCount == 0) {
            throw new BusinessException(400, "Fill blank title must contain underscore markers");
        }
        String[] answers = answer.split("[,，;；|]", -1);
        if (answers.length != blankCount) {
            throw new BusinessException(400, "Fill blank answer count must match blank markers");
        }
        for (String item : answers) {
            if (!InputValidator.hasText(item)) {
                throw new BusinessException(400, "Fill blank answers cannot be empty");
            }
        }
    }

    private List<AdminQuestionOption> normalizedOptions(List<AdminQuestionOption> options) {
        if (options == null || options.size() < 2) {
            throw new BusinessException(400, "Choice question must contain at least two options");
        }
        List<AdminQuestionOption> normalized = new ArrayList<AdminQuestionOption>();
        Set<String> optionKeys = new HashSet<String>();
        int sortOrder = 1;
        for (AdminQuestionOption option : options) {
            String optionKey = upper(trimToNull(option == null ? null : option.getOptionKey()));
            String optionText = trimToNull(option == null ? null : option.getOptionText());
            if (optionKey == null || optionText == null) {
                throw new BusinessException(400, "Choice option key and text are required");
            }
            if (!optionKeys.add(optionKey)) {
                throw new BusinessException(400, "Choice option keys cannot repeat");
            }
            AdminQuestionOption normalizedOption = new AdminQuestionOption();
            normalizedOption.setOptionKey(optionKey);
            normalizedOption.setOptionText(optionText);
            normalizedOption.setCorrect(Boolean.valueOf(Boolean.TRUE.equals(option.getCorrect())));
            normalizedOption.setSortOrder(Integer.valueOf(sortOrder++));
            normalized.add(normalizedOption);
        }
        return normalized;
    }

    private String deriveChoiceAnswer(String questionType, List<AdminQuestionOption> options) {
        List<String> correctKeys = new ArrayList<String>();
        for (AdminQuestionOption option : options) {
            if (Boolean.TRUE.equals(option.getCorrect())) {
                correctKeys.add(option.getOptionKey());
            }
        }
        if ("SINGLE".equals(questionType) && correctKeys.size() != 1) {
            throw new BusinessException(400, "Single choice must have exactly one correct option");
        }
        if ("MULTIPLE".equals(questionType) && correctKeys.size() < 2) {
            throw new BusinessException(400, "Multiple choice must have at least two correct options");
        }
        return join(correctKeys);
    }

    private AdminQuestionImportCommand normalizedImport(AdminQuestionImportCommand command) {
        if (command == null || !InputValidator.hasText(command.getFileName())) {
            throw new BusinessException(400, "Import file name is required");
        }
        String fileName = command.getFileName().trim();
        String lowerFileName = fileName.toLowerCase(Locale.ENGLISH);
        if (!lowerFileName.endsWith(".xls") && !lowerFileName.endsWith(".xlsx")) {
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
        AdminQuestionImportCommand normalized = new AdminQuestionImportCommand();
        normalized.setFileName(fileName);
        normalized.setFileSize(command.getFileSize());
        String courseName = trimToNull(command.getCourseName());
        if (courseName == null) {
            throw new BusinessException(400, "Course name is required");
        }
        if (courseName.length() > 30) {
            throw new BusinessException(400, "Course name cannot exceed 30 characters");
        }
        normalized.setCourseName(courseName);
        normalized.setRows(command.getRows());
        return normalized;
    }

    private AdminQuestionCommand commandFromRow(AdminQuestionImportRow row, String courseName) {
        AdminQuestionCommand command = new AdminQuestionCommand();
        command.setCourseName(courseName);
        command.setQuestionType(row == null ? null : row.getQuestionType());
        command.setTitle(row == null ? null : row.getTitle());
        command.setScore(row == null ? null : row.getScore());
        command.setStandardAnswer(row == null ? null : row.getStandardAnswer());
        command.setExplanation(row == null ? null : row.getExplanation());
        command.setOptions(row == null ? null : row.getOptions());
        return command;
    }

    private AdminQuestionQuery normalizedQuery(AdminQuestionQuery query) {
        AdminQuestionQuery normalized = new AdminQuestionQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setQuestionType(upper(trimToNull(query.getQuestionType())));
            normalized.setEnabled(query.getEnabled());
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

    private String join(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
