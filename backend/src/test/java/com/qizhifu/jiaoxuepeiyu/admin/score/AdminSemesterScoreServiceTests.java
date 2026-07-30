package com.qizhifu.jiaoxuepeiyu.admin.score;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.score.port.AdminSemesterScoreRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminSemesterScoreServiceTests {

    @Test
    void listsScoresWithPagingAndComputedComprehensiveScore() {
        FakeScores repository = new FakeScores();
        repository.scores = Arrays.asList(score(null));
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);

        AdminSemesterScore score = service.listScores(new AdminSemesterScoreQuery()).getRecords().get(0);

        assertEquals(1, repository.lastQuery.getPage());
        assertEquals(20, repository.lastQuery.getPageSize());
        assertEquals(new BigDecimal("86.4"), score.getComprehensiveScore());
    }

    @Test
    void returnsStatisticsWithDefaultNumbers() {
        FakeScores repository = new FakeScores();
        repository.statistics = null;
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);

        AdminSemesterScoreStatistics statistics = service.getStatistics(new AdminSemesterScoreQuery());

        assertEquals(0, statistics.getStudentCount().intValue());
        assertEquals(0, statistics.getAverageScore().doubleValue(), 0.001);
    }

    private AdminSemesterScore score(BigDecimal comprehensiveScore) {
        AdminSemesterScore score = new AdminSemesterScore();
        score.setScoreId(1L);
        score.setStudentId(2L);
        score.setStudentName("Student One");
        score.setSemesterId(3L);
        score.setCoursewareLearningScore(new BigDecimal("80"));
        score.setTrainingPracticeScore(new BigDecimal("90"));
        score.setCourseAssignmentScore(new BigDecimal("85"));
        score.setExamScore(new BigDecimal("88"));
        score.setCoursewareWeight(20);
        score.setTrainingPracticeWeight(30);
        score.setAssignmentWeight(20);
        score.setExamWeight(30);
        score.setComprehensiveScore(comprehensiveScore);
        return score;
    }

    private static class FakeScores implements AdminSemesterScoreRepository {
        private AdminSemesterScoreQuery lastQuery;
        private List<AdminSemesterScore> scores = new ArrayList<AdminSemesterScore>();
        private AdminSemesterScoreStatistics statistics;

        @Override
        public List<AdminSemesterScore> findScores(AdminSemesterScoreQuery query) {
            this.lastQuery = query;
            return scores;
        }

        @Override
        public long countScores(AdminSemesterScoreQuery query) {
            return scores.size();
        }

        @Override
        public AdminSemesterScoreStatistics calculateStatistics(AdminSemesterScoreQuery query) {
            this.lastQuery = query;
            return statistics;
        }

        @Override
        public List<AdminSemesterScore> findRanking(AdminSemesterScoreQuery query) {
            this.lastQuery = query;
            return scores;
        }
    }
}
