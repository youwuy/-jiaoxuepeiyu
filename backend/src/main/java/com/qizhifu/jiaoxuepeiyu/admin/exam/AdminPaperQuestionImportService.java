package com.qizhifu.jiaoxuepeiyu.admin.exam;

import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportRow;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPaperQuestionImportService {

    private final AdminQuestionService questionService;
    private final AdminPaperService paperService;

    public AdminPaperQuestionImportService(AdminQuestionService questionService, AdminPaperService paperService) {
        this.questionService = questionService;
        this.paperService = paperService;
    }

    @Transactional
    public Long importPaper(AdminPaperQuestionImportCommand command, Long operatorId) {
        if (command == null || !InputValidator.hasText(command.getPaperName())) {
            throw new BusinessException(400, "Paper name is required");
        }
        if (!InputValidator.hasText(command.getCourseName())) {
            throw new BusinessException(400, "Course name is required");
        }
        AdminQuestionImportCommand questionImport = new AdminQuestionImportCommand();
        questionImport.setFileName(command.getFileName());
        questionImport.setFileSize(command.getFileSize());
        questionImport.setRows(command.getRows());
        List<Long> questionIds = questionService.importQuestionIds(questionImport, operatorId);

        List<AdminPaperQuestionCommand> paperQuestions = new ArrayList<AdminPaperQuestionCommand>();
        List<AdminQuestionImportRow> rows = command.getRows();
        for (int i = 0; i < questionIds.size(); i++) {
            AdminQuestionImportRow row = rows.get(i);
            AdminPaperQuestionCommand question = new AdminPaperQuestionCommand();
            question.setQuestionId(questionIds.get(i));
            question.setScore(row.getScore());
            paperQuestions.add(question);
        }
        AdminPaperCommand paper = new AdminPaperCommand();
        paper.setPaperName(command.getPaperName().trim());
        paper.setCourseName(command.getCourseName().trim());
        paper.setComposeMode("MANUAL");
        paper.setQuestions(paperQuestions);
        return paperService.createPaper(paper, operatorId);
    }
}
