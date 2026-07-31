package com.qizhifu.jiaoxuepeiyu.admin.score;

import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScore;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportPreview;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreQuery;
import com.qizhifu.jiaoxuepeiyu.admin.score.model.AdminSemesterScoreStatistics;
import com.qizhifu.jiaoxuepeiyu.admin.score.port.AdminSemesterScoreRepository;
import com.qizhifu.jiaoxuepeiyu.common.api.PageResponse;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import com.qizhifu.jiaoxuepeiyu.common.validation.InputValidator;
import com.qizhifu.jiaoxuepeiyu.domain.score.ComprehensiveScoreInput;
import com.qizhifu.jiaoxuepeiyu.domain.score.ScoreCalculator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSemesterScoreService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_SCORE = new BigDecimal("100");

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

    public List<AdminSemesterScore> exportScores(AdminSemesterScoreQuery query) {
        AdminSemesterScoreQuery normalized = normalizedQuery(query);
        normalized.setPage(1);
        normalized.setPageSize(MAX_PAGE_SIZE);
        List<AdminSemesterScore> scores = repository.findScores(normalized);
        normalizeScores(scores);
        return scores;
    }

    public AdminSemesterScoreImportPreview previewImport(AdminSemesterScoreImportCommand command) {
        if (command == null || command.getRows().isEmpty()) {
            throw new BusinessException(400, "Import rows are required");
        }
        List<AdminSemesterScoreImportRow> rows = command.getRows();
        Set<String> duplicateKeys = duplicateImportKeys(rows);
        int validCount = 0;
        List<AdminSemesterScoreImportRow> previewRows = new ArrayList<AdminSemesterScoreImportRow>();
        for (int i = 0; i < rows.size(); i++) {
            AdminSemesterScoreImportRow row = copyImportRow(rows.get(i), i + 1);
            List<String> errors = validateImportRow(row);
            String importKey = importKey(row);
            if (importKey != null && duplicateKeys.contains(importKey)) {
                errors.add("Student semester row is duplicated in import rows");
            }
            row.setErrors(errors);
            row.setValid(Boolean.valueOf(errors.isEmpty()));
            if (errors.isEmpty()) {
                row.setComprehensiveScore(calculate(row));
                validCount++;
            }
            previewRows.add(row);
        }
        AdminSemesterScoreImportPreview preview = new AdminSemesterScoreImportPreview();
        preview.setTotalCount(Integer.valueOf(previewRows.size()));
        preview.setValidCount(Integer.valueOf(validCount));
        preview.setErrorCount(Integer.valueOf(previewRows.size() - validCount));
        preview.setRows(previewRows);
        return preview;
    }

    @Transactional
    public AdminSemesterScoreImportResult importScores(AdminSemesterScoreImportCommand command) {
        AdminSemesterScoreImportPreview preview = previewImport(command);
        if (preview.getErrorCount().intValue() > 0) {
            throw new BusinessException(400, "Import rows contain errors");
        }
        repository.upsertScores(preview.getRows());
        AdminSemesterScoreImportResult result = new AdminSemesterScoreImportResult();
        result.setImportedCount(preview.getValidCount());
        return result;
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

    private List<String> validateImportRow(AdminSemesterScoreImportRow row) {
        List<String> errors = new ArrayList<String>();
        if (!InputValidator.hasText(row.getStudentNo())) {
            errors.add("Student number is required");
        } else {
            Long studentId = repository.findStudentIdByStudentNo(row.getStudentNo());
            if (studentId == null) {
                errors.add("Student does not exist");
            } else {
                row.setStudentId(studentId);
            }
        }
        if (row.getSemesterId() == null) {
            errors.add("Semester is required");
        } else if (!repository.semesterExists(row.getSemesterId())) {
            errors.add("Semester does not exist");
        }
        validateScore(errors, "Courseware learning score", row.getCoursewareLearningScore());
        validateScore(errors, "Training practice score", row.getTrainingPracticeScore());
        validateScore(errors, "Course assignment score", row.getCourseAssignmentScore());
        validateScore(errors, "Exam score", row.getExamScore());
        validateWeight(errors, "Courseware weight", row.getCoursewareWeight());
        validateWeight(errors, "Training practice weight", row.getTrainingPracticeWeight());
        validateWeight(errors, "Assignment weight", row.getAssignmentWeight());
        validateWeight(errors, "Exam weight", row.getExamWeight());
        if (allWeightsPresent(row) && weightSum(row) != 100) {
            errors.add("Score weights must add up to 100");
        }
        return errors;
    }

    private void validateScore(List<String> errors, String label, BigDecimal score) {
        if (score == null) {
            errors.add(label + " is required");
            return;
        }
        if (score.compareTo(MIN_SCORE) < 0 || score.compareTo(MAX_SCORE) > 0) {
            errors.add(label + " must be between 0 and 100");
        }
    }

    private void validateWeight(List<String> errors, String label, Integer weight) {
        if (weight == null) {
            errors.add(label + " is required");
            return;
        }
        if (weight.intValue() < 0 || weight.intValue() > 100) {
            errors.add(label + " must be between 0 and 100");
        }
    }

    private boolean allWeightsPresent(AdminSemesterScoreImportRow row) {
        return row.getCoursewareWeight() != null
                && row.getTrainingPracticeWeight() != null
                && row.getAssignmentWeight() != null
                && row.getExamWeight() != null;
    }

    private int weightSum(AdminSemesterScoreImportRow row) {
        return row.getCoursewareWeight().intValue()
                + row.getTrainingPracticeWeight().intValue()
                + row.getAssignmentWeight().intValue()
                + row.getExamWeight().intValue();
    }

    private Set<String> duplicateImportKeys(List<AdminSemesterScoreImportRow> rows) {
        Set<String> seen = new HashSet<String>();
        Set<String> duplicates = new HashSet<String>();
        for (AdminSemesterScoreImportRow row : rows) {
            String key = importKey(row);
            if (key != null && !seen.add(key)) {
                duplicates.add(key);
            }
        }
        return duplicates;
    }

    private String importKey(AdminSemesterScoreImportRow row) {
        if (row == null || !InputValidator.hasText(row.getStudentNo()) || row.getSemesterId() == null) {
            return null;
        }
        return row.getStudentNo().trim() + "#" + row.getSemesterId();
    }

    private AdminSemesterScoreImportRow copyImportRow(AdminSemesterScoreImportRow source, int defaultRowNo) {
        AdminSemesterScoreImportRow row = new AdminSemesterScoreImportRow();
        if (source != null) {
            row.setRowNo(source.getRowNo() == null ? Integer.valueOf(defaultRowNo) : source.getRowNo());
            row.setStudentNo(trimToNull(source.getStudentNo()));
            row.setSemesterId(source.getSemesterId());
            row.setCoursewareLearningScore(source.getCoursewareLearningScore());
            row.setTrainingPracticeScore(source.getTrainingPracticeScore());
            row.setCourseAssignmentScore(source.getCourseAssignmentScore());
            row.setExamScore(source.getExamScore());
            row.setCoursewareWeight(source.getCoursewareWeight());
            row.setTrainingPracticeWeight(source.getTrainingPracticeWeight());
            row.setAssignmentWeight(source.getAssignmentWeight());
            row.setExamWeight(source.getExamWeight());
        } else {
            row.setRowNo(Integer.valueOf(defaultRowNo));
        }
        return row;
    }

    private BigDecimal calculate(AdminSemesterScoreImportRow row) {
        return ScoreCalculator.calculate(new ComprehensiveScoreInput(
                row.getCoursewareLearningScore(),
                row.getTrainingPracticeScore(),
                row.getCourseAssignmentScore(),
                row.getExamScore(),
                row.getCoursewareWeight().intValue(),
                row.getTrainingPracticeWeight().intValue(),
                row.getAssignmentWeight().intValue(),
                row.getExamWeight().intValue()));
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
