package com.qizhifu.jiaoxuepeiyu.admin.iam.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.iam.AdminIamService;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRolePermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Admin IAM", description = "Role, permission, data scope, and role operation log APIs.")
public class AdminIamController {

    private final AdminIamService service;

    public AdminIamController(AdminIamService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/permissions/tree")
    @Operation(summary = "List permission tree", description = "Returns menu, page, and button permissions assembled from the parent-child permission table.")
    public ApiResponse<List<AdminPermission>> listPermissionTree() {
        return ApiResponse.ok(service.listPermissionTree());
    }

    @PostMapping("/api/admin/permissions")
    @Operation(summary = "Create permission node", description = "Creates a menu, page, or button permission node for role authorization.")
    public ApiResponse<Long> createPermission(@RequestBody AdminPermissionCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createPermission(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/api/admin/permissions/{permissionId}")
    @Operation(summary = "Update permission node", description = "Updates permission metadata and hierarchy without changing role bindings.")
    public ApiResponse<Void> updatePermission(@PathVariable Long permissionId,
                                              @RequestBody AdminPermissionCommand body,
                                              HttpServletRequest request) {
        service.updatePermission(permissionId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/permissions/{permissionId}/enable")
    @Operation(summary = "Enable permission node", description = "Marks a permission node visible for menu and authorization tree rendering.")
    public ApiResponse<Void> enablePermission(@PathVariable Long permissionId, HttpServletRequest request) {
        service.enablePermission(permissionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/permissions/{permissionId}/disable")
    @Operation(summary = "Disable permission node", description = "Hides a permission node while preserving role permission bindings.")
    public ApiResponse<Void> disablePermission(@PathVariable Long permissionId, HttpServletRequest request) {
        service.disablePermission(permissionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/permissions/{permissionId}/delete")
    @Operation(summary = "Delete permission node", description = "Deletes an unbound leaf permission node from the permission tree.")
    public ApiResponse<Void> deletePermission(@PathVariable Long permissionId, HttpServletRequest request) {
        service.deletePermission(permissionId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/permissions/sort")
    @Operation(summary = "Update permission sort order", description = "Persists drag-sort order for menu, page, or button permission nodes within their current parent group.")
    public ApiResponse<Void> updatePermissionSorts(@RequestBody AdminPermissionSortCommand body,
                                                   HttpServletRequest request) {
        service.updatePermissionSorts(body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/admin/roles")
    @Operation(summary = "List roles", description = "Returns paged roles filtered by role name/code keyword or enabled status.")
    public ApiResponse<PageResponse<AdminRole>> listRoles(@ModelAttribute AdminRoleQuery query) {
        return ApiResponse.ok(service.listRoles(query));
    }

    @GetMapping("/api/admin/roles/{roleId}")
    @Operation(summary = "Get role detail", description = "Returns one role with bound permission ids and per-page data scopes.")
    public ApiResponse<AdminRole> getRole(@PathVariable Long roleId) {
        return ApiResponse.ok(service.getRole(roleId));
    }

    @PostMapping("/api/admin/roles")
    @Operation(summary = "Create role", description = "Creates an enabled role, binds submitted permissions, and writes a role operation log.")
    public ApiResponse<Long> createRole(@RequestBody AdminRoleCommand body, HttpServletRequest request) {
        return ApiResponse.ok(service.createRole(body, AdminContext.requireAdminId(request)));
    }

    @PutMapping("/api/admin/roles/{roleId}")
    @Operation(summary = "Update role", description = "Updates role metadata, permission bindings, and per-page data scopes.")
    public ApiResponse<Void> updateRole(@PathVariable Long roleId,
                                        @RequestBody AdminRoleCommand body,
                                        HttpServletRequest request) {
        service.updateRole(roleId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/roles/{roleId}/enable")
    @Operation(summary = "Enable role", description = "Enables the selected role while preserving its user and permission bindings.")
    public ApiResponse<Void> enableRole(@PathVariable Long roleId, HttpServletRequest request) {
        service.enableRole(roleId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/roles/{roleId}/disable")
    @Operation(summary = "Disable role", description = "Disables the selected role while preserving historical bindings and logs.")
    public ApiResponse<Void> disableRole(@PathVariable Long roleId, HttpServletRequest request) {
        service.disableRole(roleId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/admin/roles/{roleId}/delete")
    @Operation(summary = "Delete role", description = "Soft deletes the selected role and disables it for future use.")
    public ApiResponse<Void> deleteRole(@PathVariable Long roleId, HttpServletRequest request) {
        service.deleteRole(roleId, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/roles/{roleId}/permissions")
    @Operation(summary = "Update role permissions", description = "Fully replaces permission bindings and per-page data scopes for one role.")
    public ApiResponse<Void> updateRolePermissions(@PathVariable Long roleId,
                                                   @RequestBody AdminRolePermissionCommand body,
                                                   HttpServletRequest request) {
        service.updateRolePermissions(roleId, body, AdminContext.requireAdminId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/admin/roles/{roleId}/logs")
    @Operation(summary = "List role logs", description = "Returns operation logs for one role sorted by newest first.")
    public ApiResponse<List<AdminRoleLog>> listRoleLogs(@PathVariable Long roleId) {
        return ApiResponse.ok(service.listRoleLogs(roleId));
    }
}
