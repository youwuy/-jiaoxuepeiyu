package com.qizhifu.jiaoxuepeiyu.admin.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewItem;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.port.AdminAssignmentReviewRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminAssignmentReviewServiceTests {

    @Test
    void reviewsSubmittedAttemptAndPersistsScores() {
        FakeReviews repository = new FakeReviews();
        repository.attempt = attempt("SUBMITTED", 100);
        AdminAssignmentReviewService service = new AdminAssignmentReviewService(repository);

        service.reviewAttempt(51L, reviewCommand(), 9L);

        assertEquals(90, repository.reviewedScore.intValue());
        assertEquals("REVIEWED", repository.reviewedStatus);
        assertEquals(51L, repository.refreshedAttemptId.longValue());
        assertEquals("REVIEW", repository.lastLogAction);
    }

    @Test
    void rejectsReviewingUnsubmittedAttempt() {
        FakeReviews repository = new FakeReviews();
        repository.attempt = attempt("SAVED", 100);
        AdminAssignmentReviewService service = new AdminAssignmentReviewService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.reviewAttempt(51L, reviewCommand(), 9L);
        });

        assertEquals("Only submitted assignments can be reviewed", exception.getMessage());
    }

    @Test
    void rejectsScoreAboveAssignmentTotal() {
        FakeReviews repository = new FakeReviews();
        repository.attempt = attempt("SUBMITTED", 80);
        AdminAssignmentReviewService service = new AdminAssignmentReviewService(repository);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.reviewAttempt(51L, reviewCommand(), 9L);
        });

        assertEquals("Reviewed score cannot exceed assignment total score", exception.getMessage());
    }

    @Test
    void rejectsNegativeQuestionScore() {
        FakeReviews repository = new FakeReviews();
        repository.attempt = attempt("SUBMITTED", 100);
        AdminAssignmentReviewService service = new AdminAssignmentReviewService(repository);
        AdminAssignmentReviewCommand command = reviewCommand();
        command.getAnswers().get(0).setScore(-1);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            service.reviewAttempt(51L, command, 9L);
        });

        assertEquals("Review answer score must be between 0 and question score", exception.getMessage());
    }

    @Test
    void returnsPendingAttemptsForReviewList() {
        FakeReviews repository = new FakeReviews();
        repository.attempts = Arrays.asList(attempt("SUBMITTED", 100));
        AdminAssignmentReviewService service = new AdminAssignmentReviewService(repository);

        assertEquals(1, service.listAttempts(new AdminAssignmentAttemptQuery()).getRecords().size());
    }

    private AdminAssignmentAttempt attempt(String status, int totalScore) {
        AdminAssignmentAttempt attempt = new AdminAssignmentAttempt();
        attempt.setAttemptId(51L);
        attempt.setAssignmentId(21L);
        attempt.setCourseId(31L);
        attempt.setStudentId(41L);
        attempt.setStatus(status);
        attempt.setTotalScore(totalScore);
        AdminAssignmentAttempt.Answer answer1 = new AdminAssignmentAttempt.Answer();
        answer1.setQuestionId(1L);
        answer1.setQuestionScore(50);
        AdminAssignmentAttempt.Answer answer2 = new AdminAssignmentAttempt.Answer();
        answer2.setQuestionId(2L);
        answer2.setQuestionScore(50);
        attempt.setAnswers(Arrays.asList(answer1, answer2));
        return attempt;
    }

    private AdminAssignmentReviewCommand reviewCommand() {
        AdminAssignmentReviewCommand command = new AdminAssignmentReviewCommand();
        command.setReviewComment("Good");
        command.setAnswers(Arrays.asList(reviewItem(1L, 40), reviewItem(2L, 50)));
        return command;
    }

    private AdminAssignmentReviewItem reviewItem(Long questionId, int score) {
        AdminAssignmentReviewItem item = new AdminAssignmentReviewItem();
        item.setQuestionId(questionId);
        item.setScore(score);
        item.setComment("OK");
        return item;
    }

    private static class FakeReviews implements AdminAssignmentReviewRepository {
        private AdminAssignmentAttempt attempt;
        private List<AdminAssignmentAttempt> attempts = new ArrayList<AdminAssignmentAttempt>();
        private Integer reviewedScore;
        private String reviewedStatus;
        private Long refreshedAttemptId;
        private String lastLogAction;

        @Override
        public List<AdminAssignmentAttempt> findAttempts(AdminAssignmentAttemptQuery query) {
            return attempts;
        }

        @Override
        public long countAttempts(AdminAssignmentAttemptQuery query) {
            return attempts.size();
        }

        @Override
        public AdminAssignmentAttempt findAttempt(Long attemptId) {
            return attempt;
        }

        @Override
        public void updateAnswerScore(Long attemptId, Long questionId, Integer score, String comment) {
        }

        @Override
        public void markReviewed(Long attemptId, Integer score, String reviewComment, Long reviewerId) {
            this.reviewedScore = score;
            this.reviewedStatus = "REVIEWED";
        }

        @Override
        public void refreshCourseProgress(Long attemptId) {
            this.refreshedAttemptId = attemptId;
        }

        @Override
        public void appendReviewLog(Long attemptId, Long reviewerId, String action, String content) {
            this.lastLogAction = action;
        }
    }
}
