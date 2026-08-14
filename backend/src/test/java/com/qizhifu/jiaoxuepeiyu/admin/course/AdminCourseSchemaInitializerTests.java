package com.qizhifu.jiaoxuepeiyu.admin.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminCourseSchemaInitializerTests {

    @Test
    void addsMissingCourseContentAndAssignmentColumns() {
        FakeCourseSchemaOperations operations = new FakeCourseSchemaOperations();
        operations.addColumns("course_content",
                "id", "course_id", "chapter_id", "item_type", "title", "resource_id", "assignment_id",
                "required_duration_seconds", "sort_order", "created_at", "updated_at");
        operations.addColumns("course_assignment",
                "id", "course_id", "content_id", "assignment_title", "assignment_type", "deadline",
                "total_score", "publish_status", "created_by", "created_at", "updated_at");
        AdminCourseSchemaInitializer initializer = new AdminCourseSchemaInitializer(operations);

        initializer.ensureCourseCompatibility();

        assertTrue(operations.columns("course_content").containsAll(Arrays.asList(
                "learning_start_time", "learning_end_time")));
        assertTrue(operations.columns("course_assignment").containsAll(Arrays.asList(
                "answer_start_time", "answer_end_time", "completion_rule", "pass_score", "publish_mode")));
        assertTrue(operations.indexes("course_content").contains("idx_course_content_learning_window"));
        assertTrue(operations.indexes("course_assignment").contains("idx_course_assignment_answer_window"));
        assertEquals(9, operations.executedSql.size());
    }

    @Test
    void skipsExistingColumnsAndIndexes() {
        FakeCourseSchemaOperations operations = new FakeCourseSchemaOperations();
        operations.addColumns("course_content",
                "learning_start_time", "learning_end_time");
        operations.addColumns("course_assignment",
                "answer_start_time", "answer_end_time", "completion_rule", "pass_score", "publish_mode");
        operations.addIndexes("course_content", "idx_course_content_learning_window");
        operations.addIndexes("course_assignment", "idx_course_assignment_answer_window");
        AdminCourseSchemaInitializer initializer = new AdminCourseSchemaInitializer(operations);

        initializer.ensureCourseCompatibility();

        assertEquals(0, operations.executedSql.size());
    }

    private static class FakeCourseSchemaOperations implements AdminCourseSchemaOperations {
        private final Set<String> tables = new LinkedHashSet<String>(Arrays.asList(
                "course_content", "course_assignment", "assignment_training"));
        private final List<String> executedSql = new ArrayList<String>();
        private final java.util.Map<String, Set<String>> tableColumns = new java.util.LinkedHashMap<String, Set<String>>();
        private final java.util.Map<String, Set<String>> tableIndexes = new java.util.LinkedHashMap<String, Set<String>>();

        private void addColumns(String tableName, String... columns) {
            columns(tableName).addAll(Arrays.asList(columns));
        }

        private void addIndexes(String tableName, String... indexes) {
            indexes(tableName).addAll(Arrays.asList(indexes));
        }

        private Set<String> columns(String tableName) {
            if (!tableColumns.containsKey(tableName)) {
                tableColumns.put(tableName, new LinkedHashSet<String>());
            }
            return tableColumns.get(tableName);
        }

        private Set<String> indexes(String tableName) {
            if (!tableIndexes.containsKey(tableName)) {
                tableIndexes.put(tableName, new LinkedHashSet<String>());
            }
            return tableIndexes.get(tableName);
        }

        @Override
        public boolean tableExists(String tableName) {
            return tables.contains(tableName);
        }

        @Override
        public boolean columnExists(String tableName, String columnName) {
            return columns(tableName).contains(columnName);
        }

        @Override
        public boolean indexExists(String tableName, String indexName) {
            return indexes(tableName).contains(indexName);
        }

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
            String normalized = sql.replace("`", "");
            if (normalized.startsWith("ALTER TABLE ")) {
                String[] parts = normalized.split(" ");
                columns(parts[2]).add(parts[5]);
            }
            if (normalized.startsWith("CREATE INDEX ")) {
                String[] parts = normalized.split(" ");
                String tableName = parts[4].replace("(", "").trim();
                indexes(tableName).add(parts[2]);
            }
        }
    }
}
