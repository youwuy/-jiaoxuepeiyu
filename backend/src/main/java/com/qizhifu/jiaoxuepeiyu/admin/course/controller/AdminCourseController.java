package com.qizhifu.jiaoxuepeiyu.admin.course.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.course.AdminCourseService;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourse;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseCommand;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseLog;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseQuery;
import com.qizhifu.jiaoxuepeiyu.admin.course.model.AdminCourseStatistics;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/courses")
@Tag(name = "Admin Courses", description = "Teaching course lifecycle, publication, content, statistics, and operation logs.")
public class AdminCourseController {

    private final AdminCourseService service;

    public AdminCourseController(AdminCourseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List courses", description = "Returns paged teaching courses filtered by name, term, major, class, teacher, teaching-time overlap, or publish status.")
    public ApiResponse<PageResponse<AdminCourse>> listCourses(@ModelAttribute AdminCourseQuery query) {
        return ApiResponse.ok(service.listCourses(query));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course detail", description = "Returns one course with teachers, classes, chapters, and content nodes.")
    public ApiResponse<AdminCourse> getCourse(@PathVariable Long courseId) {
        return ApiResponse.ok(service.getCourse(courseId));
    }

    @PostMapping
    @Operation(summary = "Create course", description = "Creates a draft teaching course with teachers, classes, chapters, courseware learning rules, and assignment nodes.")
    public ApiResponse<Long> createCourse(@RequestBody AdminCourseCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createCourse(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/{courseId}")
    @Operation(summary = "Update course", description = "Updates course metadata and fully replaces submitted teachers, classes, chapters, and content nodes.")
    public ApiResponse<Void> updateCourse(@PathVariable Long courseId,
                                          @RequestBody AdminCourseCommand body,
                                          HttpServletRequest request) {
        service.updateCourse(courseId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{courseId}/publish")
    @Operation(summary = "Publish course", description = "Publishes a non-empty course and sends course notifications to bound students.")
    public ApiResponse<Void> publishCourse(@PathVariable Long courseId, HttpServletRequest request) {
        service.publishCourse(courseId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{courseId}/cancel-publish")
    @Operation(summary = "Cancel course publish", description = "Marks a course offline while preserving learning and assignment records.")
    public ApiResponse<Void> cancelPublishCourse(@PathVariable Long courseId, HttpServletRequest request) {
        service.cancelPublishCourse(courseId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{courseId}/delete")
    @Operation(summary = "Delete course", description = "Soft deletes a course and takes it offline.")
    public ApiResponse<Void> deleteCourse(@PathVariable Long courseId, HttpServletRequest request) {
        service.deleteCourse(courseId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/{courseId}/copy")
    @Operation(summary = "Copy course", description = "Copies a course as a draft with its teachers, classes, chapters, and content structure.")
    public ApiResponse<Long> copyCourse(@PathVariable Long courseId, HttpServletRequest request) {
        return ApiResponse.ok(service.copyCourse(courseId, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/{courseId}/statistics")
    @Operation(summary = "Get course statistics", description = "Returns student completion counts, pending review count, and average assignment score.")
    public ApiResponse<AdminCourseStatistics> getStatistics(@PathVariable Long courseId) {
        return ApiResponse.ok(service.getStatistics(courseId));
    }

    @GetMapping("/{courseId}/logs")
    @Operation(summary = "List course logs", description = "Returns operation logs for one course sorted by newest first.")
    public ApiResponse<List<AdminCourseLog>> listCourseLogs(@PathVariable Long courseId) {
        return ApiResponse.ok(service.listCourseLogs(courseId));
    }
}
