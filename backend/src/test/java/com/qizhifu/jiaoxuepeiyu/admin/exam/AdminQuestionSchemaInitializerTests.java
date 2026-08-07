package com.qizhifu.jiaoxuepeiyu.admin.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminQuestionSchemaInitializerTests {

    @Test
    void addsMissingExplanationColumn() {
        FakeSchemaOperations operations = new FakeSchemaOperations();
        AdminQuestionSchemaInitializer initializer = new AdminQuestionSchemaInitializer(operations);

        initializer.ensureQuestionCompatibility();

        assertEquals(1, operations.executeCount);
        assertEquals(true, operations.columns.contains("explanation"));
    }

    @Test
    void skipsExistingExplanationColumn() {
        FakeSchemaOperations operations = new FakeSchemaOperations();
        operations.columns.add("explanation");
        AdminQuestionSchemaInitializer initializer = new AdminQuestionSchemaInitializer(operations);

        initializer.ensureQuestionCompatibility();

        assertEquals(0, operations.executeCount);
    }

    private static class FakeSchemaOperations implements AdminQuestionSchemaOperations {
        private final Set<String> columns = new HashSet<String>();
        private int executeCount;

        @Override
        public boolean tableExists(String tableName) {
            return "exam_question".equals(tableName);
        }

        @Override
        public boolean columnExists(String tableName, String columnName) {
            return columns.contains(columnName);
        }

        @Override
        public void execute(String sql) {
            executeCount++;
            columns.add("explanation");
        }
    }
}
