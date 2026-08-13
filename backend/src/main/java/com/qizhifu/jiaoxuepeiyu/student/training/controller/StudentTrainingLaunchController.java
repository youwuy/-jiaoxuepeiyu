package com.qizhifu.jiaoxuepeiyu.student.training.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.ue.UeLaunchSessionService;
import com.qizhifu.jiaoxuepeiyu.ue.UeTrainingCallbackService;
import com.qizhifu.jiaoxuepeiyu.ue.model.TrainingLaunchTask;
import com.qizhifu.jiaoxuepeiyu.ue.model.UeLaunchSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/trainings")
@Tag(name = "Student Training Launch", description = "Creates short-lived launch sessions for the local UE training program.")
public class StudentTrainingLaunchController {

    private final UeTrainingCallbackService callbackService;
    private final UeLaunchSessionService launchSessionService;

    public StudentTrainingLaunchController(UeTrainingCallbackService callbackService,
                                           UeLaunchSessionService launchSessionService) {
        this.callbackService = callbackService;
        this.launchSessionService = launchSessionService;
    }

    @PostMapping("/{trainingId}/launch-session")
    @Operation(summary = "Create UE launch session", description = "Validates the assigned training and returns an eight-hour token scoped to the current student and training.")
    public ApiResponse<UeLaunchSession> create(@PathVariable Long trainingId,
                                               @RequestParam Long topicId,
                                               HttpServletRequest request) {
        Long studentId = StudentContext.requireStudentId(request);
        TrainingLaunchTask task = callbackService.getTask(studentId, trainingId, topicId);
        return ApiResponse.ok(launchSessionService.create(studentId, trainingId, topicId, task.getRoomId()));
    }
}
