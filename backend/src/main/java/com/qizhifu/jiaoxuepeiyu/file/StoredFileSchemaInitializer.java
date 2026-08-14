package com.qizhifu.jiaoxuepeiyu.file;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StoredFileSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public StoredFileSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_uploaded_file` ("
                + "`id` BIGINT NOT NULL AUTO_INCREMENT,"
                + "`file_url` VARCHAR(512) NOT NULL,"
                + "`file_name` VARCHAR(255) NOT NULL,"
                + "`stored_file_name` VARCHAR(255) NOT NULL,"
                + "`file_size` BIGINT NOT NULL,"
                + "`content_type` VARCHAR(128) NOT NULL,"
                + "`category` VARCHAR(40) NOT NULL,"
                + "`uploader_id` BIGINT NOT NULL,"
                + "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (`id`),"
                + "KEY `idx_uploaded_file_uploader` (`uploader_id`),"
                + "KEY `idx_uploaded_file_category` (`category`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='uploaded file registry'");
    }
}
