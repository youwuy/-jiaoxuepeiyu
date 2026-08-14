package com.qizhifu.jiaoxuepeiyu.admin.iam;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminUserAccess;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private final AdminIamService service;

    public AdminPermissionInterceptor(AdminIamService service) {
        this.service = service;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        List<String> codes = requiredPermissions(request);
        if (codes.isEmpty()) {
            return true;
        }
        AdminUserAccess access = service.getUserAccess(AdminContext.requireAdminId(request));
        if (access.isUnrestricted()) {
            return true;
        }
        Set<String> granted = new HashSet<String>(access.getPermissionCodes());
        for (String code : codes) {
            if (granted.contains(code)) {
                if ("GET".equals(request.getMethod()) && code.startsWith("system:user")) {
                    AdminDataScopeContext.set(request,
                            service.getUserDataScope(AdminContext.requireAdminId(request), "system:user"));
                }
                return true;
            }
        }
        throw new BusinessException(403, "You do not have permission to perform this operation");
    }

    private List<String> requiredPermissions(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.endsWith("/permissions/mine") || path.endsWith("/permissions/mine/tree")) {
            return new ArrayList<String>();
        }
        if ("GET".equals(request.getMethod()) && path.endsWith("/permissions/tree")) {
            return Arrays.asList("system:permission:list", "system:permission", "role:list");
        }
        List<String> pages = pagesFor(path);
        if (pages.isEmpty()) return pages;
        if ("GET".equals(request.getMethod())) return withAction(pages, "list", true);
        if (path.endsWith("/approve") || path.endsWith("/reject")
                || path.endsWith("/review") || path.endsWith("/dissolve")) {
            return withAction(pages, "update", false);
        }
        if (path.endsWith("/publish") || path.endsWith("/start-exam")) {
            return withAction(pages, "enable", false);
        }
        if (path.endsWith("/cancel-publish")) return withAction(pages, "disable", false);
        if (path.endsWith("/enable") || path.endsWith("/current")) return withAction(pages, "enable", false);
        if (path.endsWith("/disable")) return withAction(pages, "disable", false);
        if (path.endsWith("/delete") || "DELETE".equals(request.getMethod())) {
            return roleCompatible(path, withAction(pages, "delete", false), "role:delete");
        }
        if ("PUT".equals(request.getMethod())) return roleCompatible(path, withAction(pages, "update", false), "role:update");
        return roleCompatible(path, withAction(pages, "create", false), "role:create");
    }

    private List<String> pagesFor(String path) {
        if (path.startsWith("/api/admin/orgs")) return Arrays.asList("system:org");
        if (path.startsWith("/api/admin/accounts")) return Arrays.asList("system:user");
        if (path.startsWith("/api/admin/permissions")) return Arrays.asList("system:permission");
        if (path.startsWith("/api/admin/roles")) return Arrays.asList("role:list");
        if (path.startsWith("/api/admin/academic-years") || path.startsWith("/api/admin/semesters")) {
            return Arrays.asList("config:term", "system:settings");
        }
        if (path.startsWith("/api/admin/majors")) return Arrays.asList("config:major", "system:settings");
        if (path.startsWith("/api/admin/classes")) return Arrays.asList("config:class", "system:settings");
        if (path.startsWith("/api/admin/classrooms")) return Arrays.asList("config:classroom", "system:settings");
        if (path.startsWith("/api/admin/score-weights")) return Arrays.asList("config:score-weight", "system:settings");
        if (path.startsWith("/api/admin/score-grade-rules")) return Arrays.asList("config:score-grade", "system:settings");
        if (path.startsWith("/api/admin/public-applications")) return Arrays.asList("resource:public-apply");
        if (path.startsWith("/api/admin/public-resources")) return Arrays.asList("resource:public-library");
        if (path.startsWith("/api/admin/resources")) return Arrays.asList("resource:personal");
        if (path.startsWith("/api/admin/questions")) {
            return Arrays.asList("resource:theory-question", "exam:question-bank");
        }
        if (path.startsWith("/api/admin/papers")) {
            return Arrays.asList("resource:theory-paper", "exam:paper");
        }
        if (path.startsWith("/api/admin/courses") || path.startsWith("/api/admin/assignment-attempts")) {
            return Arrays.asList("teaching:course");
        }
        if (path.startsWith("/api/admin/trainings") || path.startsWith("/api/admin/training-topics")) {
            return Arrays.asList("teaching:training");
        }
        if (path.startsWith("/api/admin/scores/semester")) return Arrays.asList("score:semester");
        if (path.startsWith("/api/admin/archives")) return Arrays.asList("score:archive");
        if (path.startsWith("/api/admin/devices/efficiency")) return Arrays.asList("score:device");
        return new ArrayList<String>();
    }

    private List<String> withAction(List<String> pages, String action, boolean includePage) {
        List<String> result = new ArrayList<String>();
        for (String page : pages) {
            result.add(page + ":" + action);
            if (includePage) result.add(page);
        }
        return result;
    }

    private List<String> roleCompatible(String path, List<String> permissions, String legacyCode) {
        if (path.startsWith("/api/admin/roles")) permissions.add(legacyCode);
        return permissions;
    }
}
