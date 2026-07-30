package com.qizhifu.jiaoxuepeiyu.student.archive.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.archive.StudentArchiveService;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchive;
import com.qizhifu.jiaoxuepeiyu.student.archive.model.StudentTrainingArchiveDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/archives")
@Tag(name = "Student Archives", description = "Student training archive query APIs. Header X-User-Id identifies the student.")
public class StudentArchiveController {

    private final StudentArchiveService service;

    public StudentArchiveController(StudentArchiveService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List training archives", description = "Returns the current student's training archives filtered by mode or keyword.")
    public ApiResponse<List<StudentTrainingArchive>> list(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletRequest request) {
        return ApiResponse.ok(service.listArchives(StudentContext.requireStudentId(request), mode, keyword));
    }

    @GetMapping("/{archiveId}")
    @Operation(summary = "Get training archive detail", description = "Returns one training archive detail for the current student.")
    public ApiResponse<StudentTrainingArchiveDetail> get(@PathVariable Long archiveId, HttpServletRequest request) {
        return ApiResponse.ok(service.getArchiveDetail(StudentContext.requireStudentId(request), archiveId));
    }
}
