package com.qizhifu.jiaoxuepeiyu.admin.training.controller;

import com.qizhifu.jiaoxuepeiyu.admin.training.model.AdminTrainingTopic;
import com.qizhifu.jiaoxuepeiyu.admin.training.repository.AdminTrainingTopicMapper;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/training-topics")
@Tag(name = "Admin Training Topics", description = "Enabled UE training topic catalog for training composition.")
public class AdminTrainingTopicController {
    private final AdminTrainingTopicMapper mapper;

    public AdminTrainingTopicController(AdminTrainingTopicMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List training topics")
    public ApiResponse<List<AdminTrainingTopic>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trainingMode", required = false) String trainingMode) {
        String keywordLike = keyword == null || keyword.trim().isEmpty() ? null : "%" + keyword.trim() + "%";
        String mode = trainingMode == null || trainingMode.trim().isEmpty() ? null : trainingMode.trim().toUpperCase();
        return ApiResponse.ok(mapper.findTopics(keywordLike, mode));
    }
}
