package com.qizhifu.jiaoxuepeiyu.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.qizhifu.jiaoxuepeiyu.common.api.ApiResponse;
import org.junit.jupiter.api.Test;

class HealthControllerTests {

    @Test
    void returnsRuntimeAndDatabaseTargetForSmokeChecks() {
        HealthController controller = new HealthController();

        ApiResponse<HealthStatus> response = controller.health();
        HealthStatus status = response.getData();

        assertEquals(0, response.getCode());
        assertEquals("OK", status.getStatus());
        assertEquals("jiaoxuepeiyu-backend", status.getService());
        assertEquals("MySQL 5.7.42.0", status.getDatabaseVersionTarget());
        assertNotNull(status.getJavaVersion());
        assertNotNull(status.getTime());
    }
}
