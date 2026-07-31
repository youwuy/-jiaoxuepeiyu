package com.qizhifu.jiaoxuepeiyu.auth.repository;

import com.qizhifu.jiaoxuepeiyu.auth.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAccountMapper {

    @Select("SELECT id, username, phone, real_name, user_type, status, password_hash "
            + "FROM sys_user WHERE username = #{username} LIMIT 1")
    UserAccount findByUsername(@Param("username") String username);

    @Select("SELECT id, username, phone, real_name, user_type, status, password_hash "
            + "FROM sys_user WHERE phone = #{phone} LIMIT 1")
    UserAccount findByPhone(@Param("phone") String phone);

    @Select("SELECT id, username, phone, real_name, user_type, status, password_hash "
            + "FROM sys_user WHERE id = #{userId} LIMIT 1")
    UserAccount findById(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET password_hash = #{passwordHash}, updated_at = NOW() WHERE id = #{userId}")
    void updatePasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
}
