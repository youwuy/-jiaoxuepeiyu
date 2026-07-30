package com.qizhifu.jiaoxuepeiyu.admin.profile.port;

import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import java.util.Optional;

public interface AdminProfileRepository {

    Optional<AdminProfile> findByUserId(Long userId);

    void updatePhone(Long userId, String phone);

    void updateIdCard(Long userId, String idCard);
}
