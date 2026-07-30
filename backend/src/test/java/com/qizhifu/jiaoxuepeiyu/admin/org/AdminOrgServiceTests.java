package com.qizhifu.jiaoxuepeiyu.admin.org;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.port.AdminOrgRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminOrgServiceTests {

    @Test
    void buildsOrganizationTreeSortedBySortOrder() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        List<AdminOrg> tree = service.getTree();

        assertEquals(1, tree.size());
        assertEquals("School", tree.get(0).getOrgName());
        assertEquals("Department A", tree.get(0).getChildren().get(0).getOrgName());
    }

    @Test
    void rejectsBlankOrganizationName() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.create(new AdminOrgCommand(null, " ", 0));
        });

        assertEquals("Organization name is required", exception.getMessage());
    }

    @Test
    void rejectsOrganizationUsingItselfAsParent() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.update(2L, new AdminOrgCommand(2L, "Department A", 1));
        });

        assertEquals("Organization cannot use itself as parent", exception.getMessage());
    }

    @Test
    void updatesOrganizationStatus() {
        FakeOrgs repository = new FakeOrgs();
        AdminOrgService service = new AdminOrgService(repository);

        service.disable(2L);
        service.enable(2L);

        assertEquals(2L, repository.statusOrgId.longValue());
        assertEquals(true, repository.enabled);
    }

    private static class FakeOrgs implements AdminOrgRepository {
        private Long statusOrgId;
        private boolean enabled;

        @Override
        public List<AdminOrg> findAll() {
            return new ArrayList<AdminOrg>(Arrays.asList(
                    org(2L, 1L, "Department A", 1),
                    org(1L, null, "School", 1)));
        }

        @Override
        public Long create(AdminOrgCommand command) {
            return 10L;
        }

        @Override
        public void update(Long orgId, AdminOrgCommand command) {
        }

        @Override
        public void updateStatus(Long orgId, boolean enabled) {
            this.statusOrgId = orgId;
            this.enabled = enabled;
        }

        private AdminOrg org(Long id, Long parentId, String name, int sortOrder) {
            AdminOrg org = new AdminOrg();
            org.setOrgId(id);
            org.setParentId(parentId);
            org.setOrgName(name);
            org.setSortOrder(sortOrder);
            org.setEnabled(true);
            return org;
        }
    }
}
