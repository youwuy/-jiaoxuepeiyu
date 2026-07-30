package com.qizhifu.jiaoxuepeiyu.student.profile.repository;

import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentProfileMapper {

    @Select("SELECT u.id AS student_id, u.username AS student_no, u.real_name, u.phone, u.id_card, "
            + "c.class_name, u.password_hash "
            + "FROM sys_user u LEFT JOIN edu_class c ON c.id = u.class_id "
            + "WHERE u.id = #{studentId} AND u.user_type = 'student' LIMIT 1")
    StudentProfile findByStudentId(@Param("studentId") Long studentId);

    @Update("UPDATE sys_user SET phone = #{phone} WHERE id = #{studentId} AND user_type = 'student'")
    void updatePhone(@Param("studentId") Long studentId, @Param("phone") String phone);

    @Update("UPDATE sys_user SET id_card = #{idCard} WHERE id = #{studentId} AND user_type = 'student'")
    void updateIdCard(@Param("studentId") Long studentId, @Param("idCard") String idCard);

    @Update("UPDATE sys_user SET password_hash = #{passwordHash} WHERE id = #{studentId} AND user_type = 'student'")
    void updatePasswordHash(@Param("studentId") Long studentId, @Param("passwordHash") String passwordHash);
}
