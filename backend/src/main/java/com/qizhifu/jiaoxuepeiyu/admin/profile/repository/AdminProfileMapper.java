package com.qizhifu.jiaoxuepeiyu.admin.profile.repository;

import com.qizhifu.jiaoxuepeiyu.admin.profile.model.AdminProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AdminProfileMapper {

    @Select("SELECT u.id AS user_id, u.username AS account_no, u.real_name, u.user_type, "
            + "u.phone, u.id_card, o.org_name, u.job_title "
            + "FROM sys_user u LEFT JOIN sys_org o ON o.id = u.org_id "
            + "WHERE u.id = #{userId} AND u.user_type IN ('admin', 'teacher') LIMIT 1")
    AdminProfile findByUserId(@Param("userId") Long userId);

    @Update("UPDATE sys_user SET phone = #{phone}, updated_at = NOW() "
            + "WHERE id = #{userId} AND user_type IN ('admin', 'teacher')")
    void updatePhone(@Param("userId") Long userId, @Param("phone") String phone);

    @Update("UPDATE sys_user SET id_card = #{idCard}, updated_at = NOW() "
            + "WHERE id = #{userId} AND user_type IN ('admin', 'teacher')")
    void updateIdCard(@Param("userId") Long userId, @Param("idCard") String idCard);
}
