package com.qizhifu.jiaoxuepeiyu.student.message.port;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import java.util.List;

public interface StudentMessageRepository {

    List<StudentMessage> findMessages(Long studentId);

    void markRead(Long studentId, Long messageId);

    void markAllRead(Long studentId);
}
