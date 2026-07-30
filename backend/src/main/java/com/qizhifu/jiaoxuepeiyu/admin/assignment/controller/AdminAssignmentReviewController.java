package com.qizhifu.jiaoxuepeiyu.admin.assignment.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.AdminAssignmentReviewService;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewLog;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/assignment-attempts")
@Tag(name = "Admin Assignment Review", description = "Teacher review APIs for submitted course assignments.")
public class AdminAssignmentReviewController {

    private final AdminAssignmentReviewService service;

    public AdminAssignmentReviewController(AdminAssignmentReviewService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List assignment attempts", description = "Returns paged student assignment attempts filtered by course, assignment, class, student, status, or keyword.")
    public ApiResponse<PageResponse<AdminAssignmentAttempt>> listAttempts(@ModelAttribute AdminAssignmentAttemptQuery query) {
        return ApiResponse.ok(service.listAttempts(query));
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get assignment attempt detail", description = "Returns submitted answers, standard answers, and current review scores for one attempt.")
    public ApiResponse<AdminAssignmentAttempt> getAttempt(@PathVariable Long attemptId) {
        return ApiResponse.ok(service.getAttempt(attemptId));
    }

    @PostMapping("/{attemptId}/review")
    @Operation(summary = "Review assignment attempt", description = "Persists per-question scores, marks the attempt REVIEWED, and refreshes course progress.")
    public ApiResponse<Void> reviewAttempt(@PathVariable Long attemptId,
                                           @RequestBody AdminAssignmentReviewCommand body,
                                           HttpServletRequest request) {
        service.reviewAttempt(attemptId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/{attemptId}/logs")
    @Operation(summary = "List review logs", description = "Returns review operation logs for one assignment attempt sorted by newest first.")
    public ApiResponse<List<AdminAssignmentReviewLog>> listReviewLogs(@PathVariable Long attemptId) {
        return ApiResponse.ok(service.listReviewLogs(attemptId));
    }
}
