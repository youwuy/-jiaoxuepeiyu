package com.qizhifu.jiaoxuepeiyu.student.profile;

import com.qizhifu.jiaoxuepeiyu.auth.port.PasswordHasher;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.common.validation.PasswordPolicy;
import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import com.qizhifu.jiaoxuepeiyu.student.profile.port.StudentProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentProfileService {

    private final StudentProfileRepository repository;
    private final PasswordHasher passwordHasher;

    public StudentProfileService(StudentProfileRepository repository, PasswordHasher passwordHasher) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public StudentProfile getProfile(Long studentId) {
        StudentProfile profile = repository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(404, "Student profile not found"));
        profile.setPhone(maskPhone(profile.getPhone()));
        profile.setIdCard(maskIdCard(profile.getIdCard()));
        profile.setPasswordHash(null);
        return profile;
    }

    @Transactional
    public void updatePhone(Long studentId, String phone) {
        if (!InputValidator.isPhone(phone)) {
            throw new BusinessException(400, "Phone format is invalid");
        }
        repository.updatePhone(studentId, phone);
    }

    @Transactional
    public void updateIdCard(Long studentId, String idCard) {
        if (!InputValidator.isIdCard(idCard)) {
            throw new BusinessException(400, "ID card format is invalid");
        }
        repository.updateIdCard(studentId, idCard);
    }

    @Transactional
    public void changePassword(Long studentId, String currentPassword, String newPassword, String confirmPassword) {
        PasswordPolicy.Result result = PasswordPolicy.validateChange(currentPassword, newPassword, confirmPassword);
        if (!result.isValid()) {
            throw new BusinessException(400, firstError(result.getErrors()));
        }

        StudentProfile profile = repository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(404, "Student profile not found"));
        if (!passwordHasher.matches(currentPassword, profile.getPasswordHash())) {
            throw new BusinessException(400, "Current password is incorrect");
        }
        repository.updatePasswordHash(studentId, passwordHasher.hash(newPassword));
    }

    private String firstError(List<String> errors) {
        return errors.isEmpty() ? "Invalid password" : errors.get(0);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
