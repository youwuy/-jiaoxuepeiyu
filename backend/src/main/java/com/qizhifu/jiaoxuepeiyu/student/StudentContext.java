package com.qizhifu.jiaoxuepeiyu.student;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import javax.servlet.http.HttpServletRequest;

public final class StudentContext {

    private static final String USER_ID_HEADER = "X-User-Id";

    private StudentContext() {
    }

    public static Long requireStudentId(HttpServletRequest request) {
        String value = request.getHeader(USER_ID_HEADER);
        if (value == null || value.trim().length() == 0) {
            throw new BusinessException(401, "Missing student identity");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(401, "Invalid student identity");
        }
    }
}
