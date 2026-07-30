package com.qizhifu.jiaoxuepeiyu.student.course.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.course.StudentCourseService;
import com.qizhifu.jiaoxuepeiyu.student.course.model.StudentCourseCard;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    private final StudentCourseService service;

    public StudentCourseController(StudentCourseService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<StudentCourseCard>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                     HttpServletRequest request) {
        return ApiResponse.ok(service.listCourses(StudentContext.requireStudentId(request), keyword));
    }
}
