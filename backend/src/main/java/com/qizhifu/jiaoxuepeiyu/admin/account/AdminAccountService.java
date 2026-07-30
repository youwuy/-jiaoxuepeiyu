package com.qizhifu.jiaoxuepeiyu.admin.account;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.port.AdminAccountRepository;
import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccountService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminAccountRepository repository;
    private final PasswordHasher passwordHasher;
    private final String initialPassword;

    public AdminAccountService(AdminAccountRepository repository,
                               PasswordHasher passwordHasher,
                               @Value("${app.account.initial-password:}") String initialPassword) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.initialPassword = initialPassword;
    }

    public PageResponse<AdminAccount> listTeachers(AdminAccountQuery query) {
        return list("teacher", query);
    }

    public PageResponse<AdminAccount> listStudents(AdminAccountQuery query) {
        return list("student", query);
    }

    public AdminAccount get(Long userId) {
        AdminAccount account = repository.findById(userId);
        if (account == null) {
            throw new BusinessException(404, "Account not found");
        }
        mask(account);
        return account;
    }

    @Transactional
    public Long createTeacher(AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "teacher", true);
        String passwordHash = passwordHasher.hash(requireInitialPassword());
        Long userId = repository.create(normalized, passwordHash);
        repository.replaceRoles(userId, normalized.getRoleIds());
        repository.replaceManagedOrgs(userId, normalized.getManagedOrgIds());
        repository.replaceTeachingClasses(userId, normalized.getTeachingClassIds());
        return userId;
    }

    @Transactional
    public Long createStudent(AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "student", true);
        String passwordHash = passwordHasher.hash(requireInitialPassword());
        return repository.create(normalized, passwordHash);
    }

    @Transactional
    public void updateTeacher(Long userId, AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "teacher", false);
        repository.update(userId, normalized);
        repository.replaceRoles(userId, normalized.getRoleIds());
        repository.replaceManagedOrgs(userId, normalized.getManagedOrgIds());
        repository.replaceTeachingClasses(userId, normalized.getTeachingClassIds());
    }

    @Transactional
    public void updateStudent(Long userId, AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "student", false);
        repository.update(userId, normalized);
    }

    public void enable(Long userId) {
        repository.updateStatus(userId, true);
    }

    public void disable(Long userId) {
        repository.updateStatus(userId, false);
    }

    public void resetPasswords(List<Long> userIds) {
        List<Long> normalizedUserIds = unique(userIds);
        if (normalizedUserIds.isEmpty()) {
            throw new BusinessException(400, "Accounts are required");
        }
        repository.resetPasswords(normalizedUserIds, passwordHasher.hash(requireInitialPassword()));
    }

    public void updateOrg(List<Long> userIds, Long orgId) {
        List<Long> normalizedUserIds = unique(userIds);
        if (normalizedUserIds.isEmpty()) {
            throw new BusinessException(400, "Accounts are required");
        }
        if (orgId == null) {
            throw new BusinessException(400, "Organization is required");
        }
        repository.updateOrg(normalizedUserIds, orgId);
    }

    public void updateRoles(Long userId, List<Long> roleIds) {
        List<Long> normalizedRoleIds = unique(roleIds);
        if (normalizedRoleIds.isEmpty()) {
            throw new BusinessException(400, "Roles are required");
        }
        repository.replaceRoles(userId, normalizedRoleIds);
    }

    private PageResponse<AdminAccount> list(String userType, AdminAccountQuery query) {
        AdminAccountQuery normalized = normalizedQuery(query, userType);
        List<AdminAccount> accounts = repository.findAccounts(normalized);
        for (AdminAccount account : accounts) {
            mask(account);
        }
        return new PageResponse<AdminAccount>(
                accounts,
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countAccounts(normalized));
    }

    private AdminAccountQuery normalizedQuery(AdminAccountQuery query, String userType) {
        AdminAccountQuery normalized = query == null ? new AdminAccountQuery() : query;
        normalized.setUserType(userType);
        normalized.setRealName(like(normalized.getRealName()));
        normalized.setAccountNo(like(normalized.getAccountNo()));
        normalized.setPhone(like(normalized.getPhone()));
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1 || normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(20);
        }
        return normalized;
    }

    private AdminAccountCommand normalized(AdminAccountCommand command, String userType, boolean requireAccountNo) {
        if (command == null) {
            throw new BusinessException(400, "Account information is required");
        }
        if (!InputValidator.hasText(command.getRealName())) {
            throw new BusinessException(400, "Name is required");
        }
        if (requireAccountNo && !InputValidator.hasText(command.getAccountNo())) {
            throw new BusinessException(400, "Account number is required");
        }
        if (!InputValidator.isPhone(command.getPhone())) {
            throw new BusinessException(400, "Phone format is invalid");
        }
        if (InputValidator.hasText(command.getIdCard()) && !InputValidator.isIdCard(command.getIdCard())) {
            throw new BusinessException(400, "ID card format is invalid");
        }
        if (command.getOrgId() == null) {
            throw new BusinessException(400, "Organization is required");
        }
        if ("student".equals(userType) && command.getClassId() == null) {
            throw new BusinessException(400, "Class is required");
        }

        AdminAccountCommand normalized = new AdminAccountCommand();
        normalized.setRealName(command.getRealName().trim());
        normalized.setAccountNo(trimToNull(command.getAccountNo()));
        normalized.setPhone(command.getPhone().trim());
        normalized.setIdCard(trimToNull(command.getIdCard()));
        normalized.setJobTitle(trimToNull(command.getJobTitle()));
        normalized.setUserType(userType);
        normalized.setOrgId(command.getOrgId());
        normalized.setClassId("student".equals(userType) ? command.getClassId() : null);
        normalized.setFaceFileId(command.getFaceFileId());
        normalized.setFingerprintFileId(command.getFingerprintFileId());
        normalized.setRoleIds(unique(command.getRoleIds()));
        normalized.setManagedOrgIds(unique(command.getManagedOrgIds()));
        normalized.setTeachingClassIds(unique(command.getTeachingClassIds()));
        return normalized;
    }

    private String requireInitialPassword() {
        if (!InputValidator.hasText(initialPassword)) {
            throw new BusinessException(500, "Initial password is not configured");
        }
        return initialPassword;
    }

    private String like(String value) {
        return InputValidator.hasText(value) ? "%" + value.trim() + "%" : null;
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private List<Long> unique(List<Long> ids) {
        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        if (ids != null) {
            for (Long id : ids) {
                if (id != null) {
                    uniqueIds.add(id);
                }
            }
        }
        return new ArrayList<Long>(uniqueIds);
    }

    private void mask(AdminAccount account) {
        account.setMaskedPhone(maskPhone(account.getPhone()));
        account.setMaskedIdCard(maskIdCard(account.getMaskedIdCard()));
        account.setPhone(null);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
}
