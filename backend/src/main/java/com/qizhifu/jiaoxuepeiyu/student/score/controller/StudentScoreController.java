package com.qizhifu.jiaoxuepeiyu.student.score.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.score.StudentScoreService;
import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/scores")
@Tag(name = "Student Scores", description = "Student comprehensive score APIs. Header X-User-Id identifies the student.")
public class StudentScoreController {

    private final StudentScoreService service;

    public StudentScoreController(StudentScoreService service) {
        this.service = service;
    }

    @GetMapping("/semester")
    @Operation(summary = "List semester scores", description = "Returns comprehensive semester scores for the current student.")
    public ApiResponse<List<StudentSemesterScore>> listSemesterScores(HttpServletRequest request) {
        return ApiResponse.ok(service.listSemesterScores(StudentContext.requireStudentId(request)));
    }
}
