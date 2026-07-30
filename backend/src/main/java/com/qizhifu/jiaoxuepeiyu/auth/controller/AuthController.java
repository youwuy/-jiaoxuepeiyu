package com.qizhifu.jiaoxuepeiyu.auth.controller;

import com.qizhifu.jiaoxuepeiyu.auth.AuthService;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginCommand;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginIdentityType;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginResult;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginResult> adminLogin(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest servletRequest) {
        LoginCommand command = request.toCommand(Portal.ADMIN, servletRequest.getRemoteAddr());
        return ApiResponse.ok(authService.login(command));
    }

    @PostMapping("/student/login")
    public ApiResponse<LoginResult> studentLogin(@Valid @RequestBody LoginRequest request,
                                                 HttpServletRequest servletRequest) {
        LoginCommand command = request.toCommand(Portal.STUDENT, servletRequest.getRemoteAddr());
        return ApiResponse.ok(authService.login(command));
    }

    public static class LoginRequest {
        @NotBlank
        private String loginType;
        @NotBlank
        private String account;
        @NotBlank
        private String password;

        public LoginCommand toCommand(Portal portal, String loginIp) {
            return new LoginCommand(portal, LoginIdentityType.fromValue(loginType), account, password, loginIp);
        }

        public String getLoginType() {
            return loginType;
        }

        public void setLoginType(String loginType) {
            this.loginType = loginType;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
