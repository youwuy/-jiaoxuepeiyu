package com.qizhifu.jiaoxuepeiyu.student.training.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.training.StudentTrainingService;
import com.qizhifu.jiaoxuepeiyu.student.training.model.StudentTraining;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingAppInstallation;
import com.qizhifu.jiaoxuepeiyu.student.training.model.TrainingRoom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Student Training", description = "Student training center and team room APIs. Header X-User-Id identifies the student.")
public class StudentTrainingController {

    private final StudentTrainingService service;

    public StudentTrainingController(StudentTrainingService service) {
        this.service = service;
    }

    @GetMapping("/trainings")
    @Operation(summary = "List trainings", description = "Returns published trainings visible to the current student.")
    public ApiResponse<List<StudentTraining>> listTrainings(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        return ApiResponse.ok(service.listTrainings(StudentContext.requireStudentId(request), mode, keyword));
    }

    @GetMapping("/trainings/app-installation")
    @Operation(summary = "Get training app installation", description = "Returns UE training application installation status for the current student.")
    public ApiResponse<TrainingAppInstallation> appInstallation(HttpServletRequest request) {
        return ApiResponse.ok(service.getAppInstallation(StudentContext.requireStudentId(request)));
    }

    @PostMapping("/trainings/{trainingId}/rooms")
    @Operation(summary = "Create training room", description = "Creates a team training room for the current student and returns room state.")
    public ApiResponse<TrainingRoom> createRoom(@PathVariable Long trainingId, HttpServletRequest request) {
        return ApiResponse.ok(service.createRoom(StudentContext.requireStudentId(request), trainingId));
    }

    @GetMapping("/training-rooms/{roomId}")
    @Operation(summary = "Get training room", description = "Returns room members and role claims for the current student.")
    public ApiResponse<TrainingRoom> getRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.getRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/join")
    @Operation(summary = "Join training room", description = "Adds the current student to a waiting team room and returns room state.")
    public ApiResponse<TrainingRoom> joinRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.joinRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/leave")
    @Operation(summary = "Leave training room", description = "Removes the current student from a team room and dissolves waiting rooms when the owner leaves.")
    public ApiResponse<TrainingRoom> leaveRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.leaveRoom(StudentContext.requireStudentId(request), roomId));
    }

    @PostMapping("/training-rooms/{roomId}/roles/{roleId}/claim")
    @Operation(summary = "Claim training role", description = "Claims a room role for the current student and returns updated room state.")
    public ApiResponse<TrainingRoom> claimRole(@PathVariable Long roomId,
                                               @PathVariable Long roleId,
                                               HttpServletRequest request) {
        return ApiResponse.ok(service.claimRole(StudentContext.requireStudentId(request), roomId, roleId));
    }

    @PostMapping("/training-rooms/{roomId}/start")
    @Operation(summary = "Start training room", description = "Starts a waiting team room after owner, capacity, and role completion checks.")
    public ApiResponse<TrainingRoom> startRoom(@PathVariable Long roomId, HttpServletRequest request) {
        return ApiResponse.ok(service.startRoom(StudentContext.requireStudentId(request), roomId));
    }
}
