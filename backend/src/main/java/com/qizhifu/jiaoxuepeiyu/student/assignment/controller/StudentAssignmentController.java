package com.qizhifu.jiaoxuepeiyu.student.assignment.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.assignment.StudentAssignmentService;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentDetail;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentReport;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentSubmitResult;
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
@RequestMapping("/api/student/assignments")
@Tag(name = "Student Assignments", description = "Student theory assignment APIs. Header X-User-Id identifies the student.")
public class StudentAssignmentController {

    private final StudentAssignmentService service;

    public StudentAssignmentController(StudentAssignmentService service) {
        this.service = service;
    }

    @GetMapping("/{assignmentId}")
    @Operation(summary = "Get assignment detail", description = "Returns assignment questions, answers, and current submission status.")
    public ApiResponse<StudentAssignmentDetail> get(@PathVariable Long assignmentId, HttpServletRequest request) {
        return ApiResponse.ok(service.getAssignment(StudentContext.requireStudentId(request), assignmentId));
    }

    @PostMapping("/{assignmentId}/answers")
    @Operation(summary = "Save assignment answers", description = "Saves draft answers for an editable assignment and returns an empty success response.")
    public ApiResponse<Void> saveAnswers(@PathVariable Long assignmentId,
                                         @Valid @RequestBody AssignmentAnswerCommand body,
                                         HttpServletRequest request) {
        service.saveAnswers(StudentContext.requireStudentId(request), assignmentId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{assignmentId}/submit")
    @Operation(summary = "Submit assignment", description = "Submits the assignment, triggers objective scoring, and returns the submission result.")
    public ApiResponse<StudentAssignmentSubmitResult> submit(@PathVariable Long assignmentId,
                                                             HttpServletRequest request) {
        return ApiResponse.ok(service.submit(StudentContext.requireStudentId(request), assignmentId));
    }

    @PostMapping("/{assignmentId}/retry")
    @Operation(summary = "Retry assignment", description = "Clears the current answers and starts a new assignment attempt within the answer window.")
    public ApiResponse<Void> retry(@PathVariable Long assignmentId, HttpServletRequest request) {
        service.retry(StudentContext.requireStudentId(request), assignmentId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{assignmentId}/report")
    @Operation(summary = "Get assignment report", description = "Returns scoring report and answer details after submission.")
    public ApiResponse<StudentAssignmentReport> report(@PathVariable Long assignmentId, HttpServletRequest request) {
        return ApiResponse.ok(service.getReport(StudentContext.requireStudentId(request), assignmentId));
    }
}
