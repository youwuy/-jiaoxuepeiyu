package com.qizhifu.jiaoxuepeiyu.admin.training;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminTrainingOfflineScoreServiceTests {
    @Mock private AdminTrainingRepository trainingRepository;
    @Mock private AdminTrainingOfflineScoreMapper mapper;
    private AdminTrainingOfflineScoreService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-14T02:00:00Z"), ZoneId.of("Asia/Shanghai"));
        service = new AdminTrainingOfflineScoreService(trainingRepository, mapper, clock);
    }

    @Test
    void importsValidEndedTrainingRow() {
        when(trainingRepository.findTraining(9L)).thenReturn(training(LocalDateTime.of(2026, 8, 14, 9, 0)));
        when(mapper.findTopics(9L)).thenReturn(Arrays.asList(topic(31L, 100)));
        when(mapper.findParticipant(9L, "S001")).thenReturn(participant());
        when(mapper.insertBatch(any(AdminTrainingOfflineImportBatch.class))).thenAnswer(invocation -> {
            ((AdminTrainingOfflineImportBatch) invocation.getArgument(0)).setBatchId(77L);
            return 1;
        });
        when(mapper.findScoreId(9L, 5L)).thenReturn(88L);

        AdminTrainingOfflineScoreImportResult result = service.importScores(command(), 3L);

        assertEquals(Integer.valueOf(1), result.getSuccessCount());
        assertEquals(Integer.valueOf(0), result.getFailureCount());
        verify(mapper).upsertScore(any(AdminTrainingOfflineScore.class));
        verify(mapper).deleteTopicScores(88L);
        verify(mapper).insertTopicScore(88L, 31L, new BigDecimal("90"));
    }

    @Test
    void rejectsTrainingThatHasNotEnded() {
        when(trainingRepository.findTraining(9L)).thenReturn(training(LocalDateTime.of(2026, 8, 14, 11, 0)));
        assertThrows(BusinessException.class, () -> service.importScores(command(), 3L));
        verify(mapper, never()).insertBatch(any(AdminTrainingOfflineImportBatch.class));
    }

    @Test
    void storesInvalidParticipantAsBatchError() {
        when(trainingRepository.findTraining(9L)).thenReturn(training(LocalDateTime.of(2026, 8, 14, 9, 0)));
        when(mapper.findTopics(9L)).thenReturn(Arrays.asList(topic(31L, 100)));
        when(mapper.findParticipant(anyLong(), anyString())).thenReturn(new HashMap<String, Object>());
        when(mapper.insertBatch(any(AdminTrainingOfflineImportBatch.class))).thenAnswer(invocation -> {
            ((AdminTrainingOfflineImportBatch) invocation.getArgument(0)).setBatchId(77L);
            return 1;
        });

        AdminTrainingOfflineScoreImportResult result = service.importScores(command(), 3L);

        assertEquals(Integer.valueOf(0), result.getSuccessCount());
        assertEquals(Integer.valueOf(1), result.getFailureCount());
        verify(mapper).insertError(77L, 2, "S001", "学员不属于该实训组课");
        verify(mapper, never()).upsertScore(any(AdminTrainingOfflineScore.class));
    }

    private AdminTraining training(LocalDateTime endTime) {
        AdminTraining training = new AdminTraining();
        training.setTrainingId(9L);
        training.setPublishStatus("PUBLISHED");
        training.setOpenEndTime(endTime);
        return training;
    }

    private AdminTrainingTopic topic(Long id, int score) {
        AdminTrainingTopic topic = new AdminTrainingTopic();
        topic.setTopicId(id);
        topic.setTopicName("站务处置");
        topic.setScore(Integer.valueOf(score));
        return topic;
    }

    private Map<String, Object> participant() {
        Map<String, Object> participant = new HashMap<String, Object>();
        participant.put("studentId", 5L);
        participant.put("studentNo", "S001");
        participant.put("studentName", "张三");
        participant.put("className", "城轨一班");
        return participant;
    }

    private AdminTrainingOfflineScoreImportCommand command() {
        AdminTrainingOfflineScoreImportRow row = new AdminTrainingOfflineScoreImportRow();
        row.setRowNumber(Integer.valueOf(2));
        row.setStudentNo("S001");
        row.setStudentName("张三");
        row.setClassName("城轨一班");
        row.setTotalScore(new BigDecimal("90"));
        Map<Long, BigDecimal> scores = new HashMap<Long, BigDecimal>();
        scores.put(31L, new BigDecimal("90"));
        row.setTopicScores(scores);

        AdminTrainingOfflineScoreImportCommand command = new AdminTrainingOfflineScoreImportCommand();
        command.setTrainingId(9L);
        command.setFileName("成绩.xlsx");
        command.setRows(Arrays.asList(row));
        return command;
    }
}
