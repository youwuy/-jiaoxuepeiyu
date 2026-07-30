package com.qizhifu.jiaoxuepeiyu.admin.iam;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
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
    private static final Set<String> DATA_SCOPES = new HashSet<String>(
            Arrays.asList("PERSONAL", "MANAGED_ORG", "ALL"));
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
        assertParentExists(normalized.getParentId());
        assertPermissionCodeAvailable(normalized.getPermissionCode(), null);
        return repository.createPermission(normalized);
    }

    @Transactional
    public void updatePermission(Long permissionId, AdminPermissionCommand command, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        AdminPermissionCommand normalized = normalizedPermission(command, permissionId);
        assertParentExists(normalized.getParentId());
        assertPermissionCodeAvailable(normalized.getPermissionCode(), permissionId);
        repository.updatePermission(permissionId, normalized);
    }

    @Transactional
    public void enablePermission(Long permissionId, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        repository.updatePermissionStatus(permissionId, true);
    }

    @Transactional
    public void disablePermission(Long permissionId, Long operatorId) {
        requireOperator(operatorId);
        getPermission(permissionId);
        repository.updatePermissionStatus(permissionId, false);
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
        Long roleId = repository.createRole(normalized);
        repository.replacePermissions(roleId, normalized.getPermissionIds(), normalized.getDataScope());
        repository.appendRoleLog(roleId, operatorId, "CREATE", "Create role");
        return roleId;
    }

    @Transactional
    public void updateRole(Long roleId, AdminRoleCommand command, Long operatorId) {
        requireOperator(operatorId);
        getRole(roleId);
        AdminRoleCommand normalized = normalizedRole(command);
        repository.updateRole(roleId, normalized);
        repository.replacePermissions(roleId, normalized.getPermissionIds(), normalized.getDataScope());
        repository.appendRoleLog(roleId, operatorId, "UPDATE", "Update role");
    }

    @Transactional
    public void enableRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        getRole(roleId);
        repository.updateStatus(roleId, true);
        repository.appendRoleLog(roleId, operatorId, "ENABLE", "Enable role");
    }

    @Transactional
    public void disableRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        getRole(roleId);
        repository.updateStatus(roleId, false);
        repository.appendRoleLog(roleId, operatorId, "DISABLE", "Disable role");
    }

    @Transactional
    public void deleteRole(Long roleId, Long operatorId) {
        requireOperator(operatorId);
        getRole(roleId);
        repository.deleteRole(roleId);
        repository.appendRoleLog(roleId, operatorId, "DELETE", "Delete role");
    }

    @Transactional
    public void updateRolePermissions(Long roleId, AdminRolePermissionCommand command, Long operatorId) {
        requireOperator(operatorId);
        AdminRole role = getRole(roleId);
        repository.replacePermissions(roleId, unique(command == null ? null : command.getPermissionIds()), role.getDataScope());
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
            dataScope = "PERSONAL";
        }
        if (!DATA_SCOPES.contains(dataScope)) {
            throw new BusinessException(400, "Role data scope is invalid");
        }
        AdminRoleCommand normalized = new AdminRoleCommand();
        normalized.setRoleName(roleName);
        normalized.setRoleCode(roleCode);
        normalized.setDataScope(dataScope);
        normalized.setRemark(trimToNull(command.getRemark()));
        normalized.setPermissionIds(unique(command.getPermissionIds()));
        return normalized;
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
        String permissionCode = trimToNull(command.getPermissionCode());
        if (permissionCode == null) {
            throw new BusinessException(400, "Permission code is required");
        }
        String permissionType = upper(trimToNull(command.getPermissionType()));
        if (!PERMISSION_TYPES.contains(permissionType)) {
            throw new BusinessException(400, "Permission type is invalid");
        }
        AdminPermissionCommand normalized = new AdminPermissionCommand();
        normalized.setParentId(parentId);
        normalized.setPermissionName(permissionName);
        normalized.setPermissionCode(permissionCode);
        normalized.setPermissionType(permissionType);
        normalized.setRoutePath(trimToNull(command.getRoutePath()));
        normalized.setVisible(command.getVisible() == null ? Boolean.TRUE : command.getVisible());
        normalized.setSortOrder(command.getSortOrder() == null ? 0 : command.getSortOrder());
        return normalized;
    }

    private void assertPermissionCodeAvailable(String permissionCode, Long currentPermissionId) {
        Long existingPermissionId = repository.findPermissionIdByCode(permissionCode);
        if (existingPermissionId != null && !existingPermissionId.equals(currentPermissionId)) {
            throw new BusinessException(400, "Permission code already exists");
        }
    }

    private void assertParentExists(Long parentId) {
        if (parentId != null && repository.findPermission(parentId) == null) {
            throw new BusinessException(400, "Parent permission not found");
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
