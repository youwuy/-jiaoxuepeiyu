package com.qizhifu.jiaoxuepeiyu.admin.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
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
        assertEquals("MANAGED_ORG", repository.savedCommand.getDataScope());
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

    private AdminRoleCommand roleCommand() {
        AdminRoleCommand command = new AdminRoleCommand();
        command.setRoleName(" Teacher ");
        command.setRoleCode("teacher");
        command.setDataScope("managed_org");
        command.setPermissionIds(Arrays.asList(1L, 2L));
        return command;
    }

    private AdminPermission permission(Long id, Long parentId) {
        AdminPermission permission = new AdminPermission();
        permission.setPermissionId(id);
        permission.setParentId(parentId);
        permission.setPermissionName("P" + id);
        permission.setPermissionCode("p:" + id);
        permission.setPermissionType("MENU");
        permission.setSortOrder(id.intValue());
        return permission;
    }

    private static class FakeIam implements AdminIamRepository {
        private List<AdminPermission> permissions = new ArrayList<AdminPermission>();
        private AdminRole role;
        private AdminRoleCommand savedCommand;
        private List<Long> replacedPermissionIds;
        private String replacedDataScope;
        private String lastLogAction;

        @Override
        public List<AdminPermission> findPermissions() {
            return permissions;
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
