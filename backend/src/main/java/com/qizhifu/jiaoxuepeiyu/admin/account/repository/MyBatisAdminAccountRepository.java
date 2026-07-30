package com.qizhifu.jiaoxuepeiyu.admin.account.repository;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.port.AdminAccountRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminAccountRepository implements AdminAccountRepository {

    private static final String MANAGED_SCOPE = "MANAGED";
    private static final String TEACHING_CLASS_SCOPE = "TEACHING_CLASS";

    private final AdminAccountMapper mapper;

    public MyBatisAdminAccountRepository(AdminAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<AdminAccount> findAccounts(AdminAccountQuery query) {
        List<AdminAccount> accounts = mapper.findAccounts(query);
        for (AdminAccount account : accounts) {
            enrich(account);
        }
        return accounts;
    }

    @Override
    public long countAccounts(AdminAccountQuery query) {
        return mapper.countAccounts(query);
    }

    @Override
    public AdminAccount findById(Long userId) {
        AdminAccount account = mapper.findById(userId);
        if (account != null) {
            enrich(account);
        }
        return account;
    }

    @Override
    public Long create(AdminAccountCommand command, String passwordHash) {
        mapper.insert(command, passwordHash);
        return command.getUserId();
    }

    @Override
    public void update(Long userId, AdminAccountCommand command) {
        mapper.update(userId, command);
    }

    @Override
    public void updateStatus(Long userId, boolean enabled) {
        mapper.updateStatus(userId, enabled ? 1 : 0);
    }

    @Override
    public void resetPasswords(List<Long> userIds, String passwordHash) {
        mapper.resetPasswords(userIds, passwordHash);
    }

    @Override
    public void updateOrg(List<Long> userIds, Long orgId) {
        mapper.updateOrg(userIds, orgId);
    }

    @Override
    public void replaceRoles(Long userId, List<Long> roleIds) {
        mapper.deleteRoles(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            mapper.insertRoles(userId, roleIds);
        }
    }

    @Override
    public void replaceManagedOrgs(Long userId, List<Long> orgIds) {
        replaceScopes(userId, MANAGED_SCOPE, orgIds);
    }

    @Override
    public void replaceTeachingClasses(Long userId, List<Long> classIds) {
        replaceScopes(userId, TEACHING_CLASS_SCOPE, classIds);
    }

    private void replaceScopes(Long userId, String scopeType, List<Long> ids) {
        mapper.deleteScopes(userId, scopeType);
        if (ids != null && !ids.isEmpty()) {
            mapper.insertScopes(userId, scopeType, ids);
        }
    }

    private void enrich(AdminAccount account) {
        account.setRoleIds(mapper.findRoleIds(account.getUserId()));
        account.setRoleNames(mapper.findRoleNames(account.getUserId()));
        account.setManagedOrgIds(mapper.findScopeIds(account.getUserId(), MANAGED_SCOPE));
        account.setTeachingClassIds(mapper.findScopeIds(account.getUserId(), TEACHING_CLASS_SCOPE));
    }
}
