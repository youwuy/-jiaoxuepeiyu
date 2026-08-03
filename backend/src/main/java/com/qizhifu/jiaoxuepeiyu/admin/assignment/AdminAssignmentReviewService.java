package com.qizhifu.jiaoxuepeiyu.admin.assignment;

import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttempt;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentAttemptQuery;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewCommand;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewItem;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.model.AdminAssignmentReviewLog;
import com.qizhifu.jiaoxuepeiyu.admin.assignment.port.AdminAssignmentReviewRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAssignmentReviewService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminAssignmentReviewRepository repository;

    public AdminAssignmentReviewService(AdminAssignmentReviewRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminAssignmentAttempt> listAttempts(AdminAssignmentAttemptQuery query) {
        AdminAssignmentAttemptQuery normalized = normalizedQuery(query);
        return new PageResponse<AdminAssignmentAttempt>(
                repository.findAttempts(normalized),
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countAttempts(normalized));
    }

    public AdminAssignmentAttempt getAttempt(Long attemptId) {
        AdminAssignmentAttempt attempt = repository.findAttempt(attemptId);
        if (attempt == null) {
            throw new BusinessException(404, "Assignment attempt not found");
        }
        return attempt;
    }

    @Transactional
    public void reviewAttempt(Long attemptId, AdminAssignmentReviewCommand command, Long reviewerId) {
        requireReviewer(reviewerId);
        AdminAssignmentAttempt attempt = getAttempt(attemptId);
        if (!"SUBMITTED".equals(attempt.getStatus()) && !"REVIEWED".equals(attempt.getStatus())) {
            throw new BusinessException(400, "Only submitted assignments can be reviewed");
        }
        Map<Long, AdminAssignmentAttempt.Answer> answersByQuestion = answersByQuestion(attempt.getAnswers());
        List<AdminAssignmentReviewItem> reviewItems = normalizedReviewItems(command);
        Map<Long, Integer> scoreByQuestion = currentScores(answersByQuestion);
        for (AdminAssignmentReviewItem item : reviewItems) {
            if (item == null || item.getQuestionId() == null || item.getScore() == null) {
                throw new BusinessException(400, "Review answer question and score are required");
            }
            AdminAssignmentAttempt.Answer answer = answersByQuestion.get(item.getQuestionId());
            if (answer == null) {
                throw new BusinessException(400, "Review answer question does not exist in attempt");
            }
            validateQuestionScore(item, answer);
            scoreByQuestion.put(item.getQuestionId(), item.getScore());
        }
        int totalScore = sumScores(scoreByQuestion);
        if (attempt.getTotalScore() != null && totalScore > attempt.getTotalScore().intValue()) {
            throw new BusinessException(400, "Reviewed score cannot exceed assignment total score");
        }
        for (AdminAssignmentReviewItem item : reviewItems) {
            repository.updateAnswerScore(attemptId, item.getQuestionId(), item.getScore(), trimToNull(item.getComment()));
        }
        String reviewComment = trimToNull(command == null ? null : command.getReviewComment());
        repository.markReviewed(attemptId, Integer.valueOf(totalScore), reviewComment, reviewerId);
        repository.refreshCourseProgress(attemptId);
        repository.appendReviewLog(attemptId, reviewerId, "REVIEW", "Review assignment attempt");
    }

    public List<AdminAssignmentReviewLog> listReviewLogs(Long attemptId) {
        getAttempt(attemptId);
        return repository.findReviewLogs(attemptId);
    }

    private AdminAssignmentAttemptQuery normalizedQuery(AdminAssignmentAttemptQuery query) {
        AdminAssignmentAttemptQuery normalized = new AdminAssignmentAttemptQuery();
        if (query != null) {
            normalized.setCourseId(query.getCourseId());
            normalized.setAssignmentId(query.getAssignmentId());
            normalized.setClassId(query.getClassId());
            normalized.setStudentId(query.getStudentId());
            normalized.setStatus(upper(trimToNull(query.getStatus())));
            normalized.setKeyword(likeKeyword(trimToNull(query.getKeyword())));
            normalized.setPage(query.getPage());
            normalized.setPageSize(query.getPageSize());
        }
        if (normalized.getPage() < 1) {
            normalized.setPage(1);
        }
        if (normalized.getPageSize() < 1) {
            normalized.setPageSize(20);
        }
        if (normalized.getPageSize() > MAX_PAGE_SIZE) {
            normalized.setPageSize(MAX_PAGE_SIZE);
        }
        return normalized;
    }

    private List<AdminAssignmentReviewItem> normalizedReviewItems(AdminAssignmentReviewCommand command) {
        if (command == null || command.getAnswers() == null || command.getAnswers().isEmpty()) {
            throw new BusinessException(400, "Review answers are required");
        }
        Set<Long> questionIds = new HashSet<Long>();
        for (AdminAssignmentReviewItem item : command.getAnswers()) {
            if (item == null || item.getQuestionId() == null || item.getScore() == null) {
                throw new BusinessException(400, "Review answer question and score are required");
            }
            if (!questionIds.add(item.getQuestionId())) {
                throw new BusinessException(400, "Review answer questions cannot repeat");
            }
        }
        return command.getAnswers();
    }

    private void validateQuestionScore(AdminAssignmentReviewItem item, AdminAssignmentAttempt.Answer answer) {
        int maxScore = answer.getQuestionScore() == null ? 0 : answer.getQuestionScore().intValue();
        if (item.getScore().intValue() < 0 || item.getScore().intValue() > maxScore) {
            throw new BusinessException(400, "Review answer score must be between 0 and question score");
        }
    }

    private Map<Long, AdminAssignmentAttempt.Answer> answersByQuestion(List<AdminAssignmentAttempt.Answer> answers) {
        Map<Long, AdminAssignmentAttempt.Answer> byQuestion = new HashMap<Long, AdminAssignmentAttempt.Answer>();
        if (answers == null) {
            return byQuestion;
        }
        for (AdminAssignmentAttempt.Answer answer : answers) {
            if (answer != null && answer.getQuestionId() != null) {
                byQuestion.put(answer.getQuestionId(), answer);
            }
        }
        return byQuestion;
    }

    private Map<Long, Integer> currentScores(Map<Long, AdminAssignmentAttempt.Answer> answersByQuestion) {
        Map<Long, Integer> scoreByQuestion = new HashMap<Long, Integer>();
        for (Map.Entry<Long, AdminAssignmentAttempt.Answer> entry : answersByQuestion.entrySet()) {
            Integer score = entry.getValue().getScore();
            scoreByQuestion.put(entry.getKey(), score == null ? Integer.valueOf(0) : score);
        }
        return scoreByQuestion;
    }

    private int sumScores(Map<Long, Integer> scoreByQuestion) {
        int total = 0;
        for (Integer score : scoreByQuestion.values()) {
            total += score == null ? 0 : score.intValue();
        }
        return total;
    }

    private void requireReviewer(Long reviewerId) {
        if (reviewerId == null) {
            throw new BusinessException(401, "Missing admin identity");
        }
    }

    private String trimToNull(String value) {
        return InputValidator.hasText(value) ? value.trim() : null;
    }

    private String upper(String value) {
        return value == null ? null : value.toUpperCase(Locale.ENGLISH);
    }

    private String likeKeyword(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
