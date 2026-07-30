package com.qizhifu.jiaoxuepeiyu.student.message.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.message.StudentMessageService;
import com.qizhifu.jiaoxuepeiyu.student.message.StudentMessageSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/messages")
@Tag(name = "Student Messages", description = "Student notification and read-state APIs. Header X-User-Id identifies the student.")
public class StudentMessageController {

    private final StudentMessageService service;

    public StudentMessageController(StudentMessageService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List messages", description = "Returns the current student's notifications and unread count.")
    public ApiResponse<StudentMessageSummary> list(HttpServletRequest request) {
        return ApiResponse.ok(service.listMessages(StudentContext.requireStudentId(request)));
    }

    @PostMapping("/{messageId}/read")
    @Operation(summary = "Mark message as read", description = "Marks one student notification as read.")
    public ApiResponse<Void> markRead(@PathVariable Long messageId, HttpServletRequest request) {
        service.markRead(StudentContext.requireStudentId(request), messageId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all messages as read", description = "Marks all unread notifications for the current student as read.")
    public ApiResponse<Void> markAllRead(HttpServletRequest request) {
        service.markAllRead(StudentContext.requireStudentId(request));
        return ApiResponse.ok(null);
    }
}
