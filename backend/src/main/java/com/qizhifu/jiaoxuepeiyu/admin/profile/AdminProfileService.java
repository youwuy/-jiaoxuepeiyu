package com.qizhifu.jiaoxuepeiyu.admin.profile;

import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import com.qizhifu.jiaoxuepeiyu.admin.profile.port.AdminProfileRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminProfileService {

    private final AdminProfileRepository repository;

    public AdminProfileService(AdminProfileRepository repository) {
        this.repository = repository;
    }

    public AdminProfile getProfile(Long userId) {
        AdminProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(404, "Admin profile not found"));
        profile.setPhone(maskPhone(profile.getPhone()));
        profile.setIdCard(maskIdCard(profile.getIdCard()));
        return profile;
    }

    @Transactional
    public void updatePhone(Long userId, String phone) {
        if (!InputValidator.isPhone(phone)) {
            throw new BusinessException(400, "Phone format is invalid");
        }
        repository.updatePhone(userId, phone);
    }

    @Transactional
    public void updateIdCard(Long userId, String idCard) {
        if (!InputValidator.isIdCard(idCard)) {
            throw new BusinessException(400, "ID card format is invalid");
        }
        repository.updateIdCard(userId, idCard);
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
