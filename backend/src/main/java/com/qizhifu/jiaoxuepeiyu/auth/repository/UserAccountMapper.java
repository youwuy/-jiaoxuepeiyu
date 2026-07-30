package com.qizhifu.jiaoxuepeiyu.auth.repository;

import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserAccountMapper {

    @Select("SELECT id, username, phone, real_name, user_type, status, password_hash "
            + "FROM sys_user WHERE username = #{username} LIMIT 1")
    UserAccount findByUsername(@Param("username") String username);

    @Select("SELECT id, username, phone, real_name, user_type, status, password_hash "
            + "FROM sys_user WHERE phone = #{phone} LIMIT 1")
    UserAccount findByPhone(@Param("phone") String phone);
}
