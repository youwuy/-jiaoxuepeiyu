package com.qizhifu.jiaoxuepeiyu.admin.iam.repository;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import com.qizhifu.jiaoxuepeiyu.admin.iam.port.AdminIamRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminIamRepository implements AdminIamRepository {

    private final AdminIamMapper mapper;

    public MyBatisAdminIamRepository(AdminIamMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminPermission> findPermissions() {
        return mapper.findPermissions();
    }

    @Override
    public AdminPermission findPermission(Long permissionId) {
        return mapper.findPermission(permissionId);
    }

    @Override
    public Long findPermissionIdByCode(String permissionCode) {
        return mapper.findPermissionIdByCode(permissionCode);
    }

    @Override
    public Long createPermission(AdminPermissionCommand command) {
        mapper.insertPermission(command);
        return command.getPermissionId();
    }

    @Override
    public void updatePermission(Long permissionId, AdminPermissionCommand command) {
        command.setPermissionId(permissionId);
        mapper.updatePermission(command);
    }

    @Override
    public void updatePermissionStatus(Long permissionId, boolean visible) {
        mapper.updatePermissionStatus(permissionId, visible ? 1 : 0);
    }

    @Override
    public void deletePermission(Long permissionId) {
        mapper.deletePermission(permissionId);
    }

    @Override
    public int countPermissionChildren(Long permissionId) {
        return mapper.countPermissionChildren(permissionId);
    }

    @Override
    public int countPermissionRoleBindings(Long permissionId) {
        return mapper.countPermissionRoleBindings(permissionId);
    }

    @Override
    public List<AdminRole> findRoles(AdminRoleQuery query) {
        List<AdminRole> roles = mapper.findRoles(likeQuery(query));
        for (AdminRole role : roles) {
            role.setPermissionIds(mapper.findPermissionIds(role.getRoleId()));
        }
        return roles;
    }

    @Override
    public long countRoles(AdminRoleQuery query) {
        return mapper.countRoles(likeQuery(query));
    }

    @Override
    public AdminRole findRole(Long roleId) {
        AdminRole role = mapper.findRole(roleId);
        if (role != null) {
            role.setPermissionIds(mapper.findPermissionIds(roleId));
        }
        return role;
    }

    @Override
    public Long createRole(AdminRoleCommand command) {
        AdminRole role = toRole(command);
        mapper.insertRole(role);
        return role.getRoleId();
    }

    @Override
    public void updateRole(Long roleId, AdminRoleCommand command) {
        AdminRole role = toRole(command);
        role.setRoleId(roleId);
        mapper.updateRole(role);
    }

    @Override
    public void updateStatus(Long roleId, boolean enabled) {
        mapper.updateStatus(roleId, enabled ? 1 : 0);
    }

    @Override
    public void deleteRole(Long roleId) {
        mapper.deleteRole(roleId);
    }

    @Override
    public void replacePermissions(Long roleId, List<Long> permissionIds, String dataScope) {
        mapper.deletePermissions(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            mapper.insertPermissions(roleId, permissionIds, dataScope);
        }
    }

    @Override
    public void appendRoleLog(Long roleId, Long operatorId, String action, String content) {
        mapper.insertRoleLog(roleId, operatorId, action, content);
    }

    @Override
    public List<AdminRoleLog> findRoleLogs(Long roleId) {
        return mapper.findRoleLogs(roleId);
    }

    private AdminRole toRole(AdminRoleCommand command) {
        AdminRole role = new AdminRole();
        role.setRoleName(command.getRoleName());
        role.setRoleCode(command.getRoleCode());
        role.setDataScope(command.getDataScope());
        role.setRemark(command.getRemark());
        role.setEnabled(Boolean.TRUE);
        return role;
    }

    private AdminRoleQuery likeQuery(AdminRoleQuery source) {
        AdminRoleQuery query = new AdminRoleQuery();
        query.setKeyword(source.getKeyword() == null ? null : "%" + source.getKeyword() + "%");
        query.setEnabled(source.getEnabled());
        query.setPage(source.getPage());
        query.setPageSize(source.getPageSize());
        return query;
    }
}
