package com.qizhifu.jiaoxuepeiyu.student.resource.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.resource.StudentResourceService;
import com.qizhifu.jiaoxuepeiyu.student.resource.model.PublicResourceCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/resources")
@Tag(name = "Student Resources", description = "Student public resource query APIs.")
public class StudentResourceController {

    private final StudentResourceService service;

    public StudentResourceController(StudentResourceService service) {
        this.service = service;
    }

    @GetMapping("/public")
    @Operation(summary = "List public resources", description = "Returns public resources filtered by keyword, resource type, or major.")
    public ApiResponse<List<PublicResourceCard>> listPublicResources(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "majorId", required = false) Long majorId) {
        return ApiResponse.ok(service.listPublicResources(keyword, resourceType, majorId));
    }
}
