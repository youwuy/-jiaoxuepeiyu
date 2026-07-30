package com.qizhifu.jiaoxuepeiyu.admin.account.controller;

import com.qizhifu.jiaoxuepeiyu.admin.account.AdminAccountService;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AccountIdsCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.BatchOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.RoleBindingCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    public ApiResponse<PageResponse<AdminAccount>> listTeachers(@ModelAttribute AdminAccountQuery query) {
        return ApiResponse.ok(service.listTeachers(query));
    }

    @GetMapping("/students")
    public ApiResponse<PageResponse<AdminAccount>> listStudents(@ModelAttribute AdminAccountQuery query) {
        return ApiResponse.ok(service.listStudents(query));
    }

    @GetMapping("/{userId}")
    public ApiResponse<AdminAccount> get(@PathVariable Long userId) {
        return ApiResponse.ok(service.get(userId));
    }

    @PostMapping("/teachers")
    public ApiResponse<Long> createTeacher(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createTeacher(body));
    }

    @PostMapping("/students")
    public ApiResponse<Long> createStudent(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createStudent(body));
    }

    @PutMapping("/teachers/{userId}")
    public ApiResponse<Void> updateTeacher(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateTeacher(userId, body);
        return ApiResponse.ok(null);
    }

    @PutMapping("/students/{userId}")
    public ApiResponse<Void> updateStudent(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateStudent(userId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/enable")
    public ApiResponse<Void> enable(@PathVariable Long userId) {
        service.enable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/disable")
    public ApiResponse<Void> disable(@PathVariable Long userId) {
        service.disable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/reset-password")
    public ApiResponse<Void> resetPasswords(@RequestBody AccountIdsCommand body) {
        service.resetPasswords(body.getUserIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/org")
    public ApiResponse<Void> updateOrg(@RequestBody BatchOrgCommand body) {
        service.updateOrg(body.getUserIds(), body.getOrgId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/teachers/{userId}/roles")
    public ApiResponse<Void> updateRoles(@PathVariable Long userId, @RequestBody RoleBindingCommand body) {
        service.updateRoles(userId, body.getRoleIds());
        return ApiResponse.ok(null);
    }
}
