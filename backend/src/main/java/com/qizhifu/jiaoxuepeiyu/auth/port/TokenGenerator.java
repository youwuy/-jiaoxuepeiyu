package com.qizhifu.jiaoxuepeiyu.auth.port;

public interface TokenGenerator {

    String generate(Long userId);
}
