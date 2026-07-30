package com.qizhifu.jiaoxuepeiyu.admin.org.port;

import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrg;
import com.qizhifu.jiaoxuepeiyu.admin.org.model.AdminOrgCommand;
import java.util.List;

public interface AdminOrgRepository {

    List<AdminOrg> findAll();

    Long create(AdminOrgCommand command);

    void update(Long orgId, AdminOrgCommand command);

    void updateStatus(Long orgId, boolean enabled);
}
