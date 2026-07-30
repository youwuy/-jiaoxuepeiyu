package com.qizhifu.jiaoxuepeiyu.admin.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import com.qizhifu.jiaoxuepeiyu.admin.profile.port.AdminProfileRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AdminProfileServiceTests {

    @Test
    void returnsMaskedProfile() {
        AdminProfileService service = new AdminProfileService(new FakeProfiles());

        AdminProfile profile = service.getProfile(7L);

        assertEquals("teacher001", profile.getAccountNo());
        assertEquals("138****5678", profile.getPhone());
        assertEquals("1101**********1234", profile.getIdCard());
    }

    @Test
    void updatesPhoneAfterFormatValidation() {
        FakeProfiles profiles = new FakeProfiles();
        AdminProfileService service = new AdminProfileService(profiles);

        service.updatePhone(7L, "13812345679");

        assertEquals("13812345679", profiles.updatedPhone);
    }

    @Test
    void rejectsInvalidPhone() {
        AdminProfileService service = new AdminProfileService(new FakeProfiles());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.updatePhone(7L, "123"));

        assertEquals("Phone format is invalid", exception.getMessage());
    }

    @Test
    void updatesIdCardAfterFormatValidation() {
        FakeProfiles profiles = new FakeProfiles();
        AdminProfileService service = new AdminProfileService(profiles);

        service.updateIdCard(7L, "110101199001011235");

        assertEquals("110101199001011235", profiles.updatedIdCard);
    }

    @Test
    void rejectsInvalidIdCard() {
        AdminProfileService service = new AdminProfileService(new FakeProfiles());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.updateIdCard(7L, "123"));

        assertEquals("ID card format is invalid", exception.getMessage());
    }

    private static class FakeProfiles implements AdminProfileRepository {
        private String updatedPhone;
        private String updatedIdCard;

        @Override
        public Optional<AdminProfile> findByUserId(Long userId) {
            AdminProfile profile = new AdminProfile();
            profile.setUserId(userId);
            profile.setAccountNo("teacher001");
            profile.setRealName("Teacher One");
            profile.setUserType("teacher");
            profile.setPhone("13812345678");
            profile.setIdCard("110101199001011234");
            profile.setOrgName("Training Center");
            profile.setJobTitle("Teacher");
            return Optional.of(profile);
        }

        @Override
        public void updatePhone(Long userId, String phone) {
            this.updatedPhone = phone;
        }

        @Override
        public void updateIdCard(Long userId, String idCard) {
            this.updatedIdCard = idCard;
        }
    }
}
