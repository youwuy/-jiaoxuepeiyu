package com.qizhifu.jiaoxuepeiyu.student.message.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.message.StudentMessageService;
import com.qizhifu.jiaoxuepeiyu.student.message.StudentMessageSummary;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/messages")
public class StudentMessageController {

    private final StudentMessageService service;

    public StudentMessageController(StudentMessageService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<StudentMessageSummary> list(HttpServletRequest request) {
        return ApiResponse.ok(service.listMessages(StudentContext.requireStudentId(request)));
    }

    @PostMapping("/{messageId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long messageId, HttpServletRequest request) {
        service.markRead(StudentContext.requireStudentId(request), messageId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(HttpServletRequest request) {
        service.markAllRead(StudentContext.requireStudentId(request));
        return ApiResponse.ok(null);
    }
}
