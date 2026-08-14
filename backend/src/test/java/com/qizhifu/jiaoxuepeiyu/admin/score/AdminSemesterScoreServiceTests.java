package com.qizhifu.jiaoxuepeiyu.admin.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.score.port.AdminSemesterScoreRepository;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    void normalizesStudentNameAsFuzzyAndStudentNumberAsExact() {
        FakeScores repository = new FakeScores();
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);
        AdminSemesterScoreQuery query = new AdminSemesterScoreQuery();
        query.setStudentName(" 张三 ");
        query.setStudentNo(" S001 ");

        service.listScores(query);

        assertEquals("%张三%", repository.lastQuery.getStudentName());
        assertEquals("S001", repository.lastQuery.getStudentNo());
    }

    @Test
    void exportsScoresFromFilteredScoreRowsNotRanking() {
        FakeScores repository = new FakeScores();
        repository.scores = Arrays.asList(score(null));
        AdminSemesterScoreQuery query = new AdminSemesterScoreQuery();
        query.setPage(3);
        query.setPageSize(10);
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);

        List<AdminSemesterScore> scores = service.exportScores(query);

        assertEquals(1, scores.size());
        assertEquals(1, repository.findScoresCalls);
        assertEquals(0, repository.findRankingCalls);
        assertEquals(1, repository.lastQuery.getPage());
        assertEquals(100, repository.lastQuery.getPageSize());
        assertEquals(new BigDecimal("86.4"), scores.get(0).getComprehensiveScore());
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

    @Test
    void previewsImportRowsWithMissingStudentAndInvalidScores() {
        FakeScores repository = new FakeScores();
        repository.studentIds.put("student001", 2L);
        repository.studentIds.put("student002", 4L);
        repository.existingSemesterIds.add(3L);
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);
        AdminSemesterScoreImportCommand command = new AdminSemesterScoreImportCommand();
        command.setRows(Arrays.asList(importRow(1, "student001", 3L, "80", "90", "85", "88", 20, 30, 20, 30),
                importRow(2, "missing001", 3L, "80", "90", "85", "88", 20, 30, 20, 30),
                importRow(3, "student002", 3L, "101", "90", "85", "88", 20, 30, 20, 30)));

        AdminSemesterScoreImportPreview preview = service.previewImport(command);

        assertEquals(1, preview.getValidCount().intValue());
        assertEquals(2, preview.getErrorCount().intValue());
        assertEquals("Student does not exist", preview.getRows().get(1).getErrors().get(0));
        assertEquals("Courseware learning score must be between 0 and 100", preview.getRows().get(2).getErrors().get(0));
        assertEquals(new BigDecimal("86.4"), preview.getRows().get(0).getComprehensiveScore());
    }

    @Test
    void rejectsDuplicateStudentSemesterRowsInImportPreview() {
        FakeScores repository = new FakeScores();
        repository.studentIds.put("student001", 2L);
        repository.existingSemesterIds.add(3L);
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);
        AdminSemesterScoreImportCommand command = new AdminSemesterScoreImportCommand();
        command.setRows(Arrays.asList(importRow(1, "student001", 3L, "80", "90", "85", "88", 20, 30, 20, 30),
                importRow(2, "student001", 3L, "82", "90", "85", "88", 20, 30, 20, 30)));

        AdminSemesterScoreImportPreview preview = service.previewImport(command);

        assertEquals(0, preview.getValidCount().intValue());
        assertEquals(2, preview.getErrorCount().intValue());
        assertEquals("Student semester row is duplicated in import rows", preview.getRows().get(0).getErrors().get(0));
    }

    @Test
    void importsValidScoreRowsWithCalculatedComprehensiveScore() {
        FakeScores repository = new FakeScores();
        repository.studentIds.put("student001", 2L);
        repository.studentIds.put("student002", 4L);
        repository.existingSemesterIds.add(3L);
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);
        AdminSemesterScoreImportCommand command = new AdminSemesterScoreImportCommand();
        command.setRows(Arrays.asList(importRow(1, "student001", 3L, "80", "90", "85", "88", 20, 30, 20, 30),
                importRow(2, "student002", 3L, "70", "80", "90", "100", 20, 30, 20, 30)));

        AdminSemesterScoreImportResult result = service.importScores(command);

        assertEquals(2, result.getImportedCount().intValue());
        assertEquals(2, repository.upsertedRows.size());
        assertEquals(2L, repository.upsertedRows.get(0).getStudentId().longValue());
        assertEquals(new BigDecimal("86.4"), repository.upsertedRows.get(0).getComprehensiveScore());
    }

    @Test
    void rejectsImportWhenPreviewHasErrors() {
        FakeScores repository = new FakeScores();
        repository.existingSemesterIds.add(3L);
        AdminSemesterScoreService service = new AdminSemesterScoreService(repository);
        AdminSemesterScoreImportCommand command = new AdminSemesterScoreImportCommand();
        command.setRows(Arrays.asList(importRow(1, "missing001", 3L, "80", "90", "85", "88", 20, 30, 20, 30)));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.importScores(command));

        assertEquals("Import rows contain errors", exception.getMessage());
        assertEquals(0, repository.upsertedRows.size());
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

    private AdminSemesterScoreImportRow importRow(int rowNo,
                                                  String studentNo,
                                                  Long semesterId,
                                                  String coursewareLearningScore,
                                                  String trainingPracticeScore,
                                                  String courseAssignmentScore,
                                                  String examScore,
                                                  Integer coursewareWeight,
                                                  Integer trainingPracticeWeight,
                                                  Integer assignmentWeight,
                                                  Integer examWeight) {
        AdminSemesterScoreImportRow row = new AdminSemesterScoreImportRow();
        row.setRowNo(Integer.valueOf(rowNo));
        row.setStudentNo(studentNo);
        row.setSemesterId(semesterId);
        row.setCoursewareLearningScore(new BigDecimal(coursewareLearningScore));
        row.setTrainingPracticeScore(new BigDecimal(trainingPracticeScore));
        row.setCourseAssignmentScore(new BigDecimal(courseAssignmentScore));
        row.setExamScore(new BigDecimal(examScore));
        row.setCoursewareWeight(coursewareWeight);
        row.setTrainingPracticeWeight(trainingPracticeWeight);
        row.setAssignmentWeight(assignmentWeight);
        row.setExamWeight(examWeight);
        return row;
    }

    private static class FakeScores implements AdminSemesterScoreRepository {
        private AdminSemesterScoreQuery lastQuery;
        private List<AdminSemesterScore> scores = new ArrayList<AdminSemesterScore>();
        private AdminSemesterScoreStatistics statistics;
        private final Map<String, Long> studentIds = new HashMap<String, Long>();
        private final List<Long> existingSemesterIds = new ArrayList<Long>();
        private final List<AdminSemesterScoreImportRow> upsertedRows = new ArrayList<AdminSemesterScoreImportRow>();
        private int findScoresCalls;
        private int findRankingCalls;

        @Override
        public List<AdminSemesterScore> findScores(AdminSemesterScoreQuery query) {
            this.lastQuery = query;
            this.findScoresCalls++;
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
            this.findRankingCalls++;
            return scores;
        }

        @Override
        public Long findStudentIdByStudentNo(String studentNo) {
            return studentIds.get(studentNo);
        }

        @Override
        public boolean semesterExists(Long semesterId) {
            return existingSemesterIds.contains(semesterId);
        }

        @Override
        public void upsertScores(List<AdminSemesterScoreImportRow> rows) {
            upsertedRows.clear();
            upsertedRows.addAll(rows);
        }
    }
}
