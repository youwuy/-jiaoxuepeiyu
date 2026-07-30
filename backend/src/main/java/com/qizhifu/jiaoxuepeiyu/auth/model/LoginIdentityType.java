package com.qizhifu.jiaoxuepeiyu.auth.model;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;

public enum LoginIdentityType {
    USERNAME,
    PHONE;

    public static LoginIdentityType fromValue(String value) {
        if ("phone".equalsIgnoreCase(value)) {
            return PHONE;
        }
        if ("username".equalsIgnoreCase(value)
                || "employeeNo".equalsIgnoreCase(value)
                || "studentNo".equalsIgnoreCase(value)) {
            return USERNAME;
        }
        throw new BusinessException(400, "Unsupported login type");
    }
}
