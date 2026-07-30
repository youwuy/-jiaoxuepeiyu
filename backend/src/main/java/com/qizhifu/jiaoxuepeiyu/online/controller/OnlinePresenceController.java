package com.qizhifu.jiaoxuepeiyu.online.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.auth.AuthenticatedUserContext;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.online.OnlinePresenceService;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineHeartbeatResult;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlinePresenceDashboard;
import com.qizhifu.jiaoxuepeiyu.online.model.OnlineUserQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Online Presence", description = "Heartbeat and online status APIs.")
public class OnlinePresenceController {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final OnlinePresenceService service;

    public OnlinePresenceController(OnlinePresenceService service) {
        this.service = service;
    }

    @PostMapping("/online/heartbeat")
    @Operation(summary = "Send heartbeat", description = "Updates the current user's heartbeat time and IP address.")
    public ApiResponse<OnlineHeartbeatResult> heartbeat(HttpServletRequest request) {
        return ApiResponse.ok(service.heartbeat(requireCurrentUserId(request), clientIp(request)));
    }

    @PostMapping("/online/offline")
    @Operation(summary = "Mark current user offline", description = "Clears the current user's heartbeat so management pages show the user offline immediately.")
    public ApiResponse<Void> offline(HttpServletRequest request) {
        service.markOffline(requireCurrentUserId(request));
        return ApiResponse.ok(null);
    }

    @GetMapping("/admin/online/users")
    @Operation(summary = "List online users", description = "Returns online user summary and current user states for management dashboards.")
    public ApiResponse<OnlinePresenceDashboard> listOnlineUsers(@ModelAttribute OnlineUserQuery query,
                                                                HttpServletRequest request) {
        AdminContext.requireAdminId(request);
        return ApiResponse.ok(service.listOnlineUsers(query));
    }

    private Long requireCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null && authorization.trim().length() > 0) {
            return AuthenticatedUserContext.requireUserId(request);
        }
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.trim().length() == 0) {
            throw new BusinessException(401, "Missing authenticated identity");
        }
        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException exception) {
            throw new BusinessException(401, "Invalid authenticated identity");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && forwardedFor.trim().length() > 0) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex >= 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor.trim();
        }
        return request.getRemoteAddr();
    }
}
