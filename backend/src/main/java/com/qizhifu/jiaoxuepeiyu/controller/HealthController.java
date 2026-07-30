package com.qizhifu.jiaoxuepeiyu.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Service health check API.")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service status, runtime version, database target, and current server time.")
    public ApiResponse<HealthStatus> health() {
        HealthStatus result = new HealthStatus();
        result.setStatus("OK");
        result.setService("jiaoxuepeiyu-backend");
        result.setJavaVersion(System.getProperty("java.version"));
        result.setDatabaseVersionTarget("MySQL 5.7.42.0");
        result.setTime(LocalDateTime.now());
        return ApiResponse.ok(result);
    }
}
