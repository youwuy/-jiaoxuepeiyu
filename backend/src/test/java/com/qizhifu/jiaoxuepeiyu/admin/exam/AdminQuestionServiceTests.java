package com.qizhifu.jiaoxuepeiyu.admin.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionOption;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminQuestionRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminQuestionServiceTests {

    @Test
    void createsSingleChoiceWithOneCorrectOption() {
        FakeQuestions repository = new FakeQuestions();
        AdminQuestionService service = new AdminQuestionService(repository);

        Long questionId = service.createQuestion(singleChoice(), 9L);

        assertEquals(11L, questionId.longValue());
        assertEquals("SINGLE", repository.savedCommand.getQuestionType());
        assertEquals("A", repository.savedCommand.getStandardAnswer());
        assertEquals("Explanation", repository.savedCommand.getExplanation());
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsSingleChoiceWithoutExactlyOneCorrectOption() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionCommand command = singleChoice();
        command.getOptions().get(1).setCorrect(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createQuestion(command, 9L);
        });

        assertEquals("Single choice must have exactly one correct option", exception.getMessage());
    }

    @Test
    void rejectsInvalidJudgmentAnswer() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionCommand command = new AdminQuestionCommand();
        command.setQuestionType("JUDGE");
        command.setCourseName("Operations");
        command.setTitle("Is this valid?");
        command.setScore(5);
        command.setStandardAnswer("MAYBE");
        command.setExplanation("Explanation");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createQuestion(command, 9L);
        });

        assertEquals("Judgment answer must be TRUE or FALSE", exception.getMessage());
    }

    @Test
    void importPreviewReturnsValidRowsAndErrors() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionImportCommand command = new AdminQuestionImportCommand();
        command.setFileName("questions.xlsx");
        command.setFileSize(1024L);
        command.setCourseName("Operations");
        command.setRows(Arrays.asList(importRow(2, "SINGLE", "Valid title", "A"), importRow(3, "BAD", "", "")));

        AdminQuestionImportPreview preview = service.previewImport(command);

        assertEquals(1, preview.getValidCount());
        assertEquals(1, preview.getErrorCount());
        assertEquals(3, preview.getErrors().get(0).getRowNumber());
    }

    @Test
    void importsAllValidatedRows() {
        FakeQuestions repository = new FakeQuestions();
        AdminQuestionService service = new AdminQuestionService(repository);
        AdminQuestionImportCommand command = new AdminQuestionImportCommand();
        command.setFileName("questions.xlsx");
        command.setFileSize(1024L);
        command.setCourseName("Operations");
        command.setRows(Arrays.asList(importRow(2, "SINGLE", "First", "A"),
                importRow(3, "SINGLE", "Second", "A")));

        int imported = service.importQuestions(command, 9L);

        assertEquals(2, imported);
        assertEquals(2, repository.createdCount);
        assertEquals("IMPORT", repository.lastLogAction);
    }

    @Test
    void rejectsWholeImportWhenAnyRowIsInvalid() {
        FakeQuestions repository = new FakeQuestions();
        AdminQuestionService service = new AdminQuestionService(repository);
        AdminQuestionImportCommand command = new AdminQuestionImportCommand();
        command.setFileName("questions.xlsx");
        command.setFileSize(1024L);
        command.setCourseName("Operations");
        command.setRows(Arrays.asList(importRow(2, "SINGLE", "Valid", "A"),
                importRow(3, "BAD", "Invalid", "A")));

        assertThrows(BusinessException.class, () -> service.importQuestions(command, 9L));
        assertEquals(0, repository.createdCount);
    }

    @Test
    void disablesQuestionWithoutRemovingHistoricalReferences() {
        FakeQuestions repository = new FakeQuestions();
        AdminQuestionService service = new AdminQuestionService(repository);

        service.disableQuestion(11L, 9L);

        assertEquals(11L, repository.statusQuestionId.longValue());
        assertEquals(false, repository.statusEnabled.booleanValue());
        assertEquals("DISABLE", repository.lastLogAction);
    }

    @Test
    void persistsCourseName() {
        FakeQuestions repository = new FakeQuestions();
        AdminQuestionService service = new AdminQuestionService(repository);

        service.createQuestion(singleChoice(), 9L);

        assertEquals("Operations", repository.savedCommand.getCourseName());
    }

    @Test
    void validatesFillBlankMarkersAndAnswerCount() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionCommand command = new AdminQuestionCommand();
        command.setQuestionType("FILL_BLANK");
        command.setCourseName("Operations");
        command.setTitle("Signal ____ and route ____");
        command.setScore(5);
        command.setStandardAnswer("red");
        command.setExplanation("Explanation");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createQuestion(command, 9L));

        assertEquals("Fill blank answer count must match blank markers", exception.getMessage());
    }

    @Test
    void rejectsFillBlankWithShortUnderscoreMarker() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionCommand command = new AdminQuestionCommand();
        command.setQuestionType("FILL_BLANK");
        command.setCourseName("Operations");
        command.setTitle("Signal _ route");
        command.setScore(5);
        command.setStandardAnswer("red");
        command.setExplanation("Explanation");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createQuestion(command, 9L));

        assertEquals("Fill blank title must contain underscore markers", exception.getMessage());
    }

    @Test
    void acceptsDocumentedExcelExtensionForImportPreview() {
        AdminQuestionService service = new AdminQuestionService(new FakeQuestions());
        AdminQuestionImportCommand command = new AdminQuestionImportCommand();
        command.setFileName("questions.excel");
        command.setFileSize(1024L);
        command.setCourseName("Operations");
        command.setRows(Arrays.asList(importRow(2, "SINGLE", "Valid title", "A")));

        assertEquals(1, service.previewImport(command).getValidCount());
    }

    private AdminQuestionCommand singleChoice() {
        AdminQuestionCommand command = new AdminQuestionCommand();
        command.setQuestionType("SINGLE");
        command.setCourseName("Operations");
        command.setTitle("Pick one");
        command.setScore(10);
        command.setExplanation("Explanation");
        command.setOptions(Arrays.asList(option("A", "Alpha", true), option("B", "Beta", false)));
        return command;
    }

    private AdminQuestionOption option(String key, String text, boolean correct) {
        AdminQuestionOption option = new AdminQuestionOption();
        option.setOptionKey(key);
        option.setOptionText(text);
        option.setCorrect(correct);
        return option;
    }

    private AdminQuestionImportRow importRow(int rowNumber, String type, String title, String answer) {
        AdminQuestionImportRow row = new AdminQuestionImportRow();
        row.setRowNumber(rowNumber);
        row.setQuestionType(type);
        row.setTitle(title);
        row.setScore(5);
        row.setStandardAnswer(answer);
        row.setExplanation("Explanation for row " + rowNumber);
        row.setOptions(Arrays.asList(option("A", "Alpha", "A".equals(answer)), option("B", "Beta", false)));
        return row;
    }

    private static class FakeQuestions implements AdminQuestionRepository {
        private AdminQuestionCommand savedCommand;
        private Long statusQuestionId;
        private Boolean statusEnabled;
        private String lastLogAction;
        private int createdCount;

        @Override
        public List<AdminQuestion> findQuestions(AdminQuestionQuery query) {
            return new ArrayList<AdminQuestion>();
        }

        @Override
        public long countQuestions(AdminQuestionQuery query) {
            return 0;
        }

        @Override
        public AdminQuestion findQuestion(Long questionId) {
            AdminQuestion question = new AdminQuestion();
            question.setQuestionId(questionId);
            question.setEnabled(true);
            return question;
        }

        @Override
        public Long createQuestion(AdminQuestionCommand command, Long creatorId) {
            this.savedCommand = command;
            this.createdCount++;
            return Long.valueOf(10L + createdCount);
        }

        @Override
        public void updateQuestion(Long questionId, AdminQuestionCommand command) {
        }

        @Override
        public void updateQuestionStatus(Long questionId, boolean enabled) {
            this.statusQuestionId = questionId;
            this.statusEnabled = enabled;
        }

        @Override
        public void appendQuestionLog(Long questionId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }
    }
}
