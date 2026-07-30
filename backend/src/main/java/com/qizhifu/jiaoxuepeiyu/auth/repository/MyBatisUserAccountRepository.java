package com.qizhifu.jiaoxuepeiyu.auth.repository;

import com.qizhifu.jiaoxuepeiyu.auth.model.LoginIdentityType;
import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import com.qizhifu.jiaoxuepeiyu.auth.port.UserAccountRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisUserAccountRepository implements UserAccountRepository {

    private final UserAccountMapper mapper;

    public MyBatisUserAccountRepository(UserAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<UserAccount> findByIdentity(LoginIdentityType identityType, String account) {
        UserAccount user;
        if (identityType == LoginIdentityType.PHONE) {
            user = mapper.findByPhone(account);
        } else {
            user = mapper.findByUsername(account);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<UserAccount> findById(Long userId) {
        return Optional.ofNullable(mapper.findById(userId));
    }

    @Override
    public void updatePasswordHash(Long userId, String passwordHash) {
        mapper.updatePasswordHash(userId, passwordHash);
    }
}
