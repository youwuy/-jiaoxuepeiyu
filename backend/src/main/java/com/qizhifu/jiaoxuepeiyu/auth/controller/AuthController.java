package com.qizhifu.jiaoxuepeiyu.auth.controller;

import com.qizhifu.jiaoxuepeiyu.auth.AuthService;
import com.qizhifu.jiaoxuepeiyu.auth.AuthenticationException;
import com.qizhifu.jiaoxuepeiyu.auth.BearerTokenResolver;
import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginCommand;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginIdentityType;
import com.qizhifu.jiaoxuepeiyu.auth.model.LoginResult;
import com.qizhifu.jiaoxuepeiyu.auth.model.Portal;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login APIs for admin and student portals.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Admin login", description = "Authenticates an admin or teacher account and returns token, expiry time, and user profile.")
    public ApiResponse<LoginResult> adminLogin(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest servletRequest) {
        LoginCommand command = request.toCommand(Portal.ADMIN, servletRequest.getRemoteAddr());
        return ApiResponse.ok(authService.login(command));
    }

    @PostMapping("/student/login")
    @Operation(summary = "Student login", description = "Authenticates a student account and returns token, expiry time, and user profile.")
    public ApiResponse<LoginResult> studentLogin(@Valid @RequestBody LoginRequest request,
                                                 HttpServletRequest servletRequest) {
        LoginCommand command = request.toCommand(Portal.STUDENT, servletRequest.getRemoteAddr());
        return ApiResponse.ok(authService.login(command));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current user", description = "Returns the authenticated user resolved from the Authorization Bearer token.")
    public ApiResponse<AuthenticatedUser> currentUser(HttpServletRequest request) {
        return ApiResponse.ok(authService.currentUser(requireBearerToken(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the current Authorization Bearer token session.")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(requireBearerToken(request));
        return ApiResponse.ok(null);
    }

    private String requireBearerToken(HttpServletRequest request) {
        return BearerTokenResolver.resolve(request.getHeader("Authorization"))
                .orElseThrow(() -> new AuthenticationException("Missing token"));
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
