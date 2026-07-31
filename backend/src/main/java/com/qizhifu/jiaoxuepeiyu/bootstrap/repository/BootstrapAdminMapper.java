package com.qizhifu.jiaoxuepeiyu.bootstrap.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BootstrapAdminMapper {

    @Select("SELECT COUNT(1) FROM sys_user WHERE user_type = 'admin'")
    int countAdmins();

    @Select("SELECT COUNT(1) FROM sys_user WHERE username = #{username}")
    int countByUsername(@Param("username") String username);

    @Insert("INSERT INTO sys_user (username, real_name, phone, user_type, status, password_hash, created_at, updated_at) "
            + "VALUES (#{username}, #{realName}, #{phone}, 'admin', 1, #{passwordHash}, NOW(), NOW())")
    void insertAdmin(@Param("username") String username,
                     @Param("realName") String realName,
                     @Param("phone") String phone,
                     @Param("passwordHash") String passwordHash);
}
