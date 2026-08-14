package com.qizhifu.jiaoxuepeiyu.admin.training;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTraining;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineImportBatch;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineScore;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineScoreImportCommand;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineScoreImportResult;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingOfflineScoreImportRow;
import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingTopic;
import com.qizhifu.jiaoxuepeiyu.admin.training.port.AdminTrainingRepository;
import com.qizhifu.jiaoxuepeiyu.admin.training.repository.AdminTrainingOfflineScoreMapper;
import com.qizhifu.jiaoxuepeiyu.common.exception.BusinessException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminTrainingOfflineScoreService {
    private static final int MAX_IMPORT_ROWS = 5000;

    private final AdminTrainingRepository trainingRepository;
    private final AdminTrainingOfflineScoreMapper mapper;
    private final Clock clock;

    @Autowired
    public AdminTrainingOfflineScoreService(AdminTrainingRepository trainingRepository,
                                            AdminTrainingOfflineScoreMapper mapper) {
        this(trainingRepository, mapper, Clock.systemDefaultZone());
    }

    AdminTrainingOfflineScoreService(AdminTrainingRepository trainingRepository,
                                     AdminTrainingOfflineScoreMapper mapper,
                                     Clock clock) {
        this.trainingRepository = trainingRepository;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<AdminTrainingOfflineScore> listScores(Long trainingId) {
        if (trainingRepository.findTraining(trainingId) == null) throw new BusinessException(404, "Training not found");
        return mapper.findScores(trainingId);
    }

    @Transactional
    public AdminTrainingOfflineScoreImportResult importScores(AdminTrainingOfflineScoreImportCommand command,
                                                               Long operatorId) {
        if (operatorId == null) throw new BusinessException(401, "Admin authentication is required");
        if (command == null || command.getTrainingId() == null) {
            throw new BusinessException(400, "Training is required");
        }
        AdminTraining training = trainingRepository.findTraining(command.getTrainingId());
        if (training == null) throw new BusinessException(404, "Training not found");
        if (!"PUBLISHED".equals(training.getPublishStatus()) || training.getOpenEndTime() == null
                || !training.getOpenEndTime().isBefore(LocalDateTime.now(clock))) {
            throw new BusinessException(400, "Only ended published trainings support offline score import");
        }
        String fileName = trim(command.getFileName());
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException(400, "Only .xlsx offline score files are supported");
        }
        if (command.getRows().isEmpty()) throw new BusinessException(400, "Import rows are required");
        if (command.getRows().size() > MAX_IMPORT_ROWS) throw new BusinessException(400, "Import rows cannot exceed 5000");

        List<AdminTrainingTopic> topics = mapper.findTopics(command.getTrainingId());
        if (topics.isEmpty()) throw new BusinessException(400, "Training topics are required before score import");
        Map<Long, BigDecimal> topicMaximums = new HashMap<Long, BigDecimal>();
        for (AdminTrainingTopic topic : topics) {
            topicMaximums.put(topic.getTopicId(), new BigDecimal(String.valueOf(topic.getScore())));
        }

        AdminTrainingOfflineImportBatch batch = new AdminTrainingOfflineImportBatch();
        batch.setTrainingId(command.getTrainingId());
        batch.setFileName(fileName);
        batch.setTotalCount(Integer.valueOf(command.getRows().size()));
        batch.setImportedBy(operatorId);
        mapper.insertBatch(batch);

        List<AdminTrainingOfflineScoreImportResult.RowError> errors = new ArrayList<AdminTrainingOfflineScoreImportResult.RowError>();
        Set<String> seenStudentNumbers = new HashSet<String>();
        int successCount = 0;
        for (int index = 0; index < command.getRows().size(); index++) {
            AdminTrainingOfflineScoreImportRow row = command.getRows().get(index);
            int rowNumber = row.getRowNumber() == null ? index + 2 : row.getRowNumber().intValue();
            String error = validateAndResolve(row, command.getTrainingId(), topicMaximums, seenStudentNumbers);
            if (error != null) {
                mapper.insertError(batch.getBatchId(), Integer.valueOf(rowNumber), trim(row.getStudentNo()), error);
                errors.add(new AdminTrainingOfflineScoreImportResult.RowError(Integer.valueOf(rowNumber), trim(row.getStudentNo()), error));
                continue;
            }
            saveRow(command.getTrainingId(), batch.getBatchId(), row);
            successCount++;
        }

        batch.setSuccessCount(Integer.valueOf(successCount));
        batch.setFailureCount(Integer.valueOf(errors.size()));
        mapper.finishBatch(batch);

        AdminTrainingOfflineScoreImportResult result = new AdminTrainingOfflineScoreImportResult();
        result.setBatchId(batch.getBatchId());
        result.setTotalCount(batch.getTotalCount());
        result.setSuccessCount(batch.getSuccessCount());
        result.setFailureCount(batch.getFailureCount());
        result.setErrors(errors);
        return result;
    }

    private String validateAndResolve(AdminTrainingOfflineScoreImportRow row,
                                      Long trainingId,
                                      Map<Long, BigDecimal> topicMaximums,
                                      Set<String> seenStudentNumbers) {
        if (row == null) return "数据行不能为空";
        String studentNo = trim(row.getStudentNo());
        if (studentNo == null) return "工号不能为空";
        if (!seenStudentNumbers.add(studentNo)) return "同一文件中工号重复";
        Map<String, Object> participant = mapper.findParticipant(trainingId, studentNo);
        if (participant == null || participant.isEmpty()) return "学员不属于该实训组课";

        String actualName = value(participant.get("studentName"));
        String actualClass = value(participant.get("className"));
        if (trim(row.getStudentName()) == null || !actualName.equals(trim(row.getStudentName()))) return "学员姓名与工号不匹配";
        if (trim(row.getClassName()) == null || !actualClass.equals(trim(row.getClassName()))) return "班级与学员信息不匹配";
        if (row.getTopicScores().size() != topicMaximums.size() || !row.getTopicScores().keySet().equals(topicMaximums.keySet())) {
            return "实训题成绩列不完整或包含非本组课题目";
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : row.getTopicScores().entrySet()) {
            BigDecimal score = entry.getValue();
            BigDecimal maximum = topicMaximums.get(entry.getKey());
            if (score == null || maximum == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(maximum) > 0) {
                return "实训题成绩必须在 0 至对应题目满分之间";
            }
            sum = sum.add(score);
        }
        if (row.getTotalScore() == null || row.getTotalScore().compareTo(BigDecimal.ZERO) < 0) return "总成绩不能为空且不能小于 0";
        if (row.getTotalScore().compareTo(sum) != 0) return "总成绩必须等于各实训题成绩合计";
        if (trim(row.getRemark()) != null && trim(row.getRemark()).length() > 500) return "训练备注不能超过 500 个字符";

        row.setStudentNo(studentNo);
        row.setStudentName(actualName);
        row.setClassName(actualClass);
        row.setRemark(trim(row.getRemark()));
        row.setStudentId(Long.valueOf(((Number) participant.get("studentId")).longValue()));
        return null;
    }

    private void saveRow(Long trainingId, Long batchId, AdminTrainingOfflineScoreImportRow row) {
        AdminTrainingOfflineScore score = new AdminTrainingOfflineScore();
        score.setTrainingId(trainingId);
        score.setStudentId(row.getStudentId());
        score.setStudentNo(row.getStudentNo());
        score.setStudentName(row.getStudentName());
        score.setClassName(row.getClassName());
        score.setTotalScore(row.getTotalScore());
        score.setRemark(row.getRemark());
        score.setImportBatchId(batchId);
        mapper.upsertScore(score);
        Long scoreId = mapper.findScoreId(trainingId, score.getStudentId());
        mapper.deleteTopicScores(scoreId);
        for (Map.Entry<Long, BigDecimal> entry : row.getTopicScores().entrySet()) {
            mapper.insertTopicScore(scoreId, entry.getKey(), entry.getValue());
        }
    }

    private String trim(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String value(Object value) { return value == null ? "" : String.valueOf(value); }
}
