package com.qizhifu.jiaoxuepeiyu.admin.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminAccountServiceTests {

    @Test
    void createsTeacherWithHashedInitialPassword() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");

        Long userId = service.createTeacher(teacher());

        assertEquals(10L, userId.longValue());
        assertEquals("hashed:InitPass123", repository.createdPasswordHash);
        assertEquals("teacher", repository.createdCommand.getUserType());
        assertEquals("teacher001", repository.createdCommand.getAccountNo());
    }

    @Test
    void rejectsInvalidPhone() {
        AdminAccountService service = new AdminAccountService(new FakeAccounts(), new PrefixHasher(), "InitPass123");
        AdminAccountCommand command = teacher();
        command.setPhone("12345");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTeacher(command));

        assertEquals("Phone format is invalid", exception.getMessage());
    }

    @Test
    void rejectsInvalidIdCardWhenPresent() {
        AdminAccountService service = new AdminAccountService(new FakeAccounts(), new PrefixHasher(), "InitPass123");
        AdminAccountCommand command = teacher();
        command.setIdCard("123");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTeacher(command));

        assertEquals("ID card format is invalid", exception.getMessage());
    }

    @Test
    void rejectsStudentWithoutClass() {
        AdminAccountService service = new AdminAccountService(new FakeAccounts(), new PrefixHasher(), "InitPass123");
        AdminAccountCommand command = student();
        command.setClassId(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createStudent(command));

        assertEquals("Class is required", exception.getMessage());
    }

    @Test
    void batchResetPasswordUsesConfiguredInitialPassword() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");

        service.resetPasswords(Arrays.asList(1L, 2L));

        assertEquals(Arrays.asList(1L, 2L), repository.resetUserIds);
        assertEquals("hashed:InitPass123", repository.resetPasswordHash);
    }

    @Test
    void updatesTeacherWithoutAccountNumber() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");
        AdminAccountCommand command = teacher();
        command.setAccountNo(null);

        service.updateTeacher(10L, command);

        assertEquals(10L, repository.updatedUserId.longValue());
        assertEquals(null, repository.updatedCommand.getAccountNo());
        assertEquals(Arrays.asList(4L), repository.roleUserIds);
    }

    @Test
    void removesDuplicateBindingIds() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");
        AdminAccountCommand command = teacher();
        command.setRoleIds(Arrays.asList(4L, 4L, null, 5L));
        command.setManagedOrgIds(Arrays.asList(1L, 1L, 2L));

        service.createTeacher(command);

        assertEquals(Arrays.asList(4L, 5L), repository.roleUserIds);
        assertEquals(Arrays.asList(1L, 2L), repository.managedOrgIds);
    }

    @Test
    void rejectsMissingInitialPasswordForCreate() {
        AdminAccountService service = new AdminAccountService(new FakeAccounts(), new PrefixHasher(), " ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.createTeacher(teacher()));

        assertEquals("Initial password is not configured", exception.getMessage());
    }

    @Test
    void previewsImportRowsWithExistingAccountAndValidationErrors() {
        FakeAccounts repository = new FakeAccounts();
        repository.existingAccountNos = Arrays.asList("teacher001");
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");
        AdminAccountImportCommand command = new AdminAccountImportCommand();
        command.setRows(Arrays.asList(importRow(1, "teacher001", "Teacher One", "13812345678", 1L, null),
                importRow(2, "teacher003", "", "12345", 1L, null),
                importRow(3, "teacher002", "Teacher Two", "13812345679", 1L, null)));

        AdminAccountImportPreview preview = service.previewImport("teacher", command);

        assertEquals(1, preview.getValidCount().intValue());
        assertEquals(2, preview.getErrorCount().intValue());
        assertEquals("Account number already exists", preview.getRows().get(0).getErrors().get(0));
        assertEquals("Name is required", preview.getRows().get(1).getErrors().get(0));
    }

    @Test
    void importsValidStudentRowsThroughCreatePath() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");
        AdminAccountImportCommand command = new AdminAccountImportCommand();
        command.setRows(Arrays.asList(importRow(1, "student101", "Student A", "13812345678", 1L, 3L),
                importRow(2, "student102", "Student B", "13812345679", 1L, 3L)));

        AdminAccountImportResult result = service.importAccounts("student", command);

        assertEquals(2, result.getImportedCount().intValue());
        assertEquals(Arrays.asList(10L, 11L), result.getUserIds());
        assertEquals("student102", repository.createdCommands.get(1).getAccountNo());
        assertEquals("hashed:InitPass123", repository.createdPasswordHashes.get(1));
    }

    @Test
    void exportsMaskedAccountRows() {
        FakeAccounts repository = new FakeAccounts();
        repository.exportAccounts = Arrays.asList(account(10L, "teacher001", "Teacher One", "13812345678", "110101199001011234"));
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");

        List<AdminAccountExportRow> rows = service.exportAccounts("teacher", new AdminAccountQuery());

        assertEquals(1, rows.size());
        assertEquals("teacher001", rows.get(0).getAccountNo());
        assertEquals("138****5678", rows.get(0).getMaskedPhone());
        assertEquals("1101**********1234", rows.get(0).getMaskedIdCard());
    }

    @Test
    void normalizesJobTitleForPagedAccountSearch() {
        FakeAccounts repository = new FakeAccounts();
        AdminAccountService service = new AdminAccountService(repository, new PrefixHasher(), "InitPass123");
        AdminAccountQuery query = new AdminAccountQuery();
        query.setJobTitle(" Instructor ");

        service.listTeachers(query);

        assertEquals("teacher", repository.lastFindQuery.getUserType());
        assertEquals("%Instructor%", repository.lastFindQuery.getJobTitle());
        assertEquals("%Instructor%", repository.lastCountQuery.getJobTitle());
    }

    private AdminAccountCommand teacher() {
        AdminAccountCommand command = new AdminAccountCommand();
        command.setRealName("Teacher One");
        command.setAccountNo("teacher001");
        command.setPhone("13812345678");
        command.setIdCard("110101199001011234");
        command.setJobTitle("Teacher");
        command.setOrgId(1L);
        command.setManagedOrgIds(Arrays.asList(1L, 2L));
        command.setTeachingClassIds(Arrays.asList(3L));
        command.setRoleIds(Arrays.asList(4L));
        return command;
    }

    private AdminAccountCommand student() {
        AdminAccountCommand command = new AdminAccountCommand();
        command.setRealName("Student One");
        command.setAccountNo("student001");
        command.setPhone("13812345678");
        command.setIdCard("110101199001011234");
        command.setOrgId(1L);
        command.setClassId(3L);
        return command;
    }

    private AdminAccountImportRow importRow(int rowNo, String accountNo, String realName, String phone, Long orgId, Long classId) {
        AdminAccountImportRow row = new AdminAccountImportRow();
        row.setRowNo(Integer.valueOf(rowNo));
        row.setAccountNo(accountNo);
        row.setRealName(realName);
        row.setPhone(phone);
        row.setOrgId(orgId);
        row.setClassId(classId);
        return row;
    }

    private AdminAccount account(Long userId, String accountNo, String realName, String phone, String idCard) {
        AdminAccount account = new AdminAccount();
        account.setUserId(userId);
        account.setAccountNo(accountNo);
        account.setRealName(realName);
        account.setPhone(phone);
        account.setMaskedIdCard(idCard);
        account.setUserType("teacher");
        account.setOrgName("Org");
        account.setEnabled(true);
        return account;
    }

    private static class FakeAccounts implements AdminAccountRepository {
        private AdminAccountCommand createdCommand;
        private String createdPasswordHash;
        private final List<AdminAccountCommand> createdCommands = new ArrayList<AdminAccountCommand>();
        private final List<String> createdPasswordHashes = new ArrayList<String>();
        private Long updatedUserId;
        private AdminAccountCommand updatedCommand;
        private List<Long> roleUserIds;
        private List<Long> managedOrgIds;
        private List<Long> resetUserIds;
        private String resetPasswordHash;
        private List<String> existingAccountNos = new ArrayList<String>();
        private List<AdminAccount> exportAccounts = new ArrayList<AdminAccount>();
        private AdminAccountQuery lastFindQuery;
        private AdminAccountQuery lastCountQuery;

        @Override
        public List<AdminAccount> findAccounts(AdminAccountQuery query) {
            this.lastFindQuery = query;
            return Arrays.asList();
        }

        @Override
        public List<AdminAccount> findAccountsForExport(AdminAccountQuery query) {
            return exportAccounts;
        }

        @Override
        public long countAccounts(AdminAccountQuery query) {
            this.lastCountQuery = query;
            return 0;
        }

        @Override
        public AdminAccount findById(Long userId) {
            return null;
        }

        @Override
        public List<String> findExistingAccountNos(List<String> accountNos) {
            return existingAccountNos;
        }

        @Override
        public Long create(AdminAccountCommand command, String passwordHash) {
            this.createdCommand = command;
            this.createdPasswordHash = passwordHash;
            this.createdCommands.add(command);
            this.createdPasswordHashes.add(passwordHash);
            return Long.valueOf(9L + createdCommands.size());
        }

        @Override
        public void update(Long userId, AdminAccountCommand command) {
            this.updatedUserId = userId;
            this.updatedCommand = command;
        }

        @Override
        public void updateStatus(Long userId, boolean enabled) {
        }

        @Override
        public void resetPasswords(List<Long> userIds, String passwordHash) {
            this.resetUserIds = userIds;
            this.resetPasswordHash = passwordHash;
        }

        @Override
        public void updateOrg(List<Long> userIds, Long orgId) {
        }

        @Override
        public void replaceRoles(Long userId, List<Long> roleIds) {
            this.roleUserIds = roleIds;
        }

        @Override
        public void replaceManagedOrgs(Long userId, List<Long> orgIds) {
            this.managedOrgIds = orgIds;
        }

        @Override
        public void replaceTeachingClasses(Long userId, List<Long> classIds) {
        }
    }

    private static class PrefixHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hashed:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return false;
        }
    }
}
