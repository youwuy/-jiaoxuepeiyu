package com.qizhifu.jiaoxuepeiyu.student.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.student.message.model.StudentMessage;
import com.qizhifu.jiaoxuepeiyu.student.message.port.StudentMessageRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudentMessageServiceTests {

    @Test
    void calculatesUnreadCountAndReturnsNewestFirst() {
        StudentMessageService service = new StudentMessageService(new FakeMessages());

        StudentMessageSummary summary = service.listMessages(7L);

        assertEquals(1, summary.getUnreadCount());
        assertEquals("New Training", summary.getMessages().get(0).getTitle());
    }

    @Test
    void marksSingleAndAllMessagesAsReadForCurrentStudent() {
        FakeMessages repository = new FakeMessages();
        StudentMessageService service = new StudentMessageService(repository);

        service.markRead(7L, 11L);
        service.markAllRead(7L);

        assertEquals(11L, repository.markedMessageId.longValue());
        assertEquals(7L, repository.markedAllStudentId.longValue());
    }

    private static class FakeMessages implements StudentMessageRepository {
        private Long markedMessageId;
        private Long markedAllStudentId;

        @Override
        public List<StudentMessage> findMessages(Long studentId) {
            return Arrays.asList(
                    message(10L, "New Training", false),
                    message(11L, "Read Course", true));
        }

        @Override
        public void markRead(Long studentId, Long messageId) {
            this.markedMessageId = messageId;
        }

        @Override
        public void markAllRead(Long studentId) {
            this.markedAllStudentId = studentId;
        }

        private StudentMessage message(Long id, String title, boolean read) {
            StudentMessage message = new StudentMessage();
            message.setId(id);
            message.setTitle(title);
            message.setRead(read);
            return message;
        }
    }
}
