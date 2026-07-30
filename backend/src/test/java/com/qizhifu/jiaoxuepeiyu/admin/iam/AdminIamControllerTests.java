package com.qizhifu.jiaoxuepeiyu.admin.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminPermission;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRole;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleLog;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminRoleQuery;
import com.qizhifu.jiaoxuepeiyu.admin.iam.port.AdminIamRepository;
import com.qizhifu.jiaoxuepeiyu.admin.iam.controller.AdminIamController;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
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

    private static class FakeIam implements AdminIamRepository {
        private Long operatorId;

        @Override
        public List<AdminPermission> findPermissions() {
            return new ArrayList<AdminPermission>();
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
        public void replacePermissions(Long roleId, List<Long> permissionIds, String dataScope) {
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
