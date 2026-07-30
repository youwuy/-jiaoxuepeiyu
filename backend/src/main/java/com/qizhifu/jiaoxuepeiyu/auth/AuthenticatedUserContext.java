package com.qizhifu.jiaoxuepeiyu.auth;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import javax.servlet.http.HttpServletRequest;

public final class AuthenticatedUserContext {

    public static final String REQUEST_ATTRIBUTE = "authenticatedUser";
    private static final String AUTHENTICATION_ERROR_ATTRIBUTE = "authenticationError";

    private AuthenticatedUserContext() {
    }

    public static void set(HttpServletRequest request, AuthenticatedUser user) {
        request.setAttribute(REQUEST_ATTRIBUTE, user);
    }

    public static void setAuthenticationError(HttpServletRequest request, AuthenticationException exception) {
        request.setAttribute(AUTHENTICATION_ERROR_ATTRIBUTE, exception);
    }

    public static Long requireAdminId(HttpServletRequest request) {
        AuthenticatedUser user = current(request);
        if (user == null) {
            throwAuthenticationError(request);
            return null;
        }
        if (!isAdminUser(user)) {
            throw new BusinessException(401, "Account does not belong to admin portal");
        }
        return user.getId();
    }

    public static Long requireStudentId(HttpServletRequest request) {
        AuthenticatedUser user = current(request);
        if (user == null) {
            throwAuthenticationError(request);
            return null;
        }
        if (!"student".equalsIgnoreCase(user.getUserType())) {
            throw new BusinessException(401, "Account does not belong to student portal");
        }
        return user.getId();
    }

    private static AuthenticatedUser current(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        if (value instanceof AuthenticatedUser) {
            return (AuthenticatedUser) value;
        }
        return null;
    }

    private static void throwAuthenticationError(HttpServletRequest request) {
        Object value = request.getAttribute(AUTHENTICATION_ERROR_ATTRIBUTE);
        if (value instanceof AuthenticationException) {
            throw (AuthenticationException) value;
        }
    }

    private static boolean isAdminUser(AuthenticatedUser user) {
        return "admin".equalsIgnoreCase(user.getUserType()) || "teacher".equalsIgnoreCase(user.getUserType());
    }
}
