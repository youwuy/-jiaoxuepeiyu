package com.qizhifu.jiaoxuepeiyu.student.message;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import java.util.Collections;
import java.util.List;

public class StudentMessageSummary {

    private int unreadCount;
    private List<StudentMessage> messages;

    public StudentMessageSummary() {
    }

    public StudentMessageSummary(int unreadCount, List<StudentMessage> messages) {
        this.unreadCount = unreadCount;
        this.messages = messages == null ? Collections.<StudentMessage>emptyList() : messages;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<StudentMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<StudentMessage> messages) {
        this.messages = messages;
    }
}
