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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Account", description = "Teacher and student account management APIs.")
public class AdminAccountController {

    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    @Operation(summary = "List teacher accounts", description = "Returns paged teacher accounts with masked sensitive fields.")
    public ApiResponse<PageResponse<AdminAccount>> listTeachers(@ModelAttribute AdminAccountQuery query) {
        return ApiResponse.ok(service.listTeachers(query));
    }

    @GetMapping("/students")
    @Operation(summary = "List student accounts", description = "Returns paged student accounts with class, organization, and masked sensitive fields.")
    public ApiResponse<PageResponse<AdminAccount>> listStudents(@ModelAttribute AdminAccountQuery query) {
        return ApiResponse.ok(service.listStudents(query));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get account detail", description = "Returns one account detail with phone and ID card masked.")
    public ApiResponse<AdminAccount> get(@PathVariable Long userId) {
        return ApiResponse.ok(service.get(userId));
    }

    @PostMapping("/teachers")
    @Operation(summary = "Create teacher account", description = "Creates a teacher account with hashed configured initial password and returns the new user id.")
    public ApiResponse<Long> createTeacher(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createTeacher(body));
    }

    @PostMapping("/students")
    @Operation(summary = "Create student account", description = "Creates a student account bound to a class and returns the new user id.")
    public ApiResponse<Long> createStudent(@RequestBody AdminAccountCommand body) {
        return ApiResponse.ok(service.createStudent(body));
    }

    @PutMapping("/teachers/{userId}")
    @Operation(summary = "Update teacher account", description = "Updates editable teacher profile fields and replaces role, managed organization, and teaching class bindings.")
    public ApiResponse<Void> updateTeacher(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateTeacher(userId, body);
        return ApiResponse.ok(null);
    }

    @PutMapping("/students/{userId}")
    @Operation(summary = "Update student account", description = "Updates editable student profile fields without changing the account number.")
    public ApiResponse<Void> updateStudent(@PathVariable Long userId, @RequestBody AdminAccountCommand body) {
        service.updateStudent(userId, body);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/enable")
    @Operation(summary = "Enable account", description = "Enables login for the selected account.")
    public ApiResponse<Void> enable(@PathVariable Long userId) {
        service.enable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{userId}/disable")
    @Operation(summary = "Disable account", description = "Disables login for the selected account.")
    public ApiResponse<Void> disable(@PathVariable Long userId) {
        service.disable(userId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/reset-password")
    @Operation(summary = "Batch reset passwords", description = "Resets selected accounts to the configured initial password hash without returning plaintext passwords.")
    public ApiResponse<Void> resetPasswords(@RequestBody AccountIdsCommand body) {
        service.resetPasswords(body.getUserIds());
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch/org")
    @Operation(summary = "Batch update organization", description = "Updates the belonging organization for selected accounts.")
    public ApiResponse<Void> updateOrg(@RequestBody BatchOrgCommand body) {
        service.updateOrg(body.getUserIds(), body.getOrgId());
        return ApiResponse.ok(null);
    }

    @PutMapping("/teachers/{userId}/roles")
    @Operation(summary = "Update teacher roles", description = "Replaces the teacher role bindings and returns an empty success response.")
    public ApiResponse<Void> updateRoles(@PathVariable Long userId, @RequestBody RoleBindingCommand body) {
        service.updateRoles(userId, body.getRoleIds());
        return ApiResponse.ok(null);
    }
}
