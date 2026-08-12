package com.qizhifu.jiaoxuepeiyu.admin.exam.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.exam.AdminQuestionService;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestion;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminQuestionQuery;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/questions")
@Tag(name = "Admin Questions", description = "Theory question bank APIs for CRUD, status changes, logs, and import previews.")
public class AdminQuestionController {

    private final AdminQuestionService service;

    public AdminQuestionController(AdminQuestionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List questions", description = "Returns paged question bank entries filtered by keyword, type, creator, or enabled state.")
    public ApiResponse<PageResponse<AdminQuestion>> listQuestions(@ModelAttribute AdminQuestionQuery query) {
        return ApiResponse.ok(service.listQuestions(query));
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Get question detail", description = "Returns one question with normalized options and standard answer.")
    public ApiResponse<AdminQuestion> getQuestion(@PathVariable Long questionId) {
        return ApiResponse.ok(service.getQuestion(questionId));
    }

    @PostMapping
    @Operation(summary = "Create question", description = "Creates a theory question and derives choice answers from correct option flags.")
    public ApiResponse<Long> createQuestion(@RequestBody AdminQuestionCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createQuestion(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "Update question", description = "Updates question body, score, answer, and options while preserving historical paper snapshots.")
    public ApiResponse<Void> updateQuestion(@PathVariable Long questionId,
                                            @RequestBody AdminQuestionCommand body,
                                            HttpServletRequest request) {
        service.updateQuestion(questionId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete question", description = "Soft deletes a question while preserving historical paper snapshots and operation logs.")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId, HttpServletRequest request) {
        service.deleteQuestion(questionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{questionId}/enable")
    @Operation(summary = "Enable question", description = "Marks a question available for future manual or automatic paper assembly.")
    public ApiResponse<Void> enableQuestion(@PathVariable Long questionId, HttpServletRequest request) {
        service.enableQuestion(questionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{questionId}/disable")
    @Operation(summary = "Disable question", description = "Disables a question without removing already published paper snapshots.")
    public ApiResponse<Void> disableQuestion(@PathVariable Long questionId, HttpServletRequest request) {
        service.disableQuestion(questionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/import/preview")
    @Operation(summary = "Preview question import", description = "Validates parsed Excel rows and returns valid row count plus row-level errors.")
    public ApiResponse<AdminQuestionImportPreview> previewImport(@RequestBody AdminQuestionImportCommand body) {
        return ApiResponse.ok(service.previewImport(body));
    }

    @PostMapping("/import")
    @Operation(summary = "Import questions", description = "Validates and atomically imports parsed Excel question rows.")
    public ApiResponse<Integer> importQuestions(@RequestBody AdminQuestionImportCommand body,
                                                HttpServletRequest request) {
        return ApiResponse.ok(service.importQuestions(body, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/{questionId}/logs")
    @Operation(summary = "List question logs", description = "Returns operation logs for one question sorted by newest first.")
    public ApiResponse<List<AdminQuestionLog>> listQuestionLogs(@PathVariable Long questionId) {
        return ApiResponse.ok(service.listQuestionLogs(questionId));
    }
}
