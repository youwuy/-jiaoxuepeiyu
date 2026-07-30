package com.qizhifu.jiaoxuepeiyu.bootstrap.repository;

import com.qizhifu.jiaoxuepeiyu.bootstrap.BootstrapAdminRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisBootstrapAdminRepository implements BootstrapAdminRepository {

    private final BootstrapAdminMapper mapper;

    public MyBatisBootstrapAdminRepository(BootstrapAdminMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean hasAnyAdmin() {
        return mapper.countAdmins() > 0;
    }

    @Override
    public boolean usernameExists(String username) {
        return mapper.countByUsername(username) > 0;
    }

    @Override
    public void createAdmin(String username, String realName, String phone, String passwordHash) {
        mapper.insertAdmin(username, realName, phone, passwordHash);
    }
}
