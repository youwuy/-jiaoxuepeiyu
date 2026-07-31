package com.qizhifu.jiaoxuepeiyu.admin.training.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.training.AdminTrainingService;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingLog;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingMonitorSnapshot;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingQuery;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingStatistics;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.export.CsvExporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/trainings")
@Tag(name = "Admin Trainings", description = "Training course lifecycle, publication, monitoring, statistics, and operation logs.")
public class AdminTrainingController {

    private final AdminTrainingService service;

    public AdminTrainingController(AdminTrainingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List trainings", description = "Returns paged training courses filtered by name, term, major, class, type, mode, or publish status.")
    public ApiResponse<PageResponse<AdminTraining>> listTrainings(@ModelAttribute AdminTrainingQuery query) {
        return ApiResponse.ok(service.listTrainings(query));
    }

    @GetMapping("/export")
    @Operation(summary = "Export training rows", description = "Returns export-ready training course rows for frontend-controlled export.")
    public ApiResponse<List<AdminTraining>> exportTrainings(@ModelAttribute AdminTrainingQuery query) {
        return ApiResponse.ok(service.exportTrainings(query));
    }

    @GetMapping("/export/file")
    @Operation(summary = "Download training CSV", description = "Downloads filtered training course rows as an Excel-compatible CSV file.")
    public ResponseEntity<byte[]> exportTrainingFile(@ModelAttribute AdminTrainingQuery query) {
        List<List<String>> csvRows = new ArrayList<List<String>>();
        for (AdminTraining training : service.exportTrainings(query)) {
            csvRows.add(Arrays.asList(
                    value(training.getTrainingId()),
                    value(training.getTrainingName()),
                    value(training.getAcademicYearName()),
                    value(training.getSemesterName()),
                    value(training.getMajorName()),
                    value(training.getTrainingType()),
                    value(training.getTrainingMode()),
                    value(training.getPaperMode()),
                    value(training.getPaperName()),
                    value(training.getPublishStatus()),
                    value(training.getOpenStartTime()),
                    value(training.getOpenEndTime()),
                    value(training.getTeamSize()),
                    value(training.getAppRequired()),
                    value(training.getClassNames()),
                    value(training.getParticipantCount()),
                    value(training.getRoomCount()),
                    value(training.getStartedRoomCount()),
                    value(training.getAverageScore()),
                    value(training.getCreatorName()),
                    value(training.getCreatedAt())));
        }
        return CsvExporter.toAttachment("trainings.csv", Arrays.asList(
                "Training ID",
                "Training Name",
                "Academic Year",
                "Semester",
                "Major",
                "Training Type",
                "Training Mode",
                "Paper Mode",
                "Paper",
                "Publish Status",
                "Open Start Time",
                "Open End Time",
                "Team Size",
                "App Required",
                "Classes",
                "Participant Count",
                "Room Count",
                "Started Room Count",
                "Average Score",
                "Creator",
                "Created At"), csvRows);
    }

    @GetMapping("/{trainingId}")
    @Operation(summary = "Get training detail", description = "Returns one training course with bound classes and team roles.")
    public ApiResponse<AdminTraining> getTraining(@PathVariable Long trainingId) {
        return ApiResponse.ok(service.getTraining(trainingId));
    }

    @PostMapping
    @Operation(summary = "Create training", description = "Creates a draft training course with class bindings, paper selection metadata, and team roles.")
    public ApiResponse<Long> createTraining(@RequestBody AdminTrainingCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createTraining(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/{trainingId}")
    @Operation(summary = "Update training", description = "Updates training metadata and fully replaces submitted class bindings and team roles.")
    public ApiResponse<Void> updateTraining(@PathVariable Long trainingId,
                                            @RequestBody AdminTrainingCommand body,
                                            HttpServletRequest request) {
        service.updateTraining(trainingId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{trainingId}/publish")
    @Operation(summary = "Publish training", description = "Publishes a training course, synchronizes participants from bound classes, and sends training notifications.")
    public ApiResponse<Void> publishTraining(@PathVariable Long trainingId, HttpServletRequest request) {
        service.publishTraining(trainingId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{trainingId}/cancel-publish")
    @Operation(summary = "Cancel training publish", description = "Marks a training course offline while preserving rooms, participants, and monitor records.")
    public ApiResponse<Void> cancelPublishTraining(@PathVariable Long trainingId, HttpServletRequest request) {
        service.cancelPublishTraining(trainingId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{trainingId}/delete")
    @Operation(summary = "Delete training", description = "Soft deletes a training course and takes it offline.")
    public ApiResponse<Void> deleteTraining(@PathVariable Long trainingId, HttpServletRequest request) {
        service.deleteTraining(trainingId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/{trainingId}/statistics")
    @Operation(summary = "Get training statistics", description = "Returns participant count, room counts, submitted monitor score count, and score aggregates.")
    public ApiResponse<AdminTrainingStatistics> getStatistics(@PathVariable Long trainingId) {
        return ApiResponse.ok(service.getStatistics(trainingId));
    }

    @GetMapping("/{trainingId}/monitor")
    @Operation(summary = "Get training monitor snapshot", description = "Returns camera states, student desk/progress states, room role states, and statistics for the management monitor page.")
    public ApiResponse<AdminTrainingMonitorSnapshot> getMonitorSnapshot(@PathVariable Long trainingId) {
        return ApiResponse.ok(service.getMonitorSnapshot(trainingId));
    }

    @GetMapping("/{trainingId}/logs")
    @Operation(summary = "List training logs", description = "Returns operation logs for one training course sorted by newest first.")
    public ApiResponse<List<AdminTrainingLog>> listTrainingLogs(@PathVariable Long trainingId) {
        return ApiResponse.ok(service.listTrainingLogs(trainingId));
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
