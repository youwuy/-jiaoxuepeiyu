package com.qizhifu.jiaoxuepeiyu.student.assignment;

import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentAnswerCommand;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.AssignmentQuestionRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentDetail;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentQuestion;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentRecord;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentReport;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentReportAnswer;
import com.qizhifu.jiaoxuepeiyu.student.assignment.model.StudentAssignmentSubmitResult;
import com.qizhifu.jiaoxuepeiyu.student.assignment.port.StudentAssignmentRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentAssignmentService {

    private final StudentAssignmentRepository repository;
    private final Clock clock;

    @Autowired
    public StudentAssignmentService(StudentAssignmentRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    StudentAssignmentService(StudentAssignmentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public StudentAssignmentDetail getAssignment(Long studentId, Long assignmentId) {
        StudentAssignmentRecord assignment = requireVisibleAssignment(studentId, assignmentId);
        List<AssignmentQuestionRecord> questions = repository.findQuestionsWithAnswers(studentId, assignmentId);
        return toDetail(assignment, questions);
    }

    @Transactional
    public void saveAnswers(Long studentId, Long assignmentId, AssignmentAnswerCommand command) {
        StudentAssignmentRecord assignment = requireVisibleAssignment(studentId, assignmentId);
        assertEditable(assignment);
        repository.saveAnswers(studentId, assignmentId, normalizeAnswers(command));
    }

    @Transactional
    public StudentAssignmentSubmitResult submit(Long studentId, Long assignmentId) {
        StudentAssignmentRecord assignment = requireVisibleAssignment(studentId, assignmentId);
        assertEditable(assignment);
        List<AssignmentQuestionRecord> questions = repository.findQuestionsWithAnswers(studentId, assignmentId);
        int autoScore = scoreObjectiveQuestions(questions);
        LocalDateTime submittedAt = LocalDateTime.now(clock);
        Long attemptId = repository.submit(studentId, assignmentId, questions, autoScore, submittedAt);

        StudentAssignmentSubmitResult result = new StudentAssignmentSubmitResult();
        result.setAttemptId(attemptId);
        result.setStatus("SUBMITTED");
        result.setAutoScore(autoScore);
        result.setSubmittedAt(submittedAt);
        return result;
    }

    public StudentAssignmentReport getReport(Long studentId, Long assignmentId) {
        StudentAssignmentRecord assignment = requireVisibleAssignment(studentId, assignmentId);
        if (!"SUBMITTED".equals(assignment.getStatus()) && !"REVIEWED".equals(assignment.getStatus())) {
            throw new BusinessException(400, "Assignment has not been submitted");
        }
        List<AssignmentQuestionRecord> questions = repository.findQuestionsWithAnswers(studentId, assignmentId);
        StudentAssignmentReport report = new StudentAssignmentReport();
        report.setAssignmentId(assignment.getAssignmentId());
        report.setAssignmentTitle(assignment.getAssignmentTitle());
        report.setStatus(assignment.getStatus());
        report.setScore(assignment.getScore());
        report.setReviewComment(assignment.getReviewComment());
        report.setSubmittedAt(assignment.getSubmittedAt());
        List<StudentAssignmentReportAnswer> answers = new ArrayList<StudentAssignmentReportAnswer>();
        for (AssignmentQuestionRecord question : questions) {
            answers.add(toReportAnswer(question));
        }
        report.setAnswers(answers);
        return report;
    }

    @Transactional
    public void retry(Long studentId, Long assignmentId) {
        StudentAssignmentRecord assignment = requireVisibleAssignment(studentId, assignmentId);
        assertWithinAnswerWindow(assignment);
        repository.retry(studentId, assignmentId);
    }

    private StudentAssignmentRecord requireVisibleAssignment(Long studentId, Long assignmentId) {
        return repository.findVisibleAssignment(studentId, assignmentId)
                .orElseThrow(() -> new BusinessException(404, "Assignment not found"));
    }

    private void assertEditable(StudentAssignmentRecord assignment) {
        if ("SUBMITTED".equals(assignment.getStatus()) || "REVIEWED".equals(assignment.getStatus())) {
            throw new BusinessException(400, "Assignment has already been submitted");
        }
        assertWithinAnswerWindow(assignment);
    }

    private void assertWithinAnswerWindow(StudentAssignmentRecord assignment) {
        LocalDateTime deadline = assignment.getAnswerEndTime() == null
                ? assignment.getDeadline()
                : assignment.getAnswerEndTime();
        LocalDateTime now = LocalDateTime.now(clock);
        if (assignment.getAnswerStartTime() != null && now.isBefore(assignment.getAnswerStartTime())) {
            throw new BusinessException(400, "Assignment is not open for answering");
        }
        if (deadline != null && now.isAfter(deadline)) {
            throw new BusinessException(400, "Assignment deadline has passed");
        }
    }

    private List<AssignmentAnswerCommand.AnswerItem> normalizeAnswers(AssignmentAnswerCommand command) {
        if (command == null || command.getAnswers() == null) {
            return Collections.emptyList();
        }
        List<AssignmentAnswerCommand.AnswerItem> answers = new ArrayList<AssignmentAnswerCommand.AnswerItem>();
        for (AssignmentAnswerCommand.AnswerItem answer : command.getAnswers()) {
            if (answer != null && answer.getQuestionId() != null) {
                answers.add(answer);
            }
        }
        return answers;
    }

    private int scoreObjectiveQuestions(List<AssignmentQuestionRecord> questions) {
        int autoScore = 0;
        for (AssignmentQuestionRecord question : questions) {
            if (isObjective(question.getQuestionType())
                    && normalize(question.getAnswerContent()).equals(normalize(question.getStandardAnswer()))) {
                question.setAwardedScore(question.getScore());
                autoScore += question.getScore();
            } else {
                question.setAwardedScore(0);
            }
        }
        return autoScore;
    }

    private boolean isObjective(String questionType) {
        return "SINGLE".equals(questionType)
                || "MULTIPLE".equals(questionType)
                || "JUDGE".equals(questionType)
                || "FILL".equals(questionType)
                || "FILL_BLANK".equals(questionType);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private StudentAssignmentDetail toDetail(StudentAssignmentRecord assignment,
                                             List<AssignmentQuestionRecord> questionRecords) {
        StudentAssignmentDetail detail = new StudentAssignmentDetail();
        detail.setAssignmentId(assignment.getAssignmentId());
        detail.setCourseId(assignment.getCourseId());
        detail.setAssignmentTitle(assignment.getAssignmentTitle());
        detail.setAssignmentType(assignment.getAssignmentType());
        detail.setDeadline(assignment.getDeadline());
        detail.setAnswerStartTime(assignment.getAnswerStartTime());
        detail.setAnswerEndTime(assignment.getAnswerEndTime());
        detail.setCompletionRule(assignment.getCompletionRule());
        detail.setPassScore(assignment.getPassScore());
        detail.setPublishMode(assignment.getPublishMode());
        detail.setTotalScore(assignment.getTotalScore());
        detail.setStatus(assignment.getStatus());
        detail.setSubmittedAt(assignment.getSubmittedAt());
        List<StudentAssignmentQuestion> questions = new ArrayList<StudentAssignmentQuestion>();
        for (AssignmentQuestionRecord record : questionRecords) {
            questions.add(toQuestion(record));
        }
        detail.setQuestions(questions);
        return detail;
    }

    private StudentAssignmentQuestion toQuestion(AssignmentQuestionRecord record) {
        StudentAssignmentQuestion question = new StudentAssignmentQuestion();
        question.setQuestionId(record.getQuestionId());
        question.setQuestionType(record.getQuestionType());
        question.setTitle(record.getTitle());
        question.setScore(record.getScore());
        question.setOptions(record.getOptions());
        question.setAnswerContent(record.getAnswerContent());
        return question;
    }

    private StudentAssignmentReportAnswer toReportAnswer(AssignmentQuestionRecord record) {
        StudentAssignmentReportAnswer answer = new StudentAssignmentReportAnswer();
        answer.setQuestionId(record.getQuestionId());
        answer.setQuestionType(record.getQuestionType());
        answer.setTitle(record.getTitle());
        answer.setStandardAnswer(record.getStandardAnswer());
        answer.setAnswerContent(record.getAnswerContent());
        answer.setScore(record.getAwardedScore());
        return answer;
    }
}
