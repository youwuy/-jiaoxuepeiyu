package com.qizhifu.jiaoxuepeiyu.student.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.student.score.model.StudentSemesterScore;
import com.qizhifu.jiaoxuepeiyu.student.score.port.StudentScoreRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class StudentScoreServiceTests {

    @Test
    void returnsSemesterScoresWithCalculatedComprehensiveScore() {
        StudentScoreService service = new StudentScoreService(new FakeScores());

        List<StudentSemesterScore> scores = service.listSemesterScores(7L);

        assertEquals(2, scores.size());
        assertEquals("2025-2026 SECOND", scores.get(0).getAcademicTerm());
        assertEquals(new BigDecimal("88.0"), scores.get(0).getComprehensiveScore());
        assertEquals(new BigDecimal("91.5"), scores.get(1).getComprehensiveScore());
    }

    private static class FakeScores implements StudentScoreRepository {
        @Override
        public List<StudentSemesterScore> findSemesterScores(Long studentId) {
            return Arrays.asList(
                    score("2025-2026 SECOND", "80", "90", "70", "100", null),
                    score("2025-2026 FIRST", "90", "90", "90", "90", "91.5"));
        }

        private StudentSemesterScore score(String term,
                                           String courseware,
                                           String training,
                                           String assignment,
                                           String exam,
                                           String storedComprehensive) {
            StudentSemesterScore score = new StudentSemesterScore();
            score.setAcademicTerm(term);
            score.setCoursewareLearningScore(new BigDecimal(courseware));
            score.setTrainingPracticeScore(new BigDecimal(training));
            score.setCourseAssignmentScore(new BigDecimal(assignment));
            score.setExamScore(new BigDecimal(exam));
            score.setCoursewareWeight(20);
            score.setTrainingPracticeWeight(30);
            score.setAssignmentWeight(20);
            score.setExamWeight(30);
            if (storedComprehensive != null) {
                score.setComprehensiveScore(new BigDecimal(storedComprehensive));
            }
            return score;
        }
    }
}
