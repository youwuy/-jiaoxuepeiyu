package com.qizhifu.jiaoxuepeiyu.admin.archive.controller;

import com.qizhifu.jiaoxuepeiyu.admin.archive.AdminTrainingArchiveService;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveQuery;
import com.qizhifu.jiaoxuepeiyu.admin.archive.model.AdminTrainingArchiveStatistics;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/archives")
@Tag(name = "Admin Training Archives", description = "Training archive list, detail, statistics, and export-ready data APIs.")
public class AdminTrainingArchiveController {

    private final AdminTrainingArchiveService service;

    public AdminTrainingArchiveController(AdminTrainingArchiveService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List training archives", description = "Returns paged immutable training attempt archives filtered by training, student, class, mode, submit type, time, or keyword.")
    public ApiResponse<PageResponse<AdminTrainingArchive>> listArchives(@ModelAttribute AdminTrainingArchiveQuery query) {
        return ApiResponse.ok(service.listArchives(query));
    }

    @GetMapping("/{archiveId}")
    @Operation(summary = "Get training archive detail", description = "Returns archive metadata, recording URL, and ordered step records.")
    public ApiResponse<AdminTrainingArchiveDetail> getArchiveDetail(@PathVariable Long archiveId) {
        return ApiResponse.ok(service.getArchiveDetail(archiveId));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get training archive statistics", description = "Returns archive counts, submit type counts, average score, and average duration for selected filters.")
    public ApiResponse<AdminTrainingArchiveStatistics> getStatistics(@ModelAttribute AdminTrainingArchiveQuery query) {
        return ApiResponse.ok(service.getStatistics(query));
    }

    @GetMapping("/export")
    @Operation(summary = "Export training archive rows", description = "Returns export-ready archive rows. Binary export generation is handled by deployment integration later.")
    public ApiResponse<List<AdminTrainingArchive>> exportArchives(@ModelAttribute AdminTrainingArchiveQuery query) {
        return ApiResponse.ok(service.exportArchives(query));
    }
}
