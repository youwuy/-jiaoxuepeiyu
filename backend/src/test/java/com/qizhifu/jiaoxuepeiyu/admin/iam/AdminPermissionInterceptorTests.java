package com.qizhifu.jiaoxuepeiyu.admin.iam;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminUserAccess;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminPermissionInterceptorTests {

    @Test
    void publicApplicationReviewUsesUpdatePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("resource:public-apply:update");
        MockHttpServletRequest request = request("POST", "/api/admin/public-applications/7/approve");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void paperPublishUsesEnablePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("resource:theory-paper:enable");
        MockHttpServletRequest request = request("POST", "/api/admin/papers/7/publish");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void paperCancelPublishUsesDisablePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("resource:theory-paper:disable");
        MockHttpServletRequest request = request("POST", "/api/admin/papers/7/cancel-publish");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void paperPublishRejectsCreateOnlyPermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("resource:theory-paper:create");
        MockHttpServletRequest request = request("POST", "/api/admin/papers/7/publish");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(403, exception.getCode());
    }

    @Test
    void organizationTreeUsesSingularControllerPathAndListPermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("system:org:list");
        MockHttpServletRequest request = request("GET", "/api/admin/org/tree");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void organizationCreateRejectsListOnlyPermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("system:org:list");
        MockHttpServletRequest request = request("POST", "/api/admin/org");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(403, exception.getCode());
    }

    @Test
    void courseUpdateUsesTeachingCourseUpdatePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("teaching:course:update");
        MockHttpServletRequest request = request("PUT", "/api/admin/courses/7");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void assignmentReviewUsesTeachingCourseUpdatePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("teaching:course:update");
        MockHttpServletRequest request = request("POST", "/api/admin/assignment-attempts/12/review");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void trainingExamStartUsesTeachingTrainingEnablePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("teaching:training:enable");
        MockHttpServletRequest request = request("POST", "/api/admin/trainings/7/start-exam");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void trainingReviewUsesTeachingTrainingUpdatePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("teaching:training:update");
        MockHttpServletRequest request = request("POST", "/api/admin/trainings/7/attempts/8/review");

        assertDoesNotThrow(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void scoreArchiveAndDeviceQueriesUseTheirOwnListPermissions() {
        assertDoesNotThrow(() -> interceptorWith("score:semester:list").preHandle(
                request("GET", "/api/admin/scores/semester"), new MockHttpServletResponse(), new Object()));
        assertDoesNotThrow(() -> interceptorWith("score:archive:list").preHandle(
                request("GET", "/api/admin/archives"), new MockHttpServletResponse(), new Object()));
        assertDoesNotThrow(() -> interceptorWith("score:device:list").preHandle(
                request("GET", "/api/admin/devices/efficiency"), new MockHttpServletResponse(), new Object()));
    }

    @Test
    void semesterScoreQueryRejectsArchivePermission() {
        AdminPermissionInterceptor interceptor = interceptorWith("score:archive:list");
        MockHttpServletRequest request = request("GET", "/api/admin/scores/semester");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals(403, exception.getCode());
    }

    private AdminPermissionInterceptor interceptorWith(String... permissionCodes) {
        AdminIamService service = mock(AdminIamService.class);
        AdminUserAccess access = new AdminUserAccess();
        access.setPermissionCodes(Arrays.asList(permissionCodes));
        when(service.getUserAccess(9L)).thenReturn(access);
        return new AdminPermissionInterceptor(service);
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        request.addHeader("X-User-Id", "9");
        return request;
    }
}
