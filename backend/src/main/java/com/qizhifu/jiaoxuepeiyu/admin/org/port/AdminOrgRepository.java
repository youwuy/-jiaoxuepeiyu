package com.qizhifu.jiaoxuepeiyu.admin.org.port;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgSortCommand;
import java.util.List;

public interface AdminOrgRepository {

    List<AdminOrg> findAll();

    Long create(AdminOrgCommand command, Long creatorId);

    void update(Long orgId, AdminOrgCommand command, Long operatorId);

    void updateStatus(Long orgId, boolean enabled, Long operatorId);

    void updateSort(AdminOrgSortCommand command, Long operatorId);
}
