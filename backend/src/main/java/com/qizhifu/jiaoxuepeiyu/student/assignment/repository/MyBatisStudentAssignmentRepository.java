package com.qizhifu.jiaoxuepeiyu.student.assignment.repository;

import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentQuestionRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.port.StudentAssignmentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisStudentAssignmentRepository implements StudentAssignmentRepository {

    private final StudentAssignmentMapper mapper;

    public MyBatisStudentAssignmentRepository(StudentAssignmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<StudentAssignmentRecord> findVisibleAssignment(Long studentId, Long assignmentId) {
        return Optional.ofNullable(mapper.findVisibleAssignment(studentId, assignmentId));
    }

    @Override
    public List<AssignmentQuestionRecord> findQuestionsWithAnswers(Long studentId, Long assignmentId) {
        return mapper.findQuestionsWithAnswers(studentId, assignmentId);
    }

    @Override
    public void saveAnswers(Long studentId,
                            Long assignmentId,
                            List<AssignmentAnswerCommand.AnswerItem> answers) {
        mapper.ensureAttempt(studentId, assignmentId, "SAVED");
        Long attemptId = mapper.findAttemptId(studentId, assignmentId);
        mapper.deleteAnswers(attemptId);
        if (answers != null && !answers.isEmpty()) {
            mapper.insertAnswers(attemptId, answers);
        }
    }

    @Override
    public void retry(Long studentId, Long assignmentId) {
        Long attemptId = mapper.findAttemptId(studentId, assignmentId);
        if (attemptId == null) {
            mapper.ensureAttempt(studentId, assignmentId, "SAVED");
            return;
        }
        mapper.deleteAnswers(attemptId);
        mapper.resetAttempt(attemptId);
    }

    @Override
    public Long submit(Long studentId,
                       Long assignmentId,
                       List<AssignmentQuestionRecord> scoredQuestions,
                       int autoScore,
                       LocalDateTime submittedAt) {
        mapper.ensureAttempt(studentId, assignmentId, "SAVED");
        Long attemptId = mapper.findAttemptId(studentId, assignmentId);
        for (AssignmentQuestionRecord question : scoredQuestions) {
            mapper.upsertScoredAnswer(
                    attemptId,
                    question.getQuestionId(),
                    question.getAnswerContent(),
                    question.getAwardedScore());
        }
        mapper.markSubmitted(attemptId, autoScore, submittedAt);
        mapper.refreshCourseProgressByAssignment(studentId, assignmentId);
        return attemptId;
    }
}
