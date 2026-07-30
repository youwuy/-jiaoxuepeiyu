package com.qizhifu.jiaoxuepeiyu.student.message;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import com.qizhifu.jiaoxuepeiyu.student.message.port.StudentMessageRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentMessageService {

    private final StudentMessageRepository repository;

    public StudentMessageService(StudentMessageRepository repository) {
        this.repository = repository;
    }

    public StudentMessageSummary listMessages(Long studentId) {
        List<StudentMessage> messages = repository.findMessages(studentId);
        int unreadCount = 0;
        for (StudentMessage message : messages) {
            if (!message.isRead()) {
                unreadCount++;
            }
        }
        return new StudentMessageSummary(unreadCount, messages);
    }

    @Transactional
    public void markRead(Long studentId, Long messageId) {
        repository.markRead(studentId, messageId);
    }

    @Transactional
    public void markAllRead(Long studentId) {
        repository.markAllRead(studentId);
    }
}
