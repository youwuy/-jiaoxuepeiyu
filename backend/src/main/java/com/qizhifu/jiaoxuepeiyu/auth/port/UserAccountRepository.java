package com.qizhifu.jiaoxuepeiyu.auth.port;

import com.qizhifu.jiaoxuepeiyu.auth.model.LoginIdentityType;
import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findByIdentity(LoginIdentityType identityType, String account);
}
