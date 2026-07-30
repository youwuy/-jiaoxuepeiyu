package com.qizhifu.jiaoxuepeiyu.admin.score;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.score.port.AdminSemesterScoreRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.domain.score.ComprehensiveScoreInput;
import com.qizhifu.jiaoxuepeiyu.domain.score.ScoreCalculator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AdminSemesterScoreService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdminSemesterScoreRepository repository;

    public AdminSemesterScoreService(AdminSemesterScoreRepository repository) {
        this.repository = repository;
    }

    public PageResponse<AdminSemesterScore> listScores(AdminSemesterScoreQuery query) {
        AdminSemesterScoreQuery normalized = normalizedQuery(query);
        List<AdminSemesterScore> scores = repository.findScores(normalized);
        normalizeScores(scores);
        return new PageResponse<AdminSemesterScore>(
                scores,
                normalized.getPage(),
                normalized.getPageSize(),
                repository.countScores(normalized));
    }

    public AdminSemesterScoreStatistics getStatistics(AdminSemesterScoreQuery query) {
        AdminSemesterScoreStatistics statistics = repository.calculateStatistics(normalizedQuery(query));
        if (statistics == null) {
            statistics = new AdminSemesterScoreStatistics();
        }
        statistics.setStudentCount(defaultInteger(statistics.getStudentCount()));
        statistics.setAverageScore(defaultDouble(statistics.getAverageScore()));
        statistics.setMaxScore(defaultDouble(statistics.getMaxScore()));
        statistics.setMinScore(defaultDouble(statistics.getMinScore()));
        statistics.setExcellentCount(defaultInteger(statistics.getExcellentCount()));
        statistics.setPassCount(defaultInteger(statistics.getPassCount()));
        return statistics;
    }

    public List<AdminSemesterScore> listRanking(AdminSemesterScoreQuery query) {
        AdminSemesterScoreQuery normalized = normalizedQuery(query);
        List<AdminSemesterScore> scores = repository.findRanking(normalized);
        normalizeScores(scores);
        int rank = 1;
        for (AdminSemesterScore score : scores) {
            score.setRankNo(Integer.valueOf(rank++));
        }
        return scores;
    }

    private void normalizeScores(List<AdminSemesterScore> scores) {
        if (scores == null) {
            return;
        }
        for (AdminSemesterScore score : scores) {
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
    }

    private AdminSemesterScoreQuery normalizedQuery(AdminSemesterScoreQuery query) {
        AdminSemesterScoreQuery normalized = new AdminSemesterScoreQuery();
        if (query != null) {
            normalized.setSemesterId(query.getSemesterId());
            normalized.setClassId(query.getClassId());
            normalized.setMajorId(query.getMajorId());
            normalized.setStudentId(query.getStudentId());
            normalized.setKeyword(trimToNull(query.getKeyword()));
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

    private Integer defaultInteger(Integer value) {
        return value == null ? Integer.valueOf(0) : value;
    }

    private Double defaultDouble(Double value) {
        return value == null ? Double.valueOf(0) : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
