package com.qizhifu.jiaoxuepeiyu.admin.config.controller;

import com.qizhifu.jiaoxuepeiyu.admin.config.AdminFacilityConfigService;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroom;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminClassroomCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/classrooms")
@Tag(name = "Admin Facility Config", description = "Classroom and NVR camera configuration APIs.")
public class AdminFacilityConfigController {

    private final AdminFacilityConfigService service;

    public AdminFacilityConfigController(AdminFacilityConfigService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List classrooms", description = "Returns each classroom's administrator-maintained fixed device count and configured NVR camera metadata.")
    public ApiResponse<List<AdminClassroom>> list() {
        return ApiResponse.ok(service.listClassrooms());
    }

    @GetMapping("/{classroomId}")
    @Operation(summary = "Get classroom detail", description = "Returns one classroom, its fixed device count, and its camera list.")
    public ApiResponse<AdminClassroom> get(@PathVariable Long classroomId) {
        return ApiResponse.ok(service.getClassroom(classroomId));
    }

    @PostMapping
    @Operation(summary = "Create classroom", description = "Creates a classroom with a fixed device count and at least one NVR camera, then returns the new classroom id.")
    public ApiResponse<Long> create(@RequestBody AdminClassroomCommand body) {
        return ApiResponse.ok(service.createClassroom(body));
    }

    @PutMapping("/{classroomId}")
    @Operation(summary = "Update classroom", description = "Updates classroom name and fixed device count, then replaces the submitted camera list.")
    public ApiResponse<Void> update(@PathVariable Long classroomId, @RequestBody AdminClassroomCommand body) {
        service.updateClassroom(classroomId, body);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{classroomId}")
    @Operation(summary = "Delete classroom", description = "Deletes the classroom and its camera records.")
    public ApiResponse<Void> delete(@PathVariable Long classroomId) {
        service.deleteClassroom(classroomId);
        return ApiResponse.ok(null);
    }
}
