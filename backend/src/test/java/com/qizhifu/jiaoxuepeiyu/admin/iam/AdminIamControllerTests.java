package com.qizhifu.jiaoxuepeiyu.admin.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.iam.controller.AdminIamController;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermissionSortItem;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRolePermissionBinding;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import com.qizhifu.jiaoxuepeiyu.admin.iam.port.AdminIamRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AdminIamControllerTests {

    @Test
    void createRoleReadsOperatorFromHeader() {
        FakeIam repository = new FakeIam();
        AdminIamController controller = new AdminIamController(new AdminIamService(repository));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "9");
        AdminRoleCommand command = new AdminRoleCommand();
        command.setRoleName("Teacher");
        command.setRoleCode("teacher");
        command.setPermissionIds(Arrays.asList(1L));

        Long roleId = controller.createRole(command, request).getData();

        assertEquals(31L, roleId.longValue());
        assertEquals(9L, repository.operatorId.longValue());
    }

    @Test
    void createRoleRejectsMissingOperatorHeader() {
        AdminIamController controller = new AdminIamController(new AdminIamService(new FakeIam()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.createRole(new AdminRoleCommand(), new MockHttpServletRequest()));

        assertEquals("Missing admin identity", exception.getMessage());
    }

    @Test
    void createPermissionReadsOperatorFromHeader() {
        FakeIam repository = new FakeIam();
        AdminIamController controller = new AdminIamController(new AdminIamService(repository));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "9");
        AdminPermissionCommand command = new AdminPermissionCommand();
        command.setPermissionName("Courses");
        command.setPermissionCode("course:center");
        command.setPermissionType("MENU");
        command.setRoutePath("/courses");

        Long permissionId = controller.createPermission(command, request).getData();

        assertEquals(41L, permissionId.longValue());
        assertEquals("course:center", repository.savedPermissionCommand.getPermissionCode());
    }

    @Test
    void updatePermissionSortsPersistsSubmittedOrder() {
        FakeIam repository = new FakeIam();
        AdminIamController controller = new AdminIamController(new AdminIamService(repository));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "9");
        AdminPermissionSortCommand command = new AdminPermissionSortCommand();
        AdminPermissionSortItem item = new AdminPermissionSortItem();
        item.setPermissionId(41L);
        item.setParentId(null);
        item.setSortOrder(2);
        command.setItems(Arrays.asList(item));

        controller.updatePermissionSorts(command, request);

        assertEquals(41L, repository.sortedPermissionId.longValue());
        assertEquals(2, repository.sortedOrder.intValue());
    }

    private static class FakeIam implements AdminIamRepository {
        private Long operatorId;
        private AdminPermissionCommand savedPermissionCommand;
        private Long sortedPermissionId;
        private Integer sortedOrder;

        @Override
        public List<AdminPermission> findPermissions() {
            return new ArrayList<AdminPermission>();
        }

        @Override
        public boolean isUnrestrictedAdmin(Long userId) {
            return false;
        }

        @Override
        public List<String> findUserPermissionCodes(Long userId) {
            return new ArrayList<String>();
        }

        @Override
        public String findUserDataScope(Long userId, String permissionCode) {
            return "SELF";
        }

        @Override
        public List<Long> findManagedOrgIds(Long userId) {
            return new ArrayList<Long>();
        }

        @Override
        public AdminPermission findPermission(Long permissionId) {
            AdminPermission permission = new AdminPermission();
            permission.setPermissionId(permissionId);
            return permission;
        }

        @Override
        public Long findPermissionIdByCode(String permissionCode) {
            return null;
        }

        @Override
        public Long findPermissionIdByNameAndParent(String permissionName, Long parentId) {
            return null;
        }

        @Override
        public Long findPermissionIdByRoutePath(String routePath) {
            return null;
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
        }

        @Override
        public void updatePermissionSort(Long permissionId, Integer sortOrder) {
            this.sortedPermissionId = permissionId;
            this.sortedOrder = sortOrder;
        }

        @Override
        public void deletePermission(Long permissionId) {
        }

        @Override
        public int countPermissionChildren(Long permissionId) {
            return 0;
        }

        @Override
        public int countPermissionRoleBindings(Long permissionId) {
            return 0;
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
            return null;
        }

        @Override
        public Long findRoleIdByCode(String roleCode) {
            return null;
        }

        @Override
        public AdminRole findRole(Long roleId) {
            AdminRole role = new AdminRole();
            role.setRoleId(roleId);
            role.setDataScope("PERSONAL");
            return role;
        }

        @Override
        public Long createRole(AdminRoleCommand command) {
            return 31L;
        }

        @Override
        public void updateRole(Long roleId, AdminRoleCommand command) {
        }

        @Override
        public void updateStatus(Long roleId, boolean enabled) {
        }

        @Override
        public void deleteRole(Long roleId) {
        }

        @Override
        public void replacePermissions(Long roleId, List<AdminRolePermissionBinding> bindings) {
        }

        @Override
        public void appendRoleLog(Long roleId, Long operatorId, String action, String content) {
            this.operatorId = operatorId;
        }

        @Override
        public List<AdminRoleLog> findRoleLogs(Long roleId) {
            return new ArrayList<AdminRoleLog>();
        }
    }
}
