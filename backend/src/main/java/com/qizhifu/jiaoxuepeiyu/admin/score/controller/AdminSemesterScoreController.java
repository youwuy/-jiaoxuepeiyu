package com.qizhifu.jiaoxuepeiyu.admin.score.controller;

import com.qizhifu.jiaoxuepeiyu.admin.score.AdminSemesterScoreService;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.export.CsvExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scores/semester")
@Tag(name = "Admin Semester Scores", description = "Semester comprehensive score list, statistics, ranking, and export-ready data APIs.")
public class AdminSemesterScoreController {

    private final AdminSemesterScoreService service;

    public AdminSemesterScoreController(AdminSemesterScoreService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List semester scores", description = "Returns paged student semester comprehensive score rows filtered by term, major, class, student, or keyword.")
    public ApiResponse<PageResponse<AdminSemesterScore>> listScores(@ModelAttribute AdminSemesterScoreQuery query) {
        return ApiResponse.ok(service.listScores(query));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get semester score statistics", description = "Returns count, average, max, min, excellent count, and pass count for the selected filters.")
    public ApiResponse<AdminSemesterScoreStatistics> getStatistics(@ModelAttribute AdminSemesterScoreQuery query) {
        return ApiResponse.ok(service.getStatistics(query));
    }

    @GetMapping("/ranking")
    @Operation(summary = "List semester score ranking", description = "Returns top students ranked by comprehensive score for the selected filters.")
    public ApiResponse<List<AdminSemesterScore>> listRanking(@ModelAttribute AdminSemesterScoreQuery query) {
        return ApiResponse.ok(service.listRanking(query));
    }

    @GetMapping("/export")
    @Operation(summary = "Export semester score rows", description = "Returns export-ready semester score rows for frontend-controlled export.")
    public ApiResponse<List<AdminSemesterScore>> exportScores(@ModelAttribute AdminSemesterScoreQuery query) {
        return ApiResponse.ok(service.exportScores(query));
    }

    @GetMapping("/export/file")
    @Operation(summary = "Download semester score CSV", description = "Downloads filtered semester score rows as an Excel-compatible CSV file.")
    public ResponseEntity<byte[]> exportScoreFile(@ModelAttribute AdminSemesterScoreQuery query) {
        List<List<String>> csvRows = new ArrayList<List<String>>();
        for (AdminSemesterScore score : service.exportScores(query)) {
            csvRows.add(Arrays.asList(
                    value(score.getScoreId()),
                    value(score.getStudentNo()),
                    value(score.getStudentName()),
                    value(score.getClassName()),
                    value(score.getMajorName()),
                    value(score.getAcademicTerm()),
                    value(score.getCoursewareLearningScore()),
                    value(score.getTrainingPracticeScore()),
                    value(score.getCourseAssignmentScore()),
                    value(score.getExamScore()),
                    value(score.getComprehensiveScore()),
                    value(score.getPublishedAt())));
        }
        return CsvExporter.toAttachment("semester-scores.csv", Arrays.asList(
                "Score ID",
                "Student No",
                "Student Name",
                "Class",
                "Major",
                "Academic Term",
                "Courseware Score",
                "Training Score",
                "Assignment Score",
                "Exam Score",
                "Comprehensive Score",
                "Published At"), csvRows);
    }

    @PostMapping("/import/preview")
    @Operation(summary = "Preview semester score import", description = "Validates parsed offline score rows and returns row-level errors with calculated comprehensive scores for valid rows.")
    public ApiResponse<AdminSemesterScoreImportPreview> previewImport(@RequestBody AdminSemesterScoreImportCommand body) {
        return ApiResponse.ok(service.previewImport(body));
    }

    @PostMapping("/import")
    @Operation(summary = "Import semester scores", description = "Upserts validated offline score rows and calculates comprehensive scores on the backend.")
    public ApiResponse<AdminSemesterScoreImportResult> importScores(@RequestBody AdminSemesterScoreImportCommand body) {
        return ApiResponse.ok(service.importScores(body));
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
