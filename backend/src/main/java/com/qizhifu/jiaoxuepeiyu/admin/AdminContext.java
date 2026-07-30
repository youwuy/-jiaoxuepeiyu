package com.qizhifu.jiaoxuepeiyu.admin;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import javax.servlet.http.HttpServletRequest;

public final class AdminContext {

    private static final String USER_ID_HEADER = "X-User-Id";

    private AdminContext() {
    }

    public static Long requireAdminId(HttpServletRequest request) {
        String value = request.getHeader(USER_ID_HEADER);
        if (value == null || value.trim().length() == 0) {
            throw new BusinessException(401, "Missing admin identity");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(401, "Invalid admin identity");
        }
    }
}
