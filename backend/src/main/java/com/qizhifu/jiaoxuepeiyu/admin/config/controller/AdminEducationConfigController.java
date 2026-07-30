package com.qizhifu.jiaoxuepeiyu.admin.config.controller;

import com.qizhifu.jiaoxuepeiyu.admin.config.AdminEducationConfigService;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYearCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajorCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Education Config", description = "Academic year, semester, major, and class configuration APIs.")
public class AdminEducationConfigController {

    private final AdminEducationConfigService service;

    public AdminEducationConfigController(AdminEducationConfigService service) {
        this.service = service;
    }

    @GetMapping("/academic-years")
    @Operation(summary = "List academic years", description = "Returns academic years with their semesters.")
    public ApiResponse<List<AdminAcademicYear>> listAcademicYears() {
        return ApiResponse.ok(service.listAcademicYears());
    }

    @PostMapping("/academic-years")
    @Operation(summary = "Create academic year", description = "Creates an academic year and automatically creates FIRST and SECOND semesters.")
    public ApiResponse<Long> createAcademicYear(@Valid @RequestBody AdminAcademicYearCommand body) {
        return ApiResponse.ok(service.createAcademicYear(body));
    }

    @PostMapping("/semesters/{semesterId}/current")
    @Operation(summary = "Set current semester", description = "Clears existing current semester flags and marks the requested semester as current.")
    public ApiResponse<Void> setCurrentSemester(@PathVariable Long semesterId) {
        service.setCurrentSemester(semesterId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/majors")
    @Operation(summary = "List majors", description = "Returns all majors with enabled status.")
    public ApiResponse<List<AdminMajor>> listMajors() {
        return ApiResponse.ok(service.listMajors());
    }

    @PostMapping("/majors")
    @Operation(summary = "Create major", description = "Creates a major and returns the new major id.")
    public ApiResponse<Long> createMajor(@Valid @RequestBody AdminMajorCommand body) {
        return ApiResponse.ok(service.createMajor(body));
    }

    @PostMapping("/majors/{majorId}/enable")
    @Operation(summary = "Enable major", description = "Enables a major for selection.")
    public ApiResponse<Void> enableMajor(@PathVariable Long majorId) {
        service.enableMajor(majorId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/majors/{majorId}/disable")
    @Operation(summary = "Disable major", description = "Disables a major for selection.")
    public ApiResponse<Void> disableMajor(@PathVariable Long majorId) {
        service.disableMajor(majorId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/classes")
    @Operation(summary = "List classes", description = "Returns classes, optionally filtered by major id.")
    public ApiResponse<List<AdminClass>> listClasses(@RequestParam(value = "majorId", required = false) Long majorId) {
        return ApiResponse.ok(service.listClasses(majorId));
    }

    @PostMapping("/classes")
    @Operation(summary = "Create class", description = "Creates a class under a major and returns the new class id.")
    public ApiResponse<Long> createClass(@Valid @RequestBody AdminClassCommand body) {
        return ApiResponse.ok(service.createClass(body));
    }
}
