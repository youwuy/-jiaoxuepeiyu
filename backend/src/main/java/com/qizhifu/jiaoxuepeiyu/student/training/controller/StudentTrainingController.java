package com.qizhifu.jiaoxuepeiyu.student.training.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.training.StudentTrainingService;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTraining;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentTrainingController {

    private final StudentTrainingService service;

    public StudentTrainingController(StudentTrainingService service) {
        this.service = service;
    }

    @GetMapping("/trainings")
    public ApiResponse<List<StudentTraining>> listTrainings(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        return ApiResponse.ok(service.listTrainings(StudentContext.requireStudentId(request), mode, keyword));
    }

    @GetMapping("/trainings/app-installation")
    public ApiResponse<TrainingAppInstallation> appInstallation(HttpServletRequest request) {
        return ApiResponse.ok(service.getAppInstallation(StudentContext.requireStudentId(request)));
    }

    @PostMapping("/trainings/{trainingId}/rooms")
    public ApiResponse<TrainingRoom> createRoom(@PathVariable Long trainingId, HttpServletRequest request) {
        return ApiResponse.ok(service.createRoom(StudentContext.requireStudentId(request), trainingId));
    }

    @GetMapping("/training-rooms/{roomId}")
    public ApiResponse<TrainingRoom> getRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.getRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/join")
    public ApiResponse<TrainingRoom> joinRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.joinRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/leave")
    public ApiResponse<TrainingRoom> leaveRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.leaveRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/roles/{roleId}/claim")
    public ApiResponse<TrainingRoom> claimRole(@PathVariable Long roomId,
                                               @PathVariable Long roleId,
                                               HttpServletRequest request) {
        return ApiResponse.ok(service.claimRole(StudentContext.requireStudentId(request), roomId, roleId));
    }

    @PostMapping("/training-rooms/{roomId}/start")
    public ApiResponse<TrainingRoom> startRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.startRoom(StudentContext.requireStudentId(request), roomId));
    }
}
