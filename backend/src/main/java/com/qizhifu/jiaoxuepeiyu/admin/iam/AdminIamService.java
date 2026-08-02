package com.qizhifu.jiaoxuepeiyu.admin.iam;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionSortItem;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRolePermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import com.qizhifu.jiaoxuepeiyu.admin.iam.port.AdminIamRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminIamService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_PERMISSION_NAME_LENGTH = 8;
    private static final int MAX_ROUTE_PATH_LENGTH = 100;
    private static final Set<String> DATA_SCOPES = new HashSet<String>(
            Arrays.asList("SELF", "ORG_ONLY", "ALL", "PERSONAL", "MANAGED_ORG"));
    private static final Set<String> PERMISSION_TYPES = new HashSet<String>(
            Arrays.asList("MENU", "PAGE", "BUTTON"));

    private final AdminIamRepository repository;

    public AdminIamService(AdminIamRepository repository) {
        this.repository = repository;
    }

    public List<AdminPermission> listPermissionTree() {
        List<AdminPermission> permissions = repository.findPermissions();
        Map<Long, AdminPermission> byId = new HashMap<Long, AdminPermission>();
        for (AdminPermission permission : permissions) {
            permission.setChildren(new ArrayList<AdminPermission>());
            byId.put(permission.getPermissionId(), permission);
        }
        List<AdminPermission> roots = new ArrayList<AdminPermission>();
        for (AdminPermission permission : permissions) {
            if (permission.getParentId() == null || !byId.containsKey(permission.getParentId())) {
                roots.add(permission);
            } else {
                byId.get(permission.getParentId()).getChildren().add(permission);
            }
        }
        return roots;
    }

    @Transactional
    public Long createPermission(AdminPermissionCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminPermissionCommand normalized = normalizedPermission(command, null);
        assertPermissionHierarchy(normalized, null);
        assertPermissionNameAvailable(normalized.getPermissionName(), normalized.getParentId(), null);
        assertPermissionCodeAvailable(normalized.getPermissionCode(), null);
        assertPermissionRoutePathAvailable(normalized.getRoutePath(), null);
        return repository.createPermission(normalized);
    }

    @Transactional
    public void updatePermission(Long permissionId, AdminPermissionCommand command, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        AdminPermissionCommand normalized = normalizedPermission(command, permissionId);
        assertPermissionHierarchy(normalized, permissionId);
        assertPermissionNameAvailable(normalized.getPermissionName(), normalized.getParentId(), permissionId);
        assertPermissionCodeAvailable(normalized.getPermissionCode(), permissionId);
        assertPermissionRoutePathAvailable(normalized.getRoutePath(), permissionId);
        repository.updatePermission(permissionId, normalized);
    }

    @Transactional
    public void enablePermission(Long permissionId, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        for (Long id : collectPermissionSubtreeIds(permissionId)) {
            repository.updatePermissionStatus(id, true);
        }
    }

    @Transactional
    public void disablePermission(Long permissionId, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        for (Long id : collectPermissionSubtreeIds(permissionId)) {
            repository.updatePermissionStatus(id, false);
        }
    }

    @Transactional
    public void deletePermission(Long permissionId, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        if (repository.countPermissionChildren(permissionId) > 0) {
            throw new BusinessException(400, "Permission has child nodes");
        }
        if (repository.countPermissionRoleBindings(permissionId) > 0) {
            throw new BusinessException(400, "Permission is bound to roles");
        }
        repository.deletePermission(permissionId);
    }

    @Transactional
    public void updatePermissionSorts(AdminPermissionSortCommand command, Long operatorId) {
        requireOperator(operatorId);
        if (command == null || command.getItems() == null) {
            return;
        }
        Set<Long> seen = new HashSet<Long>();
        for (AdminPermissionSortItem item : command.getItems()) {
            if (item == null || item.getPermissionId() == null || item.getPermissionId().longValue() <= 0) {
                continue;
            }
            if (seen.contains(item.getPermissionId())) {
                continue;
            }
            seen.add(item.getPermissionId());
            AdminPermission permission = getPermission(item.getPermissionId());
            if (item.getParentId() != null && !item.getParentId().equals(permission.getParentId())) {
                throw new BusinessException(400, "Permission parent cannot be changed by sorting");
            }
            repository.updatePermissionSort(item.getPermissionId(), item.getSortOrder() == null ? 0 : item.getSortOrder());
        }
    }

    public PageResponse<AdminRole> listRoles(AdminRoleQuery query) {
        AdminRoleQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminRole>(
                repository.findRoles(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countRoles(normalized));
    }

    public AdminRole getRole(Long roleId) {
        AdminRole role = repository.findRole(roleId);
        if (role == null) {
            throw new BusinessException(404, "Role not found");
        }
        return role;
    }

    private AdminPermission getPermission(Long permissionId) {
        AdminPermission permission = repository.findPermission(permissionId);
        if (permission == null) {
            throw new BusinessException(404, "Permission not found");
        }
        return permission;
    }

    @Transactional
    public Long createRole(AdminRoleCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminRoleCommand normalized = normalizedRole(command);
        assertNotReservedSuperAdmin(normalized);
        assertRoleNameAvailable(normalized.getRoleName(), null);
        assertRoleCodeAvailable(normalized.getRoleCode(), null);
        Long roleId = repository.createRole(normalized);
        repository.replacePermissions(roleId, normalized.getPermissionIds(), normalized.getDataScope());
        repository.appendRoleLog(roleId, operatorId, "CREATE", "Create role");
        return roleId;
    }

    @Transactional
    public void updateRole(Long roleId, AdminRoleCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        assertMutableRole(role);
        AdminRoleCommand normalized = normalizedRole(command);
        assertNotReservedSuperAdmin(normalized);
        assertRoleNameAvailable(normalized.getRoleName(), roleId);
        assertRoleCodeAvailable(normalized.getRoleCode(), roleId);
        repository.updateRole(roleId, normalized);
        repository.replacePermissions(roleId, normalized.getPermissionIds(), normalized.getDataScope());
        repository.appendRoleLog(roleId, operatorId, "UPDATE", "Update role");
    }

    @Transactional
    public void enableRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        assertMutableRole(role);
        repository.updateStatus(roleId, true);
        repository.appendRoleLog(roleId, operatorId, "ENABLE", "Enable role");
    }

    @Transactional
    public void disableRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        assertMutableRole(role);
        repository.updateStatus(roleId, false);
        repository.appendRoleLog(roleId, operatorId, "DISABLE", "Disable role");
    }

    @Transactional
    public void deleteRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        assertMutableRole(role);
        repository.deleteRole(roleId);
        repository.appendRoleLog(roleId, operatorId, "DELETE", "Delete role");
    }

    @Transactional
    public void updateRolePermissions(Long roleId, AdminRolePermissionCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        assertMutableRole(role);
        List<Long> permissionIds = unique(command == null ? null : command.getPermissionIds());
        if (permissionIds.isEmpty()) {
            throw new BusinessException(400, "Role permissions are required");
        }
        repository.replacePermissions(roleId, permissionIds, role.getDataScope());
        repository.appendRoleLog(roleId, operatorId, "UPDATE_PERMISSIONS", "Update role permissions");
    }

    public List<AdminRoleLog> listRoleLogs(Long roleId) {
        getRole(roleId);
        return repository.findRoleLogs(roleId);
    }

    private AdminRoleCommand normalizedRole(AdminRoleCommand command) {
        if (command == null) {
            throw new BusinessException(400, "Role data is required");
        }
        String roleName = trimToNull(command.getRoleName());
        if (roleName == null) {
            throw new BusinessException(400, "Role name is required");
        }
        String roleCode = trimToNull(command.getRoleCode());
        if (roleCode == null) {
            throw new BusinessException(400, "Role code is required");
        }
        String dataScope = upper(trimToNull(command.getDataScope()));
        if (dataScope == null) {
            dataScope = "SELF";
        }
        if (!DATA_SCOPES.contains(dataScope)) {
            throw new BusinessException(400, "Role data scope is invalid");
        }
        List<Long> permissionIds = unique(command.getPermissionIds());
        if (permissionIds.isEmpty()) {
            throw new BusinessException(400, "Role permissions are required");
        }
        AdminRoleCommand normalized = new AdminRoleCommand();
        normalized.setRoleName(roleName);
        normalized.setRoleCode(roleCode);
        normalized.setDataScope(normalizedDataScope(dataScope));
        normalized.setRemark(trimToNull(command.getRemark()));
        normalized.setPermissionIds(permissionIds);
        return normalized;
    }

    private String normalizedDataScope(String dataScope) {
        if ("PERSONAL".equals(dataScope)) {
            return "SELF";
        }
        if ("MANAGED_ORG".equals(dataScope)) {
            return "ORG_ONLY";
        }
        return dataScope;
    }

    private void assertRoleNameAvailable(String roleName, Long currentRoleId) {
        Long existingRoleId = repository.findRoleIdByName(roleName);
        if (existingRoleId != null && !existingRoleId.equals(currentRoleId)) {
            throw new BusinessException(400, "Role name already exists");
        }
    }

    private void assertRoleCodeAvailable(String roleCode, Long currentRoleId) {
        Long existingRoleId = repository.findRoleIdByCode(roleCode);
        if (existingRoleId != null && !existingRoleId.equals(currentRoleId)) {
            throw new BusinessException(400, "Role code already exists");
        }
    }

    private void assertMutableRole(AdminRole role) {
        if ("super_admin".equals(role.getRoleCode()) || "超级管理员".equals(role.getRoleName())) {
            throw new BusinessException(400, "Built-in super admin role cannot be changed");
        }
    }

    private void assertNotReservedSuperAdmin(AdminRoleCommand command) {
        if ("super_admin".equals(command.getRoleCode()) || "超级管理员".equals(command.getRoleName())) {
            throw new BusinessException(400, "Built-in super admin role is reserved");
        }
    }

    private AdminPermissionCommand normalizedPermission(AdminPermissionCommand command, Long permissionId) {
        if (command == null) {
            throw new BusinessException(400, "Permission data is required");
        }
        Long parentId = command.getParentId();
        if (permissionId != null && permissionId.equals(parentId)) {
            throw new BusinessException(400, "Permission cannot use itself as parent");
        }
        String permissionName = trimToNull(command.getPermissionName());
        if (permissionName == null) {
            throw new BusinessException(400, "Permission name is required");
        }
        if (permissionName.length() > MAX_PERMISSION_NAME_LENGTH) {
            throw new BusinessException(400, "Permission name cannot exceed 8 characters");
        }
        String permissionCode = trimToNull(command.getPermissionCode());
        if (permissionCode == null) {
            throw new BusinessException(400, "Permission code is required");
        }
        String permissionType = upper(trimToNull(command.getPermissionType()));
        if (!PERMISSION_TYPES.contains(permissionType)) {
            throw new BusinessException(400, "Permission type is invalid");
        }
        String routePath = trimToNull(command.getRoutePath());
        if (routePath == null) {
            throw new BusinessException(400, "Permission route path is required");
        }
        if (routePath.length() > MAX_ROUTE_PATH_LENGTH) {
            throw new BusinessException(400, "Permission route path cannot exceed 100 characters");
        }
        AdminPermissionCommand normalized = new AdminPermissionCommand();
        normalized.setParentId(parentId);
        normalized.setPermissionName(permissionName);
        normalized.setPermissionCode(permissionCode);
        normalized.setPermissionType(permissionType);
        normalized.setRoutePath(routePath);
        normalized.setVisible(command.getVisible() == null ? Boolean.TRUE : command.getVisible());
        normalized.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        return normalized;
    }

    private void assertPermissionHierarchy(AdminPermissionCommand command, Long permissionId) {
        if ("MENU".equals(command.getPermissionType())) {
            if (command.getParentId() != null) {
                throw new BusinessException(400, "Top-level menu cannot have parent");
            }
            return;
        }
        if (command.getParentId() == null) {
            if ("PAGE".equals(command.getPermissionType())) {
                throw new BusinessException(400, "Second-level menu parent is required");
            }
            throw new BusinessException(400, "Button parent is required");
        }
        AdminPermission parent = repository.findPermission(command.getParentId());
        if (parent == null) {
            throw new BusinessException(400, "Parent permission not found");
        }
        if (permissionId != null && isDescendant(command.getParentId(), permissionId)) {
            throw new BusinessException(400, "Permission cannot use descendant as parent");
        }
        if ("PAGE".equals(command.getPermissionType()) && !"MENU".equals(parent.getPermissionType())) {
            throw new BusinessException(400, "Second-level menu parent must be a MENU permission");
        }
        if ("BUTTON".equals(command.getPermissionType()) && !"PAGE".equals(parent.getPermissionType())) {
            throw new BusinessException(400, "Button parent must be a PAGE permission");
        }
    }

    private void assertPermissionCodeAvailable(String permissionCode, Long currentPermissionId) {
        Long existingPermissionId = repository.findPermissionIdByCode(permissionCode);
        if (existingPermissionId != null && !existingPermissionId.equals(currentPermissionId)) {
            throw new BusinessException(400, "Permission code already exists");
        }
    }

    private void assertPermissionNameAvailable(String permissionName, Long parentId, Long currentPermissionId) {
        Long existingPermissionId = repository.findPermissionIdByNameAndParent(permissionName, parentId);
        if (existingPermissionId != null && !existingPermissionId.equals(currentPermissionId)) {
            throw new BusinessException(400, "Permission name already exists under parent");
        }
    }

    private void assertPermissionRoutePathAvailable(String routePath, Long currentPermissionId) {
        Long existingPermissionId = repository.findPermissionIdByRoutePath(routePath);
        if (existingPermissionId != null && !existingPermissionId.equals(currentPermissionId)) {
            throw new BusinessException(400, "Permission route path already exists");
        }
    }

    private boolean isDescendant(Long candidateParentId, Long permissionId) {
        for (AdminPermission permission : repository.findPermissions()) {
            if (candidateParentId.equals(permission.getPermissionId())) {
                Long parentId = permission.getParentId();
                while (parentId != null) {
                    if (permissionId.equals(parentId)) {
                        return true;
                    }
                    AdminPermission parent = findPermissionInList(parentId);
                    parentId = parent == null ? null : parent.getParentId();
                }
                return false;
            }
        }
        return false;
    }

    private AdminPermission findPermissionInList(Long permissionId) {
        for (AdminPermission permission : repository.findPermissions()) {
            if (permissionId.equals(permission.getPermissionId())) {
                return permission;
            }
        }
        return null;
    }

    private List<Long> collectPermissionSubtreeIds(Long permissionId) {
        List<AdminPermission> permissions = repository.findPermissions();
        List<Long> ids = new ArrayList<Long>();
        collectPermissionSubtreeIds(permissionId, permissions, ids);
        return ids;
    }

    private void collectPermissionSubtreeIds(Long permissionId, List<AdminPermission> permissions, List<Long> ids) {
        ids.add(permissionId);
        for (AdminPermission permission : permissions) {
            if (permissionId.equals(permission.getParentId())) {
                collectPermissionSubtreeIds(permission.getPermissionId(), permissions, ids);
            }
        }
    }

    private AdminRoleQuery normalizedQuery(AdminRoleQuery query) {
        AdminRoleQuery normalized = new AdminRoleQuery();
        if (query != null) {
            normalized.setKeyword(trimToNull(query.getKeyword()));
            normalized.setEnabled(query.getEnabled());
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private List<Long> unique(List<Long> ids) {
        List<Long> normalized = new ArrayList<Long>();
        if (ids == null) {
            return normalized;
        }
        for (Long id : ids) {
            if (id != null && id.longValue() > 0 && !normalized.contains(id)) {
                normalized.add(id);
            }
        }
        return normalized;
    }

    private void requireOperator(Long operatorId) {
        if (operatorId == null || operatorId.longValue() <= 0) {
            throw new BusinessException(401, "Missing admin identity");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase();
    }
}
