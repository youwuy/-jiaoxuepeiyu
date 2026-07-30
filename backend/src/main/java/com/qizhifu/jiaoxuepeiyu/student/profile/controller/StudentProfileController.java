package com.qizhifu.jiaoxuepeiyu.student.profile.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.profile.StudentProfileService;
import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {

    private final StudentProfileService service;

    public StudentProfileController(StudentProfileService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<StudentProfile> get(HttpServletRequest request) {
        return ApiResponse.ok(service.getProfile(StudentContext.requireStudentId(request)));
    }

    @PutMapping("/phone")
    public ApiResponse<Void> updatePhone(@Valid @RequestBody PhoneRequest body, HttpServletRequest request) {
        service.updatePhone(StudentContext.requireStudentId(request), body.getPhone());
        return ApiResponse.ok(null);
    }

    @PutMapping("/id-card")
    public ApiResponse<Void> updateIdCard(@Valid @RequestBody IdCardRequest body, HttpServletRequest request) {
        service.updateIdCard(StudentContext.requireStudentId(request), body.getIdCard());
        return ApiResponse.ok(null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordRequest body, HttpServletRequest request) {
        service.changePassword(
                StudentContext.requireStudentId(request),
                body.getCurrentPassword(),
                body.getNewPassword(),
                body.getConfirmPassword());
        return ApiResponse.ok(null);
    }

    public static class PhoneRequest {
        @NotBlank
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class IdCardRequest {
        @NotBlank
        private String idCard;

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }

    public static class PasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
        @NotBlank
        private String confirmPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
