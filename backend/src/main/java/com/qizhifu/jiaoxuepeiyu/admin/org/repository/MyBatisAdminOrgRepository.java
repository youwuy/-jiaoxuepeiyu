package com.qizhifu.jiaoxuepeiyu.admin.org.repository;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgSortCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.port.AdminOrgRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminOrgRepository implements AdminOrgRepository {

    private final AdminOrgMapper mapper;

    public MyBatisAdminOrgRepository(AdminOrgMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminOrg> findAll() {
        return mapper.findAll();
    }

    @Override
    public Long create(AdminOrgCommand command, Long creatorId) {
        AdminOrg org = new AdminOrg();
        org.setParentId(command.getParentId());
        org.setOrgName(command.getOrgName());
        org.setSortOrder(command.getSortOrder());
        org.setCreatedBy(creatorId);
        org.setUpdatedBy(creatorId);
        mapper.insert(org);
        return org.getOrgId();
    }

    @Override
    public void update(Long orgId, AdminOrgCommand command, Long operatorId) {
        mapper.update(orgId, command, operatorId);
    }

    @Override
    public void updateStatus(Long orgId, boolean enabled, Long operatorId) {
        mapper.updateStatus(orgId, enabled ? 1 : 0, operatorId);
    }

    @Override
    public void updateSort(AdminOrgSortCommand command, Long operatorId) {
        mapper.updateSort(command.getOrgId(), command.getSortOrder(), operatorId);
    }
}
