package com.qizhifu.jiaoxuepeiyu.student.assignment.port;

import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentQuestionRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudentAssignmentRepository {

    Optional<StudentAssignmentRecord> findVisibleAssignment(Long studentId, Long assignmentId);

    List<AssignmentQuestionRecord> findQuestionsWithAnswers(Long studentId, Long assignmentId);

    void saveAnswers(Long studentId, Long assignmentId, List<AssignmentAnswerCommand.AnswerItem> answers);

    Long submit(Long studentId,
                Long assignmentId,
                List<AssignmentQuestionRecord> scoredQuestions,
                int autoScore,
                LocalDateTime submittedAt);
}
