package com.qizhifu.jiaoxuepeiyu.admin.course;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminCourseSchemaInitializer implements ApplicationRunner {

    private static final String COURSE_CONTENT = "course_content";
    private static final String COURSE_ASSIGNMENT = "course_assignment";
    private static final String ASSIGNMENT_QUESTION = "assignment_question";
    private static final List<ColumnPatch> CONTENT_COLUMNS = Arrays.asList(
            new ColumnPatch(COURSE_CONTENT, "learning_start_time",
                    "ALTER TABLE `course_content` ADD COLUMN `learning_start_time` DATETIME NULL AFTER `required_duration_seconds`"),
            new ColumnPatch(COURSE_CONTENT, "learning_end_time",
                    "ALTER TABLE `course_content` ADD COLUMN `learning_end_time` DATETIME NULL AFTER `learning_start_time`"));
    private static final List<ColumnPatch> ASSIGNMENT_COLUMNS = Arrays.asList(
            new ColumnPatch(COURSE_ASSIGNMENT, "answer_start_time",
                    "ALTER TABLE `course_assignment` ADD COLUMN `answer_start_time` DATETIME NULL AFTER `deadline`"),
            new ColumnPatch(COURSE_ASSIGNMENT, "answer_end_time",
                    "ALTER TABLE `course_assignment` ADD COLUMN `answer_end_time` DATETIME NULL AFTER `answer_start_time`"),
            new ColumnPatch(COURSE_ASSIGNMENT, "completion_rule",
                    "ALTER TABLE `course_assignment` ADD COLUMN `completion_rule` VARCHAR(32) NOT NULL DEFAULT 'SUBMIT' AFTER `publish_status`"),
            new ColumnPatch(COURSE_ASSIGNMENT, "pass_score",
                    "ALTER TABLE `course_assignment` ADD COLUMN `pass_score` INT NULL AFTER `completion_rule`"),
            new ColumnPatch(COURSE_ASSIGNMENT, "publish_mode",
                    "ALTER TABLE `course_assignment` ADD COLUMN `publish_mode` VARCHAR(32) NOT NULL DEFAULT 'PRACTICE' AFTER `pass_score`"));
    private static final List<ColumnPatch> ASSIGNMENT_QUESTION_COLUMNS = Arrays.asList(
            new ColumnPatch(ASSIGNMENT_QUESTION, "source_question_id",
                    "ALTER TABLE `assignment_question` ADD COLUMN `source_question_id` BIGINT NULL AFTER `assignment_id`"));
    private static final List<IndexPatch> CONTENT_INDEXES = Arrays.asList(
            new IndexPatch(COURSE_CONTENT, "idx_course_content_learning_window", "learning_start_time",
                    "CREATE INDEX `idx_course_content_learning_window` ON `course_content` (`learning_start_time`, `learning_end_time`)"));
    private static final List<IndexPatch> ASSIGNMENT_INDEXES = Arrays.asList(
            new IndexPatch(COURSE_ASSIGNMENT, "idx_course_assignment_answer_window", "answer_start_time",
                    "CREATE INDEX `idx_course_assignment_answer_window` ON `course_assignment` (`answer_start_time`, `answer_end_time`)"));

    private final AdminCourseSchemaOperations operations;

    public AdminCourseSchemaInitializer(AdminCourseSchemaOperations operations) {
        this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureCourseCompatibility();
    }

    void ensureCourseCompatibility() {
        applyColumns(CONTENT_COLUMNS);
        applyColumns(ASSIGNMENT_COLUMNS);
        applyColumns(ASSIGNMENT_QUESTION_COLUMNS);
        applyIndexes(CONTENT_INDEXES);
        applyIndexes(ASSIGNMENT_INDEXES);
    }

    private void applyColumns(List<ColumnPatch> columns) {
        for (ColumnPatch column : columns) {
            if (operations.tableExists(column.tableName)
                    && !operations.columnExists(column.tableName, column.columnName)) {
                operations.execute(column.sql);
            }
        }
    }

    private void applyIndexes(List<IndexPatch> indexes) {
        for (IndexPatch index : indexes) {
            if (operations.tableExists(index.tableName)
                    && operations.columnExists(index.tableName, index.requiredColumn)
                    && !operations.indexExists(index.tableName, index.indexName)) {
                operations.execute(index.sql);
            }
        }
    }

    private static class ColumnPatch {
        private final String tableName;
        private final String columnName;
        private final String sql;

        private ColumnPatch(String tableName, String columnName, String sql) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.sql = sql;
        }
    }

    private static class IndexPatch {
        private final String tableName;
        private final String indexName;
        private final String requiredColumn;
        private final String sql;

        private IndexPatch(String tableName, String indexName, String requiredColumn, String sql) {
            this.tableName = tableName;
            this.indexName = indexName;
            this.requiredColumn = requiredColumn;
            this.sql = sql;
        }
    }
}

interface AdminCourseSchemaOperations {

    boolean tableExists(String tableName);

    boolean columnExists(String tableName, String columnName);

    boolean indexExists(String tableName, String indexName);

    void execute(String sql);
}

@Component
class JdbcAdminCourseSchemaOperations implements AdminCourseSchemaOperations {

    private final JdbcTemplate jdbcTemplate;

    JdbcAdminCourseSchemaOperations(JdbcTemplate jdbcTemplate) {
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
    public boolean indexExists(String tableName, String indexName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                Integer.class,
                tableName,
                indexName);
        return count != null && count.intValue() > 0;
    }

    @Override
    public void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}
