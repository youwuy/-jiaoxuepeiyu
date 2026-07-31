package com.qizhifu.jiaoxuepeiyu.admin.org;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.port.AdminOrgRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdminOrgServiceTests {

    @Test
    void buildsOrganizationTreeSortedBySortOrder() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        List<AdminOrg> tree = service.getTree();

        assertEquals(1, tree.size());
        assertEquals("School", tree.get(0).getOrgName());
        assertEquals("Department A", tree.get(0).getChildren().get(0).getOrgName());
        assertEquals("Department B", tree.get(0).getChildren().get(1).getOrgName());
    }

    @Test
    void filtersOrganizationTreeByKeywordAndKeepsParentPath() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        List<AdminOrg> tree = service.getTree("Child");

        assertEquals(1, tree.size());
        assertEquals("School", tree.get(0).getOrgName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("Department B", tree.get(0).getChildren().get(0).getOrgName());
        assertEquals(1, tree.get(0).getChildren().get(0).getChildren().size());
        assertEquals("Child B1", tree.get(0).getChildren().get(0).getChildren().get(0).getOrgName());
    }

    @Test
    void returnsEnabledOrganizationTreeForParentPicker() {
        FakeOrgs repository = new FakeOrgs();
        repository.orgs.get(3L).setEnabled(false);
        repository.orgs.get(4L).setEnabled(false);
        AdminOrgService service = new AdminOrgService(repository);

        List<AdminOrg> tree = service.getTree(null, true);

        assertEquals(1, tree.size());
        assertEquals("School", tree.get(0).getOrgName());
        assertEquals(1, tree.get(0).getChildren().size());
        assertEquals("Department A", tree.get(0).getChildren().get(0).getOrgName());
    }

    @Test
    void rejectsBlankOrganizationName() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.create(new AdminOrgCommand(null, " ", 0), 9L);
        });

        assertEquals("Organization name is required", exception.getMessage());
    }

    @Test
    void rejectsOrganizationNameLongerThanTwentyCharacters() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.create(new AdminOrgCommand(null, "123456789012345678901", 0), 9L);
        });

        assertEquals("Organization name cannot exceed 20 characters", exception.getMessage());
    }

    @Test
    void trimsOrganizationNameAndStoresCreatorId() {
        FakeOrgs repository = new FakeOrgs();
        AdminOrgService service = new AdminOrgService(repository);

        Long orgId = service.create(new AdminOrgCommand(null, "  New School  ", 2), 9L);

        assertEquals(10L, orgId.longValue());
        assertEquals(9L, repository.createdBy.longValue());
        assertEquals("New School", repository.createdCommand.getOrgName());
    }

    @Test
    void rejectsDuplicateOrganizationNameUnderSameParent() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.create(new AdminOrgCommand(1L, "Department A", 2), 9L);
        });

        assertEquals("Organization name already exists under the same parent", exception.getMessage());
    }

    @Test
    void rejectsCreateUnderDisabledParent() {
        FakeOrgs repository = new FakeOrgs();
        repository.orgs.get(3L).setEnabled(false);
        AdminOrgService service = new AdminOrgService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.create(new AdminOrgCommand(3L, "New Child", 2), 9L);
        });

        assertEquals("Parent organization must be enabled", exception.getMessage());
    }

    @Test
    void rejectsOrganizationUsingItselfAsParent() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.update(2L, new AdminOrgCommand(2L, "Department A", 1), 9L);
        });

        assertEquals("Organization cannot use itself as parent", exception.getMessage());
    }

    @Test
    void rejectsChangingOrganizationParentOnUpdate() {
        AdminOrgService service = new AdminOrgService(new FakeOrgs());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.update(2L, new AdminOrgCommand(3L, "Department A", 1), 9L);
        });

        assertEquals("Organization parent cannot be changed", exception.getMessage());
    }

    @Test
    void disablesOrganizationCascadeToDescendantsAndEnablesOnlyTheSelectedNode() {
        FakeOrgs repository = new FakeOrgs();
        AdminOrgService service = new AdminOrgService(repository);

        service.disable(3L, 9L);
        assertEquals(false, repository.enabledById.get(3L).booleanValue());
        assertEquals(false, repository.enabledById.get(4L).booleanValue());
        service.enable(3L, 9L);

        assertEquals(true, repository.enabledById.get(3L).booleanValue());
        assertEquals(false, repository.enabledById.get(4L).booleanValue());
    }

    @Test
    void savesOrganizationSortWeights() {
        FakeOrgs repository = new FakeOrgs();
        AdminOrgService service = new AdminOrgService(repository);
        AdminOrgSortCommand first = new AdminOrgSortCommand();
        first.setOrgId(2L);
        first.setSortOrder(Integer.valueOf(3));
        AdminOrgSortCommand second = new AdminOrgSortCommand();
        second.setOrgId(3L);
        second.setSortOrder(Integer.valueOf(1));

        service.updateSort(Arrays.asList(first, second), 9L);

        assertEquals(2, repository.sortUpdates);
        assertEquals(9L, repository.updatedBy.longValue());
        assertEquals(1, repository.orgs.get(3L).getSortOrder());
    }

    private static class FakeOrgs implements AdminOrgRepository {
        private final Map<Long, AdminOrg> orgs = new LinkedHashMap<Long, AdminOrg>();
        private final Map<Long, Boolean> enabledById = new LinkedHashMap<Long, Boolean>();
        private Long createdBy;
        private AdminOrgCommand createdCommand;
        private Long updatedBy;
        private Long updatedOrgId;
        private AdminOrgCommand updatedCommand;
        private int sortUpdates;

        private FakeOrgs() {
            put(org(1L, null, "School", 1, true));
            put(org(2L, 1L, "Department A", 1, true));
            put(org(3L, 1L, "Department B", 2, true));
            put(org(4L, 3L, "Child B1", 1, true));
        }

        @Override
        public List<AdminOrg> findAll() {
            List<AdminOrg> list = new ArrayList<AdminOrg>();
            for (AdminOrg org : orgs.values()) {
                list.add(copy(org));
            }
            return list;
        }

        @Override
        public Long create(AdminOrgCommand command, Long creatorId) {
            this.createdBy = creatorId;
            this.createdCommand = command;
            AdminOrg org = org(10L, command.getParentId(), command.getOrgName(), command.getSortOrder(), true);
            put(org);
            return 10L;
        }

        @Override
        public void update(Long orgId, AdminOrgCommand command, Long operatorId) {
            this.updatedOrgId = orgId;
            this.updatedBy = operatorId;
            this.updatedCommand = command;
            AdminOrg org = orgs.get(orgId);
            org.setOrgName(command.getOrgName());
            org.setSortOrder(command.getSortOrder());
        }

        @Override
        public void updateStatus(Long orgId, boolean enabled, Long operatorId) {
            this.updatedBy = operatorId;
            enabledById.put(orgId, Boolean.valueOf(enabled));
            AdminOrg org = orgs.get(orgId);
            if (org != null) {
                org.setEnabled(enabled);
            }
        }

        @Override
        public void updateSort(AdminOrgSortCommand command, Long operatorId) {
            this.updatedBy = operatorId;
            this.sortUpdates++;
            AdminOrg org = orgs.get(command.getOrgId());
            if (org != null) {
                org.setSortOrder(command.getSortOrder().intValue());
            }
        }

        private void put(AdminOrg org) {
            orgs.put(org.getOrgId(), org);
            enabledById.put(org.getOrgId(), Boolean.valueOf(org.isEnabled()));
        }

        private AdminOrg copy(AdminOrg source) {
            AdminOrg org = new AdminOrg();
            org.setOrgId(source.getOrgId());
            org.setParentId(source.getParentId());
            org.setOrgName(source.getOrgName());
            org.setSortOrder(source.getSortOrder());
            org.setEnabled(source.isEnabled());
            return org;
        }

        private AdminOrg org(Long id, Long parentId, String name, int sortOrder, boolean enabled) {
            AdminOrg org = new AdminOrg();
            org.setOrgId(id);
            org.setParentId(parentId);
            org.setOrgName(name);
            org.setSortOrder(sortOrder);
            org.setEnabled(enabled);
            return org;
        }
    }
}
