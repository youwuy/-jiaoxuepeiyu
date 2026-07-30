package com.qizhifu.jiaoxuepeiyu.student.assignment.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.assignment.StudentAssignmentService;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentDetail;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentReport;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentSubmitResult;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/assignments")
public class StudentAssignmentController {

    private final StudentAssignmentService service;

    public StudentAssignmentController(StudentAssignmentService service) {
        this.service = service;
    }

    @GetMapping("/{assignmentId}")
    public ApiResponse<StudentAssignmentDetail> get(@PathVariable Long assignmentId, HttpServletRequest request) {
        return ApiResponse.ok(service.getAssignment(StudentContext.requireStudentId(request), assignmentId));
    }

    @PostMapping("/{assignmentId}/answers")
    public ApiResponse<Void> saveAnswers(@PathVariable Long assignmentId,
                                         @Valid @RequestBody AssignmentAnswerCommand body,
                                         HttpServletRequest request) {
        service.saveAnswers(StudentContext.requireStudentId(request), assignmentId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{assignmentId}/submit")
    public ApiResponse<StudentAssignmentSubmitResult> submit(@PathVariable Long assignmentId,
                                                             HttpServletRequest request) {
        return ApiResponse.ok(service.submit(StudentContext.requireStudentId(request), assignmentId));
    }

    @GetMapping("/{assignmentId}/report")
    public ApiResponse<StudentAssignmentReport> report(@PathVariable Long assignmentId, HttpServletRequest request) {
        return ApiResponse.ok(service.getReport(StudentContext.requireStudentId(request), assignmentId));
    }
}
