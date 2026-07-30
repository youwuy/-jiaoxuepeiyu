package com.qizhifu.jiaoxuepeiyu.student.score;

import com.qizhifu.jiaoxuepeiyu.domain.score.ComprehensiveScoreInput;
import com.qizhifu.jiaoxuepeiyu.domain.score.ScoreCalculator;
import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import com.qizhifu.jiaoxuepeiyu.student.score.port.StudentScoreRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudentScoreService {

    private final StudentScoreRepository repository;

    public StudentScoreService(StudentScoreRepository repository) {
        this.repository = repository;
    }

    public List<StudentSemesterScore> listSemesterScores(Long studentId) {
        List<StudentSemesterScore> scores = repository.findSemesterScores(studentId);
        for (StudentSemesterScore score : scores) {
            if (score.getComprehensiveScore() == null) {
                score.setComprehensiveScore(ScoreCalculator.calculate(new ComprehensiveScoreInput(
                        score.getCoursewareLearningScore(),
                        score.getTrainingPracticeScore(),
                        score.getCourseAssignmentScore(),
                        score.getExamScore(),
                        score.getCoursewareWeight(),
                        score.getTrainingPracticeWeight(),
                        score.getAssignmentWeight(),
                        score.getExamWeight())));
            }
        }
        return scores;
    }
}
