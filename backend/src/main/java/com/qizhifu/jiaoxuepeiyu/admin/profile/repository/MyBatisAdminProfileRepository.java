package com.qizhifu.jiaoxuepeiyu.admin.profile.repository;

import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import com.qizhifu.jiaoxuepeiyu.admin.profile.port.AdminProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisAdminProfileRepository implements AdminProfileRepository {

    private final AdminProfileMapper mapper;

    public MyBatisAdminProfileRepository(AdminProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<AdminProfile> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.findByUserId(userId));
    }

    @Override
    public void updatePhone(Long userId, String phone) {
        mapper.updatePhone(userId, phone);
    }

    @Override
    public void updateIdCard(Long userId, String idCard) {
        mapper.updateIdCard(userId, idCard);
    }
}
