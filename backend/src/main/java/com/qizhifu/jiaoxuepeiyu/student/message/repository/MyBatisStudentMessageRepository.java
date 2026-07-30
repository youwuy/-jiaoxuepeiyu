package com.qizhifu.jiaoxuepeiyu.student.message.repository;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import com.qizhifu.jiaoxuepeiyu.student.message.port.StudentMessageRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentMessageRepository implements StudentMessageRepository {

    private final StudentMessageMapper mapper;

    public MyBatisStudentMessageRepository(StudentMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<StudentMessage> findMessages(Long studentId) {
        return mapper.findMessages(studentId);
    }

    @Override
    public void markRead(Long studentId, Long messageId) {
        mapper.markRead(studentId, messageId);
    }

    @Override
    public void markAllRead(Long studentId) {
        mapper.markAllRead(studentId);
    }
}
