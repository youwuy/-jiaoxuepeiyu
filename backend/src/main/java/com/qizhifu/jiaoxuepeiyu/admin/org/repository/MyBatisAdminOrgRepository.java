package com.qizhifu.jiaoxuepeiyu.admin.org.repository;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
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
    public Long create(AdminOrgCommand command) {
        AdminOrg org = new AdminOrg();
        org.setParentId(command.getParentId());
        org.setOrgName(command.getOrgName());
        org.setSortOrder(command.getSortOrder());
        mapper.insert(org);
        return org.getOrgId();
    }

    @Override
    public void update(Long orgId, AdminOrgCommand command) {
        mapper.update(orgId, command);
    }

    @Override
    public void updateStatus(Long orgId, boolean enabled) {
        mapper.updateStatus(orgId, enabled ? 1 : 0);
    }
}
