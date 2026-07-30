package com.qizhifu.jiaoxuepeiyu.admin.profile.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.profile.AdminProfileService;
import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
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
@RequestMapping("/api/admin/profile")
@Tag(name = "Admin Profile", description = "Current admin or teacher profile APIs.")
public class AdminProfileController {

    private final AdminProfileService service;

    public AdminProfileController(AdminProfileService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get admin profile", description = "Returns the current admin or teacher profile with masked phone and ID card.")
    public ApiResponse<AdminProfile> get(HttpServletRequest request) {
        return ApiResponse.ok(service.getProfile(AdminContext.requireAdminId(request)));
    }

    @PutMapping("/phone")
    @Operation(summary = "Update admin phone", description = "Updates the current admin or teacher phone number.")
    public ApiResponse<Void> updatePhone(@Valid @RequestBody PhoneRequest body, HttpServletRequest request) {
        service.updatePhone(AdminContext.requireAdminId(request), body.getPhone());
        return ApiResponse.ok(null);
    }

    @PutMapping("/id-card")
    @Operation(summary = "Update admin ID card", description = "Updates the current admin or teacher ID card number.")
    public ApiResponse<Void> updateIdCard(@Valid @RequestBody IdCardRequest body, HttpServletRequest request) {
        service.updateIdCard(AdminContext.requireAdminId(request), body.getIdCard());
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
}
