package com.qizhifu.jiaoxuepeiyu.controller;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Service health check API.")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service status, service name, and current server time.")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("status", "OK");
        result.put("service", "jiaoxuepeiyu-backend");
        result.put("time", LocalDateTime.now().toString());
        return ApiResponse.ok(result);
    }
}
