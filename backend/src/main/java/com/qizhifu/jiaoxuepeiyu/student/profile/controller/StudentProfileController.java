package com.qizhifu.jiaoxuepeiyu.student.profile.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.student.StudentContext;
import com.qizhifu.jiaoxuepeiyu.student.profile.StudentProfileService;
import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Student Profile", description = "Student profile and password APIs. Header X-User-Id identifies the student.")
public class StudentProfileController {

    private final StudentProfileService service;

    public StudentProfileController(StudentProfileService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get student profile", description = "Returns the current student's profile.")
    public ApiResponse<StudentProfile> get(HttpServletRequest request) {
        return ApiResponse.ok(service.getProfile(StudentContext.requireStudentId(request)));
    }

    @PutMapping("/phone")
    @Operation(summary = "Update phone", description = "Updates the current student's phone number.")
    public ApiResponse<Void> updatePhone(@Valid @RequestBody PhoneRequest body, HttpServletRequest request) {
        service.updatePhone(StudentContext.requireStudentId(request), body.getPhone());
        return ApiResponse.ok(null);
    }

    @PutMapping("/id-card")
    @Operation(summary = "Update ID card", description = "Updates the current student's ID card number.")
    public ApiResponse<Void> updateIdCard(@Valid @RequestBody IdCardRequest body, HttpServletRequest request) {
        service.updateIdCard(StudentContext.requireStudentId(request), body.getIdCard());
        return ApiResponse.ok(null);
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Changes the current student's password after validating the current password and password policy.")
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
