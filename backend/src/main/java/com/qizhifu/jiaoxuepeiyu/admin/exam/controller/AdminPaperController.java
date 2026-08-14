package com.qizhifu.jiaoxuepeiyu.admin.exam.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.exam.AdminPaperService;
import com.qizhifu.jiaoxuepeiyu.admin.exam.AdminPaperQuestionImportService;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaper;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperLog;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperPreview;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuery;
import com.qizhifu.jiaoxuepeiyu.admin.exam.model.AdminPaperQuestionImportCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/papers")
@Tag(name = "Admin Papers", description = "Theory paper APIs for manual assembly, automatic assembly, publishing, and logs.")
public class AdminPaperController {

    private final AdminPaperService service;
    private final AdminPaperQuestionImportService questionImportService;

    public AdminPaperController(AdminPaperService service, AdminPaperQuestionImportService questionImportService) {
        this.service = service;
        this.questionImportService = questionImportService;
    }

    @GetMapping
    @Operation(summary = "List papers", description = "Returns paged theory papers filtered by name, assembly mode, creator, or publish status.")
    public ApiResponse<PageResponse<AdminPaper>> listPapers(@ModelAttribute AdminPaperQuery query) {
        return ApiResponse.ok(service.listPapers(query));
    }

    @GetMapping("/{paperId}")
    @Operation(summary = "Get paper detail", description = "Returns one paper with immutable question snapshots and scores.")
    public ApiResponse<AdminPaper> getPaper(@PathVariable Long paperId) {
        return ApiResponse.ok(service.getPaper(paperId));
    }

    @PostMapping
    @Operation(summary = "Create paper", description = "Creates a manual or automatic paper and stores question snapshots for historical consistency.")
    public ApiResponse<Long> createPaper(@RequestBody AdminPaperCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createPaper(body, AdminContext.requireAdminId(request)));
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview paper", description = "Builds a stable manual or automatic paper preview without persisting it.")
    public ApiResponse<AdminPaperPreview> previewPaper(@RequestBody AdminPaperCommand body) {
        return ApiResponse.ok(service.previewPaper(body));
    }

    @PutMapping("/{paperId}")
    @Operation(summary = "Update paper", description = "Rebuilds paper question snapshots and total score from the submitted paper definition.")
    public ApiResponse<Void> updatePaper(@PathVariable Long paperId,
                                         @RequestBody AdminPaperCommand body,
                                         HttpServletRequest request) {
        service.updatePaper(paperId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{paperId}/publish")
    @Operation(summary = "Publish paper", description = "Publishes a non-empty paper for student-side exam use.")
    public ApiResponse<Void> publishPaper(@PathVariable Long paperId, HttpServletRequest request) {
        service.publishPaper(paperId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{paperId}/cancel-publish")
    @Operation(summary = "Cancel paper publish", description = "Marks a published paper offline while preserving its historical records.")
    public ApiResponse<Void> cancelPublishPaper(@PathVariable Long paperId, HttpServletRequest request) {
        service.cancelPublishPaper(paperId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/import/preview")
    @Operation(summary = "Preview paper import", description = "Validates parsed Excel rows for manual or automatic paper definitions and returns row-level errors.")
    public ApiResponse<AdminPaperImportPreview> previewImport(@RequestBody AdminPaperImportCommand body) {
        return ApiResponse.ok(service.previewImport(body));
    }

    @PostMapping("/import/questions")
    @Operation(summary = "Import question rows as a paper", description = "Atomically imports Excel questions into the question bank and creates a manual paper.")
    public ApiResponse<Long> importQuestionPaper(@RequestBody AdminPaperQuestionImportCommand body,
                                                 HttpServletRequest request) {
        return ApiResponse.ok(questionImportService.importPaper(body, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/{paperId}/logs")
    @Operation(summary = "List paper logs", description = "Returns operation logs for one paper sorted by newest first.")
    public ApiResponse<List<AdminPaperLog>> listPaperLogs(@PathVariable Long paperId) {
        return ApiResponse.ok(service.listPaperLogs(paperId));
    }
}
