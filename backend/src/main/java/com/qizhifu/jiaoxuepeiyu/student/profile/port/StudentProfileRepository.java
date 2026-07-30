package com.qizhifu.jiaoxuepeiyu.student.profile.port;

import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import java.util.Optional;

public interface StudentProfileRepository {

    Optional<StudentProfile> findByStudentId(Long studentId);

    void updatePhone(Long studentId, String phone);

    void updateIdCard(Long studentId, String idCard);

    void updatePasswordHash(Long studentId, String passwordHash);
}
