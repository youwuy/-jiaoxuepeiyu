package com.qizhifu.jiaoxuepeiyu.auth.security;

import com.qizhifu.jiaoxuepeiyu.auth.port.TokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureTokenGenerator implements TokenGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate(Long userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
