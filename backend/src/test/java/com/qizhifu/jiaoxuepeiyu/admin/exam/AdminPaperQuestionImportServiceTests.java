package com.qizhifu.jiaoxuepeiyu.admin.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportRow;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminPaperQuestionImportServiceTests {

    @Test
    void importsQuestionsAndCreatesManualPaper() {
        AdminQuestionService questionService = mock(AdminQuestionService.class);
        AdminPaperService paperService = mock(AdminPaperService.class);
        when(questionService.importQuestionIds(any(), eq(9L))).thenReturn(Arrays.asList(31L, 32L));
        when(paperService.createPaper(any(), eq(9L))).thenReturn(21L);
        AdminPaperQuestionImportService service = new AdminPaperQuestionImportService(questionService, paperService);

        Long paperId = service.importPaper(command(), 9L);

        ArgumentCaptor<AdminPaperCommand> paper = ArgumentCaptor.forClass(AdminPaperCommand.class);
        verify(paperService).createPaper(paper.capture(), eq(9L));
        assertEquals(21L, paperId.longValue());
        assertEquals("MANUAL", paper.getValue().getComposeMode());
        assertEquals("Operations", paper.getValue().getCourseName());
        assertEquals(2, paper.getValue().getQuestions().size());
        assertEquals(10, paper.getValue().getQuestions().get(1).getScore().intValue());
    }

    @Test
    void rejectsMissingCourseBeforeImportingQuestions() {
        AdminPaperQuestionImportCommand command = command();
        command.setCourseName("");
        AdminPaperQuestionImportService service = new AdminPaperQuestionImportService(
                mock(AdminQuestionService.class), mock(AdminPaperService.class));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.importPaper(command, 9L));

        assertEquals("Course name is required", exception.getMessage());
    }

    private AdminPaperQuestionImportCommand command() {
        AdminPaperQuestionImportCommand command = new AdminPaperQuestionImportCommand();
        command.setPaperName("Imported Paper");
        command.setCourseName("Operations");
        command.setFileName("paper.xlsx");
        command.setFileSize(1024L);
        command.setRows(Arrays.asList(row(2, 5), row(3, 10)));
        return command;
    }

    private AdminQuestionImportRow row(int rowNumber, int score) {
        AdminQuestionImportRow row = new AdminQuestionImportRow();
        row.setRowNumber(rowNumber);
        row.setScore(score);
        return row;
    }
}
