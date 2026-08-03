package com.qizhifu.jiaoxuepeiyu.admin.account;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountExportRow;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.port.AdminAccountRepository;
import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
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
        attachMasks(account);
        return account;
    }

    @Transactional
    public Long createTeacher(AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "teacher", true);
        ensureAccountNoAvailable(normalized.getAccountNo());
        String passwordHash = passwordHasher.hash(resolveInitialPassword(normalized));
        Long userId = createAccount(normalized, passwordHash);
        repository.replaceRoles(userId, normalized.getRoleIds());
        repository.replaceManagedOrgs(userId, normalized.getManagedOrgIds());
        repository.replaceTeachingClasses(userId, normalized.getTeachingClassIds());
        return userId;
    }

    @Transactional
    public Long createStudent(AdminAccountCommand command) {
        AdminAccountCommand normalized = normalized(command, "student", true);
        ensureAccountNoAvailable(normalized.getAccountNo());
        String passwordHash = passwordHasher.hash(resolveInitialPassword(normalized));
        return createAccount(normalized, passwordHash);
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
        resetPasswords(userIds, requireInitialPassword());
    }

    public void resetPasswords(List<Long> userIds, String password) {
        List<Long> normalizedUserIds = unique(userIds);
        if (normalizedUserIds.isEmpty()) {
            throw new BusinessException(400, "Accounts are required");
        }
        String normalizedPassword = normalizedResetPassword(password);
        repository.resetPasswords(normalizedUserIds, passwordHasher.hash(normalizedPassword));
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

    public AdminAccountImportPreview previewImport(String userType, AdminAccountImportCommand command) {
        return preview(normalizeUserType(userType), command);
    }

    @Transactional
    public AdminAccountImportResult importAccounts(String userType, AdminAccountImportCommand command) {
        String normalizedUserType = normalizeUserType(userType);
        AdminAccountImportPreview preview = preview(normalizedUserType, command);
        if (preview.getErrorCount().intValue() > 0) {
            throw new BusinessException(400, "Import rows contain errors");
        }
        List<Long> userIds = new ArrayList<Long>();
        for (AdminAccountImportRow row : preview.getRows()) {
            AdminAccountCommand account = toCommand(row);
            if ("teacher".equals(normalizedUserType)) {
                userIds.add(createTeacher(account));
            } else {
                userIds.add(createStudent(account));
            }
        }
        AdminAccountImportResult result = new AdminAccountImportResult();
        result.setImportedCount(Integer.valueOf(userIds.size()));
        result.setUserIds(userIds);
        return result;
    }

    public List<AdminAccountExportRow> exportAccounts(String userType, AdminAccountQuery query) {
        AdminAccountQuery normalized = normalizedQuery(query, normalizeUserType(userType));
        List<AdminAccountExportRow> rows = new ArrayList<AdminAccountExportRow>();
        for (AdminAccount account : repository.findAccountsForExport(normalized)) {
            rows.add(toExportRow(account));
        }
        return rows;
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
        normalized.setJobTitle(like(normalized.getJobTitle()));
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
        if ("teacher".equals(userType) && command.getOrgId() == null) {
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
        normalized.setInitialPassword(trimToNull(command.getInitialPassword()));
        normalized.setRoleIds(unique(command.getRoleIds()));
        normalized.setManagedOrgIds(unique(command.getManagedOrgIds()));
        normalized.setTeachingClassIds(unique(command.getTeachingClassIds()));
        return normalized;
    }

    private AdminAccountImportPreview preview(String userType, AdminAccountImportCommand command) {
        if (command == null || command.getRows().isEmpty()) {
            throw new BusinessException(400, "Import rows are required");
        }
        List<AdminAccountImportRow> rows = command.getRows();
        Set<String> accountNos = new LinkedHashSet<String>();
        Set<String> duplicateNos = duplicateAccountNos(rows);
        for (AdminAccountImportRow row : rows) {
            String accountNo = trimToNull(row == null ? null : row.getAccountNo());
            if (accountNo != null) {
                accountNos.add(accountNo);
            }
        }
        Set<String> existingNos = new HashSet<String>(repository.findExistingAccountNos(new ArrayList<String>(accountNos)));

        int validCount = 0;
        List<AdminAccountImportRow> previewRows = new ArrayList<AdminAccountImportRow>();
        for (int i = 0; i < rows.size(); i++) {
            AdminAccountImportRow source = rows.get(i);
            AdminAccountImportRow row = copyImportRow(source, i + 1);
            List<String> errors = validateImportRow(row, userType);
            String accountNo = trimToNull(row.getAccountNo());
            if (accountNo != null && existingNos.contains(accountNo)) {
                errors.add("Account number already exists");
            }
            if (accountNo != null && duplicateNos.contains(accountNo)) {
                errors.add("Account number is duplicated in import rows");
            }
            row.setErrors(errors);
            row.setValid(Boolean.valueOf(errors.isEmpty()));
            if (errors.isEmpty()) {
                validCount++;
            }
            previewRows.add(row);
        }
        AdminAccountImportPreview preview = new AdminAccountImportPreview();
        preview.setTotalCount(Integer.valueOf(previewRows.size()));
        preview.setValidCount(Integer.valueOf(validCount));
        preview.setErrorCount(Integer.valueOf(previewRows.size() - validCount));
        preview.setRows(previewRows);
        return preview;
    }

    private List<String> validateImportRow(AdminAccountImportRow row, String userType) {
        List<String> errors = new ArrayList<String>();
        if (!InputValidator.hasText(row.getAccountNo())) {
            errors.add("Account number is required");
        }
        if (!InputValidator.hasText(row.getRealName())) {
            errors.add("Name is required");
        }
        if (!InputValidator.isPhone(row.getPhone())) {
            errors.add("Phone format is invalid");
        }
        if ("teacher".equals(userType) && row.getOrgId() == null) {
            errors.add("Organization is required");
        }
        if ("student".equals(userType) && row.getClassId() == null) {
            errors.add("Class is required");
        }
        if (InputValidator.hasText(row.getIdCard()) && !InputValidator.isIdCard(row.getIdCard())) {
            errors.add("ID card format is invalid");
        }
        return errors;
    }

    private Set<String> duplicateAccountNos(List<AdminAccountImportRow> rows) {
        Set<String> seen = new HashSet<String>();
        Set<String> duplicates = new HashSet<String>();
        for (AdminAccountImportRow row : rows) {
            String accountNo = trimToNull(row == null ? null : row.getAccountNo());
            if (accountNo != null && !seen.add(accountNo)) {
                duplicates.add(accountNo);
            }
        }
        return duplicates;
    }

    private AdminAccountImportRow copyImportRow(AdminAccountImportRow source, int defaultRowNo) {
        AdminAccountImportRow row = new AdminAccountImportRow();
        if (source != null) {
            row.setRowNo(source.getRowNo() == null ? Integer.valueOf(defaultRowNo) : source.getRowNo());
            row.setAccountNo(trimToNull(source.getAccountNo()));
            row.setRealName(trimToNull(source.getRealName()));
            row.setPhone(trimToNull(source.getPhone()));
            row.setIdCard(trimToNull(source.getIdCard()));
            row.setJobTitle(trimToNull(source.getJobTitle()));
            row.setOrgId(source.getOrgId());
            row.setClassId(source.getClassId());
            row.setRoleIds(unique(source.getRoleIds()));
            row.setManagedOrgIds(unique(source.getManagedOrgIds()));
            row.setTeachingClassIds(unique(source.getTeachingClassIds()));
        } else {
            row.setRowNo(Integer.valueOf(defaultRowNo));
        }
        return row;
    }

    private AdminAccountCommand toCommand(AdminAccountImportRow row) {
        AdminAccountCommand command = new AdminAccountCommand();
        command.setAccountNo(row.getAccountNo());
        command.setRealName(row.getRealName());
        command.setPhone(row.getPhone());
        command.setIdCard(row.getIdCard());
        command.setJobTitle(row.getJobTitle());
        command.setOrgId(row.getOrgId());
        command.setClassId(row.getClassId());
        command.setRoleIds(row.getRoleIds());
        command.setManagedOrgIds(row.getManagedOrgIds());
        command.setTeachingClassIds(row.getTeachingClassIds());
        return command;
    }

    private AdminAccountExportRow toExportRow(AdminAccount account) {
        AdminAccountExportRow row = new AdminAccountExportRow();
        row.setUserId(account.getUserId());
        row.setAccountNo(account.getAccountNo());
        row.setRealName(account.getRealName());
        row.setMaskedPhone(maskPhone(account.getPhone()));
        row.setMaskedIdCard(maskIdCard(account.getIdCard()));
        row.setUserType(account.getUserType());
        row.setOrgName(account.getOrgName());
        row.setClassName(account.getClassName());
        row.setJobTitle(account.getJobTitle());
        row.setEnabled(Boolean.valueOf(account.isEnabled()));
        row.setCreatedAt(account.getCreatedAt());
        return row;
    }

    private String normalizeUserType(String userType) {
        if (!"teacher".equals(userType) && !"student".equals(userType)) {
            throw new BusinessException(400, "Account type is invalid");
        }
        return userType;
    }

    private String requireInitialPassword() {
        if (!InputValidator.hasText(initialPassword)) {
            throw new BusinessException(500, "Initial password is not configured");
        }
        return initialPassword;
    }

    private String resolveInitialPassword(AdminAccountCommand command) {
        if (InputValidator.hasText(command.getInitialPassword())) {
            return normalizedResetPassword(command.getInitialPassword());
        }
        return requireInitialPassword();
    }

    private String normalizedResetPassword(String password) {
        String normalized = trimToNull(password);
        if (normalized == null) {
            throw new BusinessException(400, "Password is required");
        }
        if (normalized.length() < 8 || normalized.length() > 20) {
            throw new BusinessException(400, "Password length must be 8-20 characters");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetter(ch)) {
                hasLetter = true;
            }
            if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(400, "Password must contain letters and digits");
        }
        return normalized;
    }

    private void ensureAccountNoAvailable(String accountNo) {
        List<String> accountNos = new ArrayList<String>();
        accountNos.add(accountNo);
        if (!repository.findExistingAccountNos(accountNos).isEmpty()) {
            throw new BusinessException(400, "Account number already exists");
        }
    }

    private Long createAccount(AdminAccountCommand command, String passwordHash) {
        try {
            return repository.create(command, passwordHash);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(400, "Account number already exists");
        }
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
        attachMasks(account);
        account.setPhone(null);
        account.setIdCard(null);
    }

    private void attachMasks(AdminAccount account) {
        account.setMaskedPhone(maskPhone(account.getPhone()));
        account.setMaskedIdCard(maskIdCard(account.getIdCard()));
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
