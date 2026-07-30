package com.qizhifu.jiaoxuepeiyu.admin.config.controller;

import com.qizhifu.jiaoxuepeiyu.admin.AdminContext;
import com.qizhifu.jiaoxuepeiyu.admin.config.AdminScoreConfigService;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRule;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreGradeRuleCommand;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeight;
import com.qizhifu.jiaoxuepeiyu.admin.config.model.AdminScoreWeightCommand;
import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Score Config", description = "Comprehensive score weight and grade rule configuration APIs.")
public class AdminScoreConfigController {

    private final AdminScoreConfigService service;

    public AdminScoreConfigController(AdminScoreConfigService service) {
        this.service = service;
    }

    @GetMapping("/score-weights")
    @Operation(summary = "List score weights", description = "Returns score weight history, optionally filtered by semester.")
    public ApiResponse<List<AdminScoreWeight>> listScoreWeights(
            @RequestParam(value = "semesterId", required = false) Long semesterId) {
        return ApiResponse.ok(service.listScoreWeights(semesterId));
    }

    @PostMapping("/score-weights")
    @Operation(summary = "Create score weight", description = "Creates a new score weight history record. Header X-User-Id identifies the admin operator.")
    public ApiResponse<Long> createScoreWeight(@RequestBody AdminScoreWeightCommand body,
                                               HttpServletRequest request) {
        return ApiResponse.ok(service.createScoreWeight(body, AdminContext.requireAdminId(request)));
    }

    @GetMapping("/score-grade-rules")
    @Operation(summary = "List score grade rules", description = "Returns score grade rules sorted from high score to low score.")
    public ApiResponse<List<AdminScoreGradeRule>> listGradeRules() {
        return ApiResponse.ok(service.listGradeRules());
    }

    @PutMapping("/score-grade-rules")
    @Operation(summary = "Replace score grade rules", description = "Replaces all grade rules after validating that score ranges do not overlap.")
    public ApiResponse<Void> replaceGradeRules(@RequestBody List<AdminScoreGradeRuleCommand> body) {
        service.replaceGradeRules(body);
        return ApiResponse.ok(null);
    }
}
