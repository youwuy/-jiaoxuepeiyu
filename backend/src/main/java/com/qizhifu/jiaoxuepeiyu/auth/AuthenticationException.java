package com.qizhifu.jiaoxuepeiyu.auth;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(401, message);
    }
}
