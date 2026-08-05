package com.qizhifu.jiaoxuepeiyu.ue.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.ue.UeIdentityResolver;
import com.qizhifu.jiaoxuepeiyu.ue.UeTrainingCallbackService;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingAttemptCommand;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingStatusCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ue/trainings")
@Tag(name = "UE Training Callback", description = "Four integration APIs for the UE training program: task metadata, recording upload through /api/files, live status, and result callback.")
public class UeTrainingCallbackController {

    private final UeTrainingCallbackService service;
    private final UeIdentityResolver identityResolver;

    public UeTrainingCallbackController(UeTrainingCallbackService service, UeIdentityResolver identityResolver) {
        this.service = service;
        this.identityResolver = identityResolver;
    }

    @GetMapping("/{trainingId}/task")
    @Operation(summary = "Get UE training task", description = "Returns launch metadata for a training assigned to the current student.")
    public ApiResponse<TrainingLaunchTask> getTask(@PathVariable Long trainingId, HttpServletRequest request) {
        Long studentId = identityResolver.requireStudentId(request, trainingId);
        return ApiResponse.ok(service.getTask(studentId, trainingId));
    }

    @PostMapping("/{trainingId}/status")
    @Operation(summary = "Report UE training status", description = "Updates the latest monitor snapshot for the current student's running training.")
    public ApiResponse<Void> reportStatus(@PathVariable Long trainingId,
                                          @RequestBody TrainingStatusCommand body,
                                          HttpServletRequest request) {
        Long studentId = identityResolver.requireStudentId(request, trainingId);
        service.reportStatus(studentId, trainingId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{trainingId}/attempts")
    @Operation(summary = "Submit UE training result", description = "Creates an immutable training archive with optional step records, updates monitor status, and synchronizes the submitted personalScore to semester comprehensive scores as the training-practice score.")
    public ApiResponse<Long> submitAttempt(@PathVariable Long trainingId,
                                           @RequestBody TrainingAttemptCommand body,
                                           HttpServletRequest request) {
        Long studentId = identityResolver.requireStudentId(request, trainingId);
        return ApiResponse.ok(service.submitAttempt(studentId, trainingId, body));
    }
}
