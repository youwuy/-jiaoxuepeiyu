package com.qizhifu.jiaoxuepeiyu.admin.config.controller;

import com.qizhifu.jiaoxuepeiyu.admin.config.AdminEducationConfigService;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYear;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminAcademicYearCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClass;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajor;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminMajorCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
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
public class AdminEducationConfigController {

    private final AdminEducationConfigService service;

    public AdminEducationConfigController(AdminEducationConfigService service) {
        this.service = service;
    }

    @GetMapping("/academic-years")
    public ApiResponse<List<AdminAcademicYear>> listAcademicYears() {
        return ApiResponse.ok(service.listAcademicYears());
    }

    @PostMapping("/academic-years")
    public ApiResponse<Long> createAcademicYear(@Valid @RequestBody AdminAcademicYearCommand body) {
        return ApiResponse.ok(service.createAcademicYear(body));
    }

    @PostMapping("/semesters/{semesterId}/current")
    public ApiResponse<Void> setCurrentSemester(@PathVariable Long semesterId) {
        service.setCurrentSemester(semesterId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/majors")
    public ApiResponse<List<AdminMajor>> listMajors() {
        return ApiResponse.ok(service.listMajors());
    }

    @PostMapping("/majors")
    public ApiResponse<Long> createMajor(@Valid @RequestBody AdminMajorCommand body) {
        return ApiResponse.ok(service.createMajor(body));
    }

    @PostMapping("/majors/{majorId}/enable")
    public ApiResponse<Void> enableMajor(@PathVariable Long majorId) {
        service.enableMajor(majorId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/majors/{majorId}/disable")
    public ApiResponse<Void> disableMajor(@PathVariable Long majorId) {
        service.disableMajor(majorId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/classes")
    public ApiResponse<List<AdminClass>> listClasses(@RequestParam(value = "majorId", required = false) Long majorId) {
        return ApiResponse.ok(service.listClasses(majorId));
    }

    @PostMapping("/classes")
    public ApiResponse<Long> createClass(@Valid @RequestBody AdminClassCommand body) {
        return ApiResponse.ok(service.createClass(body));
    }
}
