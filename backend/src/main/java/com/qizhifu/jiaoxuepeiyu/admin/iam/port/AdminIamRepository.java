package com.qizhifu.jiaoxuepeiyu.admin.iam.port;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import java.util.List;

public interface AdminIamRepository {

    List<AdminPermission> findPermissions();

    AdminPermission findPermission(Long permissionId);

    Long findPermissionIdByCode(String permissionCode);

    Long createPermission(AdminPermissionCommand command);

    void updatePermission(Long permissionId, AdminPermissionCommand command);

    void updatePermissionStatus(Long permissionId, boolean visible);

    void deletePermission(Long permissionId);

    int countPermissionChildren(Long permissionId);

    int countPermissionRoleBindings(Long permissionId);

    List<AdminRole> findRoles(AdminRoleQuery query);

    long countRoles(AdminRoleQuery query);

    AdminRole findRole(Long roleId);

    Long createRole(AdminRoleCommand command);

    void updateRole(Long roleId, AdminRoleCommand command);

    void updateStatus(Long roleId, boolean enabled);

    void deleteRole(Long roleId);

    void replacePermissions(Long roleId, List<Long> permissionIds, String dataScope);

    void appendRoleLog(Long roleId, Long operatorId, String action, String content);

    List<AdminRoleLog> findRoleLogs(Long roleId);
}
