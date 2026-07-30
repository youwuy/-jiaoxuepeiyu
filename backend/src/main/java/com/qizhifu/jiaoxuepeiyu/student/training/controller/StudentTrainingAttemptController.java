package com.qizhifu.jiaoxuepeiyu.student.training.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import com.qizhifu.jiaoxuepeiyu.student.training.StudentTrainingAttemptRequest;
import com.qizhifu.jiaoxuepeiyu.student.training.StudentTrainingAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/training-attempts")
@Tag(name = "Student Training Attempts", description = "Student training result submission and score sheet APIs.")
public class StudentTrainingAttemptController {

    private final StudentTrainingAttemptService service;

    public StudentTrainingAttemptController(StudentTrainingAttemptService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Submit training attempt", description = "Creates an immutable training archive from a student or UE client training result.")
    public ApiResponse<Long> submitAttempt(@Valid @RequestBody StudentTrainingAttemptRequest body,
                                           HttpServletRequest request) {
        return ApiResponse.ok(service.submitAttempt(StudentContext.requireStudentId(request), body));
    }

    @GetMapping("/{attemptId}/score-sheet")
    @Operation(summary = "Get training score sheet", description = "Returns the current student's training attempt score sheet and step details.")
    public ApiResponse<StudentTrainingArchiveDetail> getScoreSheet(@PathVariable Long attemptId,
                                                                  HttpServletRequest request) {
        return ApiResponse.ok(service.getScoreSheet(StudentContext.requireStudentId(request), attemptId));
    }
}
