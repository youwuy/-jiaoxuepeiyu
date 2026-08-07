package com.qizhifu.jiaoxuepeiyu.admin.exam;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AdminQuestionSchemaInitializer implements ApplicationRunner {

    private final AdminQuestionSchemaOperations operations;

    public AdminQuestionSchemaInitializer(AdminQuestionSchemaOperations operations) {
        this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureQuestionCompatibility();
    }

    void ensureQuestionCompatibility() {
        if (operations.tableExists("exam_question")
                && !operations.columnExists("exam_question", "explanation")) {
            operations.execute("ALTER TABLE `exam_question` ADD COLUMN `explanation` TEXT NULL AFTER `standard_answer`");
        }
        if (operations.tableExists("exam_paper")
                && !operations.columnExists("exam_paper", "course_name")) {
            operations.execute("ALTER TABLE `exam_paper` ADD COLUMN `course_name` VARCHAR(128) NULL AFTER `paper_name`");
        }
    }
}

interface AdminQuestionSchemaOperations {

    boolean tableExists(String tableName);

    boolean columnExists(String tableName, String columnName);

    void execute(String sql);
}

@Component
class JdbcAdminQuestionSchemaOperations implements AdminQuestionSchemaOperations {

    private final JdbcTemplate jdbcTemplate;

    JdbcAdminQuestionSchemaOperations(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName);
        return count != null && count.intValue() > 0;
    }

    @Override
    public boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count.intValue() > 0;
    }

    @Override
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}
