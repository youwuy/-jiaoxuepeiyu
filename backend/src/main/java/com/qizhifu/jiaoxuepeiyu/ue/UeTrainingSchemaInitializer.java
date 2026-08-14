package com.qizhifu.jiaoxuepeiyu.ue;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UeTrainingSchemaInitializer implements ApplicationRunner {

    private static final String TABLE_NAME = "training_attempt";
    private static final String COLUMN_NAME = "client_attempt_id";
    private static final String INDEX_NAME = "uk_training_attempt_client";
    private static final String DELETED_COLUMN_NAME = "deleted_flag";
    private static final String DELETED_INDEX_NAME = "idx_training_attempt_active_topic";

    private final JdbcTemplate jdbcTemplate;

    public UeTrainingSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!exists("information_schema.TABLES", "TABLE_NAME", TABLE_NAME)) {
            return;
        }
        if (!exists("information_schema.COLUMNS", "COLUMN_NAME", COLUMN_NAME)) {
            jdbcTemplate.execute("ALTER TABLE `training_attempt` ADD COLUMN `client_attempt_id` VARCHAR(64) NULL AFTER `training_id`");
        }
        if (!exists("information_schema.STATISTICS", "INDEX_NAME", INDEX_NAME)) {
            jdbcTemplate.execute("CREATE UNIQUE INDEX `uk_training_attempt_client` "
                    + "ON `training_attempt` (`student_id`, `training_id`, `client_attempt_id`)");
        }
        if (!exists("information_schema.COLUMNS", "COLUMN_NAME", DELETED_COLUMN_NAME)) {
            jdbcTemplate.execute("ALTER TABLE `training_attempt` ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `recording_url`");
        }
        if (!exists("information_schema.STATISTICS", "INDEX_NAME", DELETED_INDEX_NAME)) {
            jdbcTemplate.execute("CREATE INDEX `idx_training_attempt_active_topic` "
                    + "ON `training_attempt` (`training_id`, `topic_id`, `deleted_flag`, `student_id`, `submitted_at`)");
        }
    }

    private boolean exists(String metadataTable, String fieldName, String value) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + metadataTable
                        + " WHERE TABLE_SCHEMA = DATABASE() AND " + fieldName + " = ?",
                Integer.class,
                value);
        return count != null && count.intValue() > 0;
    }
}
