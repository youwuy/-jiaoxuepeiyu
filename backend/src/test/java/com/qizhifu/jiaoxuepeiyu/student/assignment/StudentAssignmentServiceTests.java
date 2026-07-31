package com.qizhifu.jiaoxuepeiyu.student.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentQuestionRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentDetail;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentSubmitResult;
import com.qizhifu.jiaoxuepeiyu.student.assignment.port.StudentAssignmentRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StudentAssignmentServiceTests {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-30T08:00:00Z"),
            ZoneId.of("Asia/Shanghai"));

    @Test
    void returnsAssignmentDetailWithoutStandardAnswers() {
        StudentAssignmentService service = new StudentAssignmentService(new FakeAssignments(), CLOCK);

        StudentAssignmentDetail detail = service.getAssignment(7L, 12L);

        assertEquals("Theory Homework", detail.getAssignmentTitle());
        assertEquals("SAVED", detail.getStatus());
        assertEquals(2, detail.getQuestions().size());
        assertEquals("A", detail.getQuestions().get(0).getAnswerContent());
    }

    @Test
    void savesDraftAnswersBeforeDeadline() {
        FakeAssignments repository = new FakeAssignments();
        StudentAssignmentService service = new StudentAssignmentService(repository, CLOCK);

        service.saveAnswers(7L, 12L, new AssignmentAnswerCommand(Arrays.asList(
                new AssignmentAnswerCommand.AnswerItem(1L, "A"))));

        assertEquals(7L, repository.savedStudentId.longValue());
        assertEquals(12L, repository.savedAssignmentId.longValue());
        assertEquals("A", repository.savedAnswers.get(0).getAnswerContent());
    }

    @Test
    void rejectsAnswerChangesAfterDeadline() {
        FakeAssignments repository = new FakeAssignments();
        repository.assignment.setDeadline(LocalDateTime.parse("2026-07-01T00:00:00"));
        StudentAssignmentService service = new StudentAssignmentService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.saveAnswers(7L, 12L, new AssignmentAnswerCommand(Arrays.asList(
                    new AssignmentAnswerCommand.AnswerItem(1L, "A"))));
        });

        assertEquals("Assignment deadline has passed", exception.getMessage());
    }

    @Test
    void rejectsAnswerChangesBeforeAnswerStartTime() {
        FakeAssignments repository = new FakeAssignments();
        repository.assignment.setAnswerStartTime(LocalDateTime.parse("2026-08-01T00:00:00"));
        StudentAssignmentService service = new StudentAssignmentService(repository, CLOCK);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.saveAnswers(7L, 12L, new AssignmentAnswerCommand(Arrays.asList(
                    new AssignmentAnswerCommand.AnswerItem(1L, "A"))));
        });

        assertEquals("Assignment is not open for answering", exception.getMessage());
    }

    @Test
    void submitsAssignmentAndScoresObjectiveQuestions() {
        FakeAssignments repository = new FakeAssignments();
        StudentAssignmentService service = new StudentAssignmentService(repository, CLOCK);

        StudentAssignmentSubmitResult result = service.submit(7L, 12L);

        assertEquals("SUBMITTED", result.getStatus());
        assertEquals(55L, result.getAttemptId().longValue());
        assertEquals(10, result.getAutoScore());
        assertEquals(10, repository.submittedAutoScore);
        assertEquals(1L, repository.scoredQuestions.get(0).getQuestionId().longValue());
        assertEquals(10, repository.scoredQuestions.get(0).getAwardedScore());
        assertEquals(0, repository.scoredQuestions.get(1).getAwardedScore());
    }

    private static class FakeAssignments implements StudentAssignmentRepository {
        private StudentAssignmentRecord assignment = assignment();
        private Long savedStudentId;
        private Long savedAssignmentId;
        private List<AssignmentAnswerCommand.AnswerItem> savedAnswers;
        private int submittedAutoScore;
        private List<AssignmentQuestionRecord> scoredQuestions;

        @Override
        public Optional<StudentAssignmentRecord> findVisibleAssignment(Long studentId, Long assignmentId) {
            return Optional.of(assignment);
        }

        @Override
        public List<AssignmentQuestionRecord> findQuestionsWithAnswers(Long studentId, Long assignmentId) {
            return Arrays.asList(
                    question(1L, "SINGLE", "Signal color?", "A", 10, "A"),
                    question(2L, "SHORT", "Explain safety rule", "Stay clear", 20, "Text"));
        }

        @Override
        public void saveAnswers(Long studentId,
                                Long assignmentId,
                                List<AssignmentAnswerCommand.AnswerItem> answers) {
            this.savedStudentId = studentId;
            this.savedAssignmentId = assignmentId;
            this.savedAnswers = answers;
        }

        @Override
        public Long submit(Long studentId,
                           Long assignmentId,
                           List<AssignmentQuestionRecord> scoredQuestions,
                           int autoScore,
                           LocalDateTime submittedAt) {
            this.scoredQuestions = scoredQuestions;
            this.submittedAutoScore = autoScore;
            return 55L;
        }

        private StudentAssignmentRecord assignment() {
            StudentAssignmentRecord record = new StudentAssignmentRecord();
            record.setAssignmentId(12L);
            record.setCourseId(3L);
            record.setAssignmentTitle("Theory Homework");
            record.setAssignmentType("THEORY");
            record.setDeadline(LocalDateTime.parse("2026-08-31T23:59:59"));
            record.setTotalScore(30);
            record.setStatus("SAVED");
            return record;
        }

        private AssignmentQuestionRecord question(Long id,
                                                  String type,
                                                  String title,
                                                  String standardAnswer,
                                                  int score,
                                                  String answerContent) {
            AssignmentQuestionRecord record = new AssignmentQuestionRecord();
            record.setQuestionId(id);
            record.setQuestionType(type);
            record.setTitle(title);
            record.setStandardAnswer(standardAnswer);
            record.setScore(score);
            record.setAnswerContent(answerContent);
            return record;
        }
    }
}
