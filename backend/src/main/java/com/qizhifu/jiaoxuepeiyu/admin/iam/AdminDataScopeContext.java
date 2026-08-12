package com.qizhifu.jiaoxuepeiyu.admin.iam;

import com.qizhifu.jiaoxuepeiyu.admin.iam.model.AdminDataScopeAccess;
import javax.servlet.http.HttpServletRequest;

public final class AdminDataScopeContext {

    public static final String REQUEST_ATTRIBUTE = AdminDataScopeContext.class.getName() + ".ACCESS";

    private AdminDataScopeContext() {
    }

    public static void set(HttpServletRequest request, AdminDataScopeAccess access) {
        request.setAttribute(REQUEST_ATTRIBUTE, access);
    }

    public static AdminDataScopeAccess get(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        return value instanceof AdminDataScopeAccess ? (AdminDataScopeAccess) value : null;
    }
}
