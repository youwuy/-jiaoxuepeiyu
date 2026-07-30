package com.qizhifu.jiaoxuepeiyu.student.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import com.qizhifu.jiaoxuepeiyu.student.profile.port.StudentProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentProfileServiceTests {

    @Test
    void updatesPhoneAfterFormatValidation() {
        FakeProfiles profiles = new FakeProfiles();
        StudentProfileService service = new StudentProfileService(profiles, new PlainHasher());

        service.updatePhone(7L, "13812345678");

        assertEquals("13812345678", profiles.updatedPhone);
    }

    @Test
    void rejectsInvalidPhone() {
        StudentProfileService service = new StudentProfileService(new FakeProfiles(), new PlainHasher());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.updatePhone(7L, "123");
        });

        assertEquals("Phone format is invalid", exception.getMessage());
    }

    @Test
    void returnsMaskedProfileWithoutPasswordHash() {
        StudentProfileService service = new StudentProfileService(new FakeProfiles(), new PlainHasher());

        StudentProfile profile = service.getProfile(7L);

        assertEquals("138****5678", profile.getPhone());
        assertEquals("110***********1234", profile.getIdCard());
        assertNull(profile.getPasswordHash());
    }

    @Test
    void updatesIdCardAfterFormatValidation() {
        FakeProfiles profiles = new FakeProfiles();
        StudentProfileService service = new StudentProfileService(profiles, new PlainHasher());

        service.updateIdCard(7L, "110101199001011234");

        assertEquals("110101199001011234", profiles.updatedIdCard);
    }

    @Test
    void changesPasswordAfterPolicyAndCurrentPasswordValidation() {
        FakeProfiles profiles = new FakeProfiles();
        StudentProfileService service = new StudentProfileService(profiles, new PlainHasher());

        service.changePassword(7L, "oldPass123", "newPass123", "newPass123");

        assertEquals("newPass123", profiles.updatedPasswordHash);
    }

    @Test
    void rejectsWrongCurrentPassword() {
        StudentProfileService service = new StudentProfileService(new FakeProfiles(), new PlainHasher());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.changePassword(7L, "wrongPass123", "newPass123", "newPass123");
        });

        assertEquals("Current password is incorrect", exception.getMessage());
    }

    private static class FakeProfiles implements StudentProfileRepository {
        private String updatedPhone;
        private String updatedIdCard;
        private String updatedPasswordHash;

        @Override
        public Optional<StudentProfile> findByStudentId(Long studentId) {
            StudentProfile profile = new StudentProfile();
            profile.setStudentId(studentId);
            profile.setPhone("13812345678");
            profile.setIdCard("110101199001011234");
            profile.setPasswordHash("oldPass123");
            return Optional.of(profile);
        }

        @Override
        public void updatePhone(Long studentId, String phone) {
            this.updatedPhone = phone;
        }

        @Override
        public void updateIdCard(Long studentId, String idCard) {
            this.updatedIdCard = idCard;
        }

        @Override
        public void updatePasswordHash(Long studentId, String passwordHash) {
            this.updatedPasswordHash = passwordHash;
        }
    }

    private static class PlainHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return rawPassword.equals(passwordHash);
        }
    }
}
