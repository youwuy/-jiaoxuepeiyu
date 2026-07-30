package com.qizhifu.jiaoxuepeiyu.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.auth.model.AuthenticatedUser;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthenticatedUserContextTests {

    @Test
    void returnsAdminIdFromAuthenticatedUserAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthenticatedUserContext.REQUEST_ATTRIBUTE, user(9L, "teacher"));

        Long userId = AuthenticatedUserContext.requireAdminId(request);

        assertEquals(9L, userId.longValue());
    }

    @Test
    void returnsStudentIdFromAuthenticatedUserAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthenticatedUserContext.REQUEST_ATTRIBUTE, user(7L, "student"));

        Long userId = AuthenticatedUserContext.requireStudentId(request);

        assertEquals(7L, userId.longValue());
    }

    @Test
    void rejectsStudentTokenOnAdminContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthenticatedUserContext.REQUEST_ATTRIBUTE, user(7L, "student"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> AuthenticatedUserContext.requireAdminId(request));

        assertEquals("Account does not belong to admin portal", exception.getMessage());
    }

    @Test
    void rejectsInvalidTokenBeforeCompatibilityHeaderFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Id", "9");
        AuthenticatedUserContext.setAuthenticationError(request,
                new AuthenticationException("Invalid or expired token"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> AuthenticatedUserContext.requireAdminId(request));

        assertEquals("Invalid or expired token", exception.getMessage());
    }

    private AuthenticatedUser user(Long id, String userType) {
        AuthenticatedUser user = new AuthenticatedUser();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRealName("User " + id);
        user.setUserType(userType);
        return user;
    }
}
