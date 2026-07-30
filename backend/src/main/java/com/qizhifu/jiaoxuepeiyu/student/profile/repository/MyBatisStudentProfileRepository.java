package com.qizhifu.jiaoxuepeiyu.student.profile.repository;

import com.qizhifu.jiaoxuepeiyu.student.profile.model.StudentProfile;
import com.qizhifu.jiaoxuepeiyu.student.profile.port.StudentProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentProfileRepository implements StudentProfileRepository {

    private final StudentProfileMapper mapper;

    public MyBatisStudentProfileRepository(StudentProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<StudentProfile> findByStudentId(Long studentId) {
        return Optional.ofNullable(mapper.findByStudentId(studentId));
    }

    @Override
    public void updatePhone(Long studentId, String phone) {
        mapper.updatePhone(studentId, phone);
    }

    @Override
    public void updateIdCard(Long studentId, String idCard) {
        mapper.updateIdCard(studentId, idCard);
    }

    @Override
    public void updatePasswordHash(Long studentId, String passwordHash) {
        mapper.updatePasswordHash(studentId, passwordHash);
    }
}
