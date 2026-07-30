package com.qizhifu.jiaoxuepeiyu.student.course.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.course.StudentCourseService;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/courses")
@Tag(name = "Student Courses", description = "Student course learning APIs. Header X-User-Id identifies the student.")
public class StudentCourseController {

    private final StudentCourseService service;

    public StudentCourseController(StudentCourseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List student courses", description = "Returns published courses visible to the current student.")
    public ApiResponse<List<StudentCourseCard>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                     HttpServletRequest request) {
        return ApiResponse.ok(service.listCourses(StudentContext.requireStudentId(request), keyword));
    }

    @GetMapping("/{courseId}")
    @Operation(summary = "Get course detail", description = "Returns course detail, chapter content tree, and learning progress.")
    public ApiResponse<StudentCourseDetail> get(@PathVariable Long courseId, HttpServletRequest request) {
        return ApiResponse.ok(service.getCourseDetail(StudentContext.requireStudentId(request), courseId));
    }

    @PostMapping("/{courseId}/progress")
    @Operation(summary = "Update courseware progress", description = "Saves courseware study seconds and completion state for the current student.")
    public ApiResponse<Void> updateProgress(@PathVariable Long courseId,
                                            @Valid @RequestBody ProgressRequest body,
                                            HttpServletRequest request) {
        service.updateCoursewareProgress(
                StudentContext.requireStudentId(request),
                courseId,
                body.getContentId(),
                body.getStudiedSeconds(),
                body.isCompleted());
        return ApiResponse.ok(null);
    }

    public static class ProgressRequest {
        @NotNull
        private Long contentId;
        private int studiedSeconds;
        private boolean completed;

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public int getStudiedSeconds() {
            return studiedSeconds;
        }

        public void setStudiedSeconds(int studiedSeconds) {
            this.studiedSeconds = studiedSeconds;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}
