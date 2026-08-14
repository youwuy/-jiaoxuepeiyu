package com.qizhifu.jiaoxuepeiyu.admin.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperAutoRule;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.port.AdminPaperRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminPaperServiceTests {

    @Test
    void createsManualPaperWithUniqueEnabledQuestions() {
        FakePapers repository = new FakePapers();
        repository.availableQuestions = Arrays.asList(question(1L, "SINGLE", true), question(2L, "JUDGE", true));
        AdminPaperService service = new AdminPaperService(repository);

        Long paperId = service.createPaper(manualPaper(), 9L);

        assertEquals(21L, paperId.longValue());
        assertEquals(2, repository.savedCommand.getQuestions().size());
        assertEquals(15, repository.savedTotalScore.intValue());
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsManualPaperWithDuplicateQuestion() {
        AdminPaperService service = new AdminPaperService(new FakePapers());
        AdminPaperCommand command = manualPaper();
        command.setQuestions(Arrays.asList(questionCommand(1L, 5), questionCommand(1L, 5)));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createPaper(command, 9L);
        });

        assertEquals("Paper questions cannot repeat", exception.getMessage());
    }

    @Test
    void rejectsManualPaperWithDisabledQuestion() {
        FakePapers repository = new FakePapers();
        repository.availableQuestions = Arrays.asList(question(1L, "SINGLE", true), question(2L, "JUDGE", false));
        AdminPaperService service = new AdminPaperService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createPaper(manualPaper(), 9L);
        });

        assertEquals("Paper contains disabled or missing questions", exception.getMessage());
    }

    @Test
    void createsAutoPaperFromEnabledQuestionPool() {
        FakePapers repository = new FakePapers();
        repository.autoQuestions = Arrays.asList(question(1L, "SINGLE", true), question(2L, "SINGLE", true));
        AdminPaperService service = new AdminPaperService(repository);
        AdminPaperCommand command = new AdminPaperCommand();
        command.setPaperName("Auto Paper");
        command.setComposeMode("AUTO");
        AdminPaperAutoRule rule = new AdminPaperAutoRule();
        rule.setQuestionType("SINGLE");
        rule.setQuestionCount(2);
        rule.setScorePerQuestion(5);
        command.setAutoRules(Arrays.asList(rule));

        Long paperId = service.createPaper(command, 9L);

        assertEquals(21L, paperId.longValue());
        assertEquals(2, repository.savedCommand.getQuestions().size());
        assertEquals(10, repository.savedTotalScore.intValue());
    }

    @Test
    void previewsAutoPaperAndReusesTheSameQuestionsOnCreate() {
        FakePapers repository = new FakePapers();
        AdminQuestion first = question(1L, "SINGLE", true);
        first.setTitle("Question 1");
        AdminQuestion second = question(2L, "SINGLE", true);
        second.setTitle("Question 2");
        repository.autoQuestions = Arrays.asList(first, second);
        repository.availableQuestions = Arrays.asList(first, second);
        AdminPaperService service = new AdminPaperService(repository);
        AdminPaperCommand command = new AdminPaperCommand();
        command.setPaperName("Auto Paper");
        command.setComposeMode("AUTO");
        AdminPaperAutoRule rule = new AdminPaperAutoRule();
        rule.setQuestionType("SINGLE");
        rule.setQuestionCount(2);
        rule.setScorePerQuestion(5);
        command.setAutoRules(Arrays.asList(rule));

        AdminPaperPreview preview = service.previewPaper(command);
        command.setQuestions(Arrays.asList(
                questionCommand(preview.getQuestions().get(0).getQuestionId(), preview.getQuestions().get(0).getScore()),
                questionCommand(preview.getQuestions().get(1).getQuestionId(), preview.getQuestions().get(1).getScore())));
        service.createPaper(command, 9L);

        assertEquals(2, preview.getQuestions().size());
        assertEquals(10, preview.getTotalScore().intValue());
        assertEquals(preview.getQuestions().get(0).getQuestionId(), repository.savedCommand.getQuestions().get(0).getQuestionId());
        assertEquals(preview.getQuestions().get(1).getQuestionId(), repository.savedCommand.getQuestions().get(1).getQuestionId());
    }

    @Test
    void rejectsAutoPaperWhenQuestionPoolIsTooSmall() {
        FakePapers repository = new FakePapers();
        repository.autoQuestions = Arrays.asList(question(1L, "SINGLE", true));
        AdminPaperService service = new AdminPaperService(repository);
        AdminPaperCommand command = new AdminPaperCommand();
        command.setPaperName("Auto Paper");
        command.setComposeMode("AUTO");
        AdminPaperAutoRule rule = new AdminPaperAutoRule();
        rule.setQuestionType("SINGLE");
        rule.setQuestionCount(2);
        rule.setScorePerQuestion(5);
        command.setAutoRules(Arrays.asList(rule));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.createPaper(command, 9L);
        });

        assertEquals("Not enough enabled questions for auto paper", exception.getMessage());
    }

    @Test
    void rejectsPublishingEmptyPaper() {
        FakePapers repository = new FakePapers();
        repository.paper = new AdminPaper();
        repository.paper.setPaperId(21L);
        repository.paper.setQuestionCount(0);
        AdminPaperService service = new AdminPaperService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.publishPaper(21L, 9L);
        });

        assertEquals("Paper must contain questions before publishing", exception.getMessage());
    }

    @Test
    void importPreviewReturnsValidRowsAndErrors() {
        FakePapers repository = new FakePapers();
        repository.availableQuestions = Arrays.asList(question(1L, "SINGLE", true));
        AdminPaperService service = new AdminPaperService(repository);
        AdminPaperImportCommand command = new AdminPaperImportCommand();
        command.setFileName("papers.xlsx");
        command.setFileSize(2048L);
        command.setRows(Arrays.asList(importRow(2, "Manual Paper"), importRow(3, "")));

        AdminPaperImportPreview preview = service.previewImport(command);

        assertEquals(1, preview.getValidCount());
        assertEquals(1, preview.getErrorCount());
        assertEquals(3, preview.getErrors().get(0).getRowNumber());
    }

    private AdminPaperCommand manualPaper() {
        AdminPaperCommand command = new AdminPaperCommand();
        command.setPaperName("Manual Paper");
        command.setComposeMode("MANUAL");
        command.setQuestions(Arrays.asList(questionCommand(1L, 5), questionCommand(2L, 10)));
        return command;
    }

    private AdminPaperQuestionCommand questionCommand(Long questionId, int score) {
        AdminPaperQuestionCommand command = new AdminPaperQuestionCommand();
        command.setQuestionId(questionId);
        command.setScore(score);
        return command;
    }

    private AdminPaperImportRow importRow(int rowNumber, String paperName) {
        AdminPaperImportRow row = new AdminPaperImportRow();
        row.setRowNumber(rowNumber);
        row.setPaperName(paperName);
        row.setComposeMode("MANUAL");
        row.setQuestions(Arrays.asList(questionCommand(1L, 5)));
        return row;
    }

    private AdminQuestion question(Long questionId, String type, boolean enabled) {
        AdminQuestion question = new AdminQuestion();
        question.setQuestionId(questionId);
        question.setQuestionType(type);
        question.setEnabled(enabled);
        return question;
    }

    private static class FakePapers implements AdminPaperRepository {
        private List<AdminQuestion> availableQuestions = new ArrayList<AdminQuestion>();
        private List<AdminQuestion> autoQuestions = new ArrayList<AdminQuestion>();
        private AdminPaperCommand savedCommand;
        private Integer savedTotalScore;
        private AdminPaper paper;
        private String lastLogAction;

        @Override
        public List<AdminPaper> findPapers(AdminPaperQuery query) {
            return new ArrayList<AdminPaper>();
        }

        @Override
        public long countPapers(AdminPaperQuery query) {
            return 0;
        }

        @Override
        public AdminPaper findPaper(Long paperId) {
            return paper;
        }

        @Override
        public List<AdminQuestion> findQuestionsByIds(List<Long> questionIds) {
            return availableQuestions;
        }

        @Override
        public List<AdminQuestion> findEnabledQuestionsByType(String questionType, int limit) {
            return autoQuestions;
        }

        @Override
        public Long createPaper(AdminPaperCommand command, Long creatorId, Integer totalScore) {
            this.savedCommand = command;
            this.savedTotalScore = totalScore;
            return 21L;
        }

        @Override
        public void updatePaper(Long paperId, AdminPaperCommand command, Integer totalScore) {
        }

        @Override
        public void updatePaperPublishStatus(Long paperId, String publishStatus) {
        }

        @Override
        public void appendPaperLog(Long paperId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }
    }
}
