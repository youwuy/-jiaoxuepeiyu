package com.qizhifu.jiaoxuepeiyu.admin.account.port;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import java.util.List;

public interface AdminAccountRepository {

    List<AdminAccount> findAccounts(AdminAccountQuery query);

    long countAccounts(AdminAccountQuery query);

    AdminAccount findById(Long userId);

    Long create(AdminAccountCommand command, String passwordHash);

    void update(Long userId, AdminAccountCommand command);

    void updateStatus(Long userId, boolean enabled);

    void resetPasswords(List<Long> userIds, String passwordHash);

    void updateOrg(List<Long> userIds, Long orgId);

    void replaceRoles(Long userId, List<Long> roleIds);

    void replaceManagedOrgs(Long userId, List<Long> orgIds);

    void replaceTeachingClasses(Long userId, List<Long> classIds);
}
