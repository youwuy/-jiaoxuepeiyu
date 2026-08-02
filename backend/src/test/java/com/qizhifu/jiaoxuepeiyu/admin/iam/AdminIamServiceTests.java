package com.qizhifu.jiaoxuepeiyu.admin.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminIamServiceTests {

    @Test
    void buildsPermissionTreeFromFlatPermissions() {
        FakeIam repository = new FakeIam();
        repository.permissions = Arrays.asList(permission(1L, null), permission(2L, 1L));
        AdminIamService service = new AdminIamService(repository);

        List<AdminPermission> tree = service.listPermissionTree();

        assertEquals(1, tree.size());
        assertEquals(1, tree.get(0).getChildren().size());
    }

    @Test
    void createsRoleWithNormalizedDataScope() {
        FakeIam repository = new FakeIam();
        AdminIamService service = new AdminIamService(repository);

        Long roleId = service.createRole(roleCommand(), 9L);

        assertEquals(31L, roleId.longValue());
        assertEquals("Teacher", repository.savedCommand.getRoleName());
        assertEquals("ORG_ONLY", repository.savedCommand.getDataScope());
        assertEquals("CREATE", repository.lastLogAction);
    }

    @Test
    void rejectsInvalidDataScope() {
        AdminIamService service = new AdminIamService(new FakeIam());
        AdminRoleCommand command = roleCommand();
        command.setDataScope("EVERYTHING");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createRole(command, 9L));

        assertEquals("Role data scope is invalid", exception.getMessage());
    }

    @Test
    void rejectsRoleWithoutPermissions() {
        AdminIamService service = new AdminIamService(new FakeIam());
        AdminRoleCommand command = roleCommand();
        command.setPermissionIds(new ArrayList<Long>());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createRole(command, 9L));

        assertEquals("Role permissions are required", exception.getMessage());
    }

    @Test
    void rejectsDuplicateRoleNameOrCode() {
        FakeIam repository = new FakeIam();
        repository.existingRoleIdByName = 32L;
        AdminIamService service = new AdminIamService(repository);

        BusinessException nameException = assertThrows(BusinessException.class,
                () -> service.createRole(roleCommand(), 9L));
        assertEquals("Role name already exists", nameException.getMessage());

        repository.existingRoleIdByName = null;
        repository.existingRoleIdByCode = 33L;
        BusinessException codeException = assertThrows(BusinessException.class,
                () -> service.createRole(roleCommand(), 9L));
        assertEquals("Role code already exists", codeException.getMessage());
    }

    @Test
    void protectsSuperAdminRoleFromMutation() {
        FakeIam repository = new FakeIam();
        repository.role = new AdminRole();
        repository.role.setRoleId(1L);
        repository.role.setRoleName("超级管理员");
        repository.role.setRoleCode("super_admin");
        repository.role.setDataScope("ALL");
        AdminIamService service = new AdminIamService(repository);

        BusinessException updateException = assertThrows(BusinessException.class,
                () -> service.updateRole(1L, roleCommand(), 9L));
        assertEquals("Built-in super admin role cannot be changed", updateException.getMessage());

        BusinessException disableException = assertThrows(BusinessException.class,
                () -> service.disableRole(1L, 9L));
        assertEquals("Built-in super admin role cannot be changed", disableException.getMessage());

        BusinessException deleteException = assertThrows(BusinessException.class,
                () -> service.deleteRole(1L, 9L));
        assertEquals("Built-in super admin role cannot be changed", deleteException.getMessage());
    }

    @Test
    void rejectsCreatingReservedSuperAdminRole() {
        AdminIamService service = new AdminIamService(new FakeIam());
        AdminRoleCommand command = roleCommand();
        command.setRoleCode("super_admin");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createRole(command, 9L));

        assertEquals("Built-in super admin role is reserved", exception.getMessage());
    }

    @Test
    void replacesPermissionsWithUniqueIds() {
        FakeIam repository = new FakeIam();
        repository.role = new AdminRole();
        repository.role.setRoleId(31L);
        repository.role.setRoleName("Teacher");
        repository.role.setDataScope("ALL");
        AdminIamService service = new AdminIamService(repository);
        AdminRolePermissionCommand command = new AdminRolePermissionCommand();
        command.setPermissionIds(Arrays.asList(1L, 1L, null, 2L));

        service.updateRolePermissions(31L, command, 9L);

        assertEquals(Arrays.asList(1L, 2L), repository.replacedPermissionIds);
        assertEquals("ALL", repository.replacedDataScope);
        assertEquals("UPDATE_PERMISSIONS", repository.lastLogAction);
    }

    @Test
    void createsPermissionWithNormalizedFields() {
        FakeIam repository = new FakeIam();
        AdminIamService service = new AdminIamService(repository);

        Long permissionId = service.createPermission(rootPermissionCommand(), 9L);

        assertEquals(41L, permissionId.longValue());
        assertEquals("Courses", repository.savedPermissionCommand.getPermissionName());
        assertEquals("course:center", repository.savedPermissionCommand.getPermissionCode());
        assertEquals("MENU", repository.savedPermissionCommand.getPermissionType());
        assertEquals("/courses", repository.savedPermissionCommand.getRoutePath());
        assertEquals(Boolean.TRUE, repository.savedPermissionCommand.getVisible());
    }

    @Test
    void rejectsInvalidPermissionType() {
        AdminIamService service = new AdminIamService(new FakeIam());
        AdminPermissionCommand command = permissionCommand();
        command.setPermissionType("LINK");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createPermission(command, 9L));

        assertEquals("Permission type is invalid", exception.getMessage());
    }

    @Test
    void rejectsDuplicatePermissionCode() {
        FakeIam repository = new FakeIam();
        repository.parentPermission = permission(1L, null);
        repository.existingPermissionIdByCode = 41L;
        AdminIamService service = new AdminIamService(repository);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createPermission(rootPermissionCommand(), 9L));

        assertEquals("Permission code already exists", exception.getMessage());
    }

    @Test
    void rejectsMissingParentPermission() {
        AdminIamService service = new AdminIamService(new FakeIam());
        AdminPermissionCommand command = permissionCommand();
        command.setPermissionType("PAGE");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createPermission(command, 9L));

        assertEquals("Parent permission not found", exception.getMessage());
    }

    @Test
    void rejectsSelfParentOnPermissionUpdate() {
        FakeIam repository = new FakeIam();
        repository.permission = permission(41L, null);
        AdminIamService service = new AdminIamService(repository);
        AdminPermissionCommand command = permissionCommand();
        command.setParentId(41L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updatePermission(41L, command, 9L));

        assertEquals("Permission cannot use itself as parent", exception.getMessage());
    }

    @Test
    void rejectsDeletingPermissionWithChildrenOrRoleBindings() {
        FakeIam repository = new FakeIam();
        repository.permission = permission(41L, null);
        repository.childCount = 1;
        AdminIamService service = new AdminIamService(repository);

        BusinessException childrenException = assertThrows(BusinessException.class,
                () -> service.deletePermission(41L, 9L));
        assertEquals("Permission has child nodes", childrenException.getMessage());

        repository.childCount = 0;
        repository.roleBindingCount = 2;
        BusinessException bindingException = assertThrows(BusinessException.class,
                () -> service.deletePermission(41L, 9L));
        assertEquals("Permission is bound to roles", bindingException.getMessage());
    }

    @Test
    void rejectsPermissionHierarchyViolations() {
        FakeIam repository = new FakeIam();
        repository.parentPermission = permission(1L, null, "MENU");
        AdminIamService service = new AdminIamService(repository);

        AdminPermissionCommand topMenu = permissionCommand();
        topMenu.setParentId(1L);
        BusinessException topMenuException = assertThrows(BusinessException.class,
                () -> service.createPermission(topMenu, 9L));
        assertEquals("Top-level menu cannot have parent", topMenuException.getMessage());

        AdminPermissionCommand page = permissionCommand();
        page.setPermissionType("PAGE");
        page.setParentId(null);
        BusinessException pageException = assertThrows(BusinessException.class,
                () -> service.createPermission(page, 9L));
        assertEquals("Second-level menu parent is required", pageException.getMessage());

        AdminPermissionCommand button = permissionCommand();
        button.setPermissionType("BUTTON");
        button.setParentId(1L);
        BusinessException buttonException = assertThrows(BusinessException.class,
                () -> service.createPermission(button, 9L));
        assertEquals("Button parent must be a PAGE permission", buttonException.getMessage());
    }

    @Test
    void rejectsDuplicateSiblingPermissionNameAndRoutePath() {
        FakeIam repository = new FakeIam();
        repository.existingPermissionIdByName = 42L;
        AdminIamService service = new AdminIamService(repository);

        BusinessException nameException = assertThrows(BusinessException.class,
                () -> service.createPermission(rootPermissionCommand(), 9L));
        assertEquals("Permission name already exists under parent", nameException.getMessage());

        repository.existingPermissionIdByName = null;
        repository.existingPermissionIdByRoutePath = 43L;
        BusinessException routeException = assertThrows(BusinessException.class,
                () -> service.createPermission(rootPermissionCommand(), 9L));
        assertEquals("Permission route path already exists", routeException.getMessage());
    }

    @Test
    void disablesPermissionWithDescendants() {
        FakeIam repository = new FakeIam();
        repository.permission = permission(1L, null, "MENU");
        repository.permissions = Arrays.asList(
                permission(1L, null, "MENU"),
                permission(2L, 1L, "PAGE"),
                permission(3L, 2L, "BUTTON"),
                permission(4L, null, "MENU"));
        AdminIamService service = new AdminIamService(repository);

        service.disablePermission(1L, 9L);

        assertEquals(Arrays.asList(1L, 2L, 3L), repository.statusPermissionIds);
        assertEquals(Arrays.asList(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE), repository.statusValues);
    }

    @Test
    void enablesPermissionWithDescendants() {
        FakeIam repository = new FakeIam();
        repository.permission = permission(1L, null, "MENU");
        repository.permissions = Arrays.asList(
                permission(1L, null, "MENU"),
                permission(2L, 1L, "PAGE"),
                permission(3L, 2L, "BUTTON"),
                permission(4L, null, "MENU"));
        AdminIamService service = new AdminIamService(repository);

        service.enablePermission(1L, 9L);

        assertEquals(Arrays.asList(1L, 2L, 3L), repository.statusPermissionIds);
        assertEquals(Arrays.asList(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE), repository.statusValues);
    }

    @Test
    void updatesPermissionSortOrdersWithoutChangingParents() {
        FakeIam repository = new FakeIam();
        repository.permission = permission(2L, 1L, "PAGE");
        AdminIamService service = new AdminIamService(repository);
        AdminPermissionSortCommand command = new AdminPermissionSortCommand();
        command.setItems(Arrays.asList(sortItem(2L, 1L, 8), sortItem(2L, 1L, 9)));

        service.updatePermissionSorts(command, 9L);

        assertEquals(Arrays.asList(2L), repository.sortedPermissionIds);
        assertEquals(Arrays.asList(8), repository.sortedOrders);
    }

    private AdminRoleCommand roleCommand() {
        AdminRoleCommand command = new AdminRoleCommand();
        command.setRoleName(" Teacher ");
        command.setRoleCode("teacher");
        command.setDataScope("ORG_ONLY");
        command.setPermissionIds(Arrays.asList(1L, 2L));
        return command;
    }

    private AdminPermission permission(Long id, Long parentId) {
        return permission(id, parentId, "MENU");
    }

    private AdminPermission permission(Long id, Long parentId, String type) {
        AdminPermission permission = new AdminPermission();
        permission.setPermissionId(id);
        permission.setParentId(parentId);
        permission.setPermissionName("P" + id);
        permission.setPermissionCode("p:" + id);
        permission.setPermissionType(type);
        permission.setSortOrder(id.intValue());
        return permission;
    }

    private AdminPermissionCommand permissionCommand() {
        AdminPermissionCommand command = new AdminPermissionCommand();
        command.setParentId(1L);
        command.setPermissionName(" Courses ");
        command.setPermissionCode(" course:center ");
        command.setPermissionType("menu");
        command.setRoutePath(" /courses ");
        command.setVisible(null);
        command.setSortOrder(10);
        return command;
    }

    private AdminPermissionCommand rootPermissionCommand() {
        AdminPermissionCommand command = permissionCommand();
        command.setParentId(null);
        command.setPermissionType("MENU");
        command.setRoutePath("/courses");
        return command;
    }

    private AdminPermissionSortItem sortItem(Long permissionId, Long parentId, Integer sortOrder) {
        AdminPermissionSortItem item = new AdminPermissionSortItem();
        item.setPermissionId(permissionId);
        item.setParentId(parentId);
        item.setSortOrder(sortOrder);
        return item;
    }

    private static class FakeIam implements AdminIamRepository {
        private List<AdminPermission> permissions = new ArrayList<AdminPermission>();
        private AdminPermission permission;
        private AdminPermission parentPermission;
        private AdminRole role;
        private AdminRoleCommand savedCommand;
        private AdminPermissionCommand savedPermissionCommand;
        private List<Long> replacedPermissionIds;
        private String replacedDataScope;
        private String lastLogAction;
        private Long existingPermissionIdByCode;
        private Long existingPermissionIdByName;
        private Long existingPermissionIdByRoutePath;
        private Long existingRoleIdByName;
        private Long existingRoleIdByCode;
        private List<Long> statusPermissionIds = new ArrayList<Long>();
        private List<Boolean> statusValues = new ArrayList<Boolean>();
        private List<Long> sortedPermissionIds = new ArrayList<Long>();
        private List<Integer> sortedOrders = new ArrayList<Integer>();
        private int childCount;
        private int roleBindingCount;

        @Override
        public List<AdminPermission> findPermissions() {
            return permissions;
        }

        @Override
        public AdminPermission findPermission(Long permissionId) {
            if (permission != null && permission.getPermissionId().equals(permissionId)) {
                return permission;
            }
            if (parentPermission != null && parentPermission.getPermissionId().equals(permissionId)) {
                return parentPermission;
            }
            return null;
        }

        @Override
        public Long findPermissionIdByCode(String permissionCode) {
            return existingPermissionIdByCode;
        }

        @Override
        public Long findPermissionIdByNameAndParent(String permissionName, Long parentId) {
            return existingPermissionIdByName;
        }

        @Override
        public Long findPermissionIdByRoutePath(String routePath) {
            return existingPermissionIdByRoutePath;
        }

        @Override
        public Long createPermission(AdminPermissionCommand command) {
            this.savedPermissionCommand = command;
            return 41L;
        }

        @Override
        public void updatePermission(Long permissionId, AdminPermissionCommand command) {
            this.savedPermissionCommand = command;
        }

        @Override
        public void updatePermissionStatus(Long permissionId, boolean visible) {
            statusPermissionIds.add(permissionId);
            statusValues.add(visible);
        }

        @Override
        public void updatePermissionSort(Long permissionId, Integer sortOrder) {
            sortedPermissionIds.add(permissionId);
            sortedOrders.add(sortOrder);
        }

        @Override
        public void deletePermission(Long permissionId) {
        }

        @Override
        public int countPermissionChildren(Long permissionId) {
            return childCount;
        }

        @Override
        public int countPermissionRoleBindings(Long permissionId) {
            return roleBindingCount;
        }

        @Override
        public List<AdminRole> findRoles(AdminRoleQuery query) {
            return new ArrayList<AdminRole>();
        }

        @Override
        public long countRoles(AdminRoleQuery query) {
            return 0;
        }

        @Override
        public Long findRoleIdByName(String roleName) {
            return existingRoleIdByName;
        }

        @Override
        public Long findRoleIdByCode(String roleCode) {
            return existingRoleIdByCode;
        }

        @Override
        public AdminRole findRole(Long roleId) {
            return role;
        }

        @Override
        public Long createRole(AdminRoleCommand command) {
            this.savedCommand = command;
            return 31L;
        }

        @Override
        public void updateRole(Long roleId, AdminRoleCommand command) {
            this.savedCommand = command;
        }

        @Override
        public void updateStatus(Long roleId, boolean enabled) {
        }

        @Override
        public void deleteRole(Long roleId) {
        }

        @Override
        public void replacePermissions(Long roleId, List<Long> permissionIds, String dataScope) {
            this.replacedPermissionIds = permissionIds;
            this.replacedDataScope = dataScope;
        }

        @Override
        public void appendRoleLog(Long roleId, Long operatorId, String action, String content) {
            this.lastLogAction = action;
        }

        @Override
        public List<AdminRoleLog> findRoleLogs(Long roleId) {
            return new ArrayList<AdminRoleLog>();
        }
    }
}
