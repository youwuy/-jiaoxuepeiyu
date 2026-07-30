package com.qizhifu.jiaoxuepeiyu.admin.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountCommand;
import com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccountQuery;
import com.qizhifu.jiaoxuepeiyu.admin.account.port.AdminAccountRepository;
import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
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

    private static class FakeAccounts implements AdminAccountRepository {
        private AdminAccountCommand createdCommand;
        private String createdPasswordHash;
        private Long updatedUserId;
        private AdminAccountCommand updatedCommand;
        private List<Long> roleUserIds;
        private List<Long> managedOrgIds;
        private List<Long> resetUserIds;
        private String resetPasswordHash;

        @Override
        public List<com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount> findAccounts(AdminAccountQuery query) {
            return Arrays.asList();
        }

        @Override
        public long countAccounts(AdminAccountQuery query) {
            return 0;
        }

        @Override
        public com.qizhifu.jiaoxuepeiyu.admin.account.model.AdminAccount findById(Long userId) {
            return null;
        }

        @Override
        public Long create(AdminAccountCommand command, String passwordHash) {
            this.createdCommand = command;
            this.createdPasswordHash = passwordHash;
            return 10L;
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
