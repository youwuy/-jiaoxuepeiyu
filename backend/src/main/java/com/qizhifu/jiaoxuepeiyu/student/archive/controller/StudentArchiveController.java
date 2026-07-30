package com.qizhifu.jiaoxuepeiyu.student.archive.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.archive.StudentArchiveService;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/archives")
public class StudentArchiveController {

    private final StudentArchiveService service;

    public StudentArchiveController(StudentArchiveService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StudentTrainingArchive>> list(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        return ApiResponse.ok(service.listArchives(StudentContext.requireStudentId(request), mode, keyword));
    }

    @GetMapping("/{archiveId}")
    public ApiResponse<StudentTrainingArchiveDetail> get(@PathVariable Long archiveId, HttpServletRequest request) {
        return ApiResponse.ok(service.getArchiveDetail(StudentContext.requireStudentId(request), archiveId));
    }
}
