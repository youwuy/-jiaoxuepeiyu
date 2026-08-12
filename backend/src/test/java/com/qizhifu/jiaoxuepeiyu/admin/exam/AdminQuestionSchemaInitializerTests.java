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

        assertEquals(2, operations.executeCount);
        assertEquals(true, operations.columns.contains("explanation"));
        assertEquals(true, operations.columns.contains("question_course_name"));
    }

    @Test
    void skipsExistingExplanationColumn() {
        FakeSchemaOperations operations = new FakeSchemaOperations();
        operations.columns.add("explanation");
        operations.columns.add("question_course_name");
        AdminQuestionSchemaInitializer initializer = new AdminQuestionSchemaInitializer(operations);

        initializer.ensureQuestionCompatibility();

        assertEquals(0, operations.executeCount);
    }

    @Test
    void addsMissingPaperCourseNameColumn() {
        FakeSchemaOperations operations = new FakeSchemaOperations();
        operations.paperTableExists = true;
        operations.columns.add("explanation");
        operations.columns.add("question_course_name");
        AdminQuestionSchemaInitializer initializer = new AdminQuestionSchemaInitializer(operations);

        initializer.ensureQuestionCompatibility();

        assertEquals(1, operations.executeCount);
        assertEquals(true, operations.columns.contains("course_name"));
    }

    private static class FakeSchemaOperations implements AdminQuestionSchemaOperations {
        private final Set<String> columns = new HashSet<String>();
        private int executeCount;
        private boolean paperTableExists;

        @Override
        public boolean tableExists(String tableName) {
            return "exam_question".equals(tableName) || (paperTableExists && "exam_paper".equals(tableName));
        }

        @Override
        public boolean columnExists(String tableName, String columnName) {
            if ("exam_question".equals(tableName) && "course_name".equals(columnName)) {
                return columns.contains("question_course_name");
            }
            return columns.contains(columnName);
        }

        @Override
        public void execute(String sql) {
            executeCount++;
            if (sql.contains("exam_question") && sql.contains("course_name")) {
                columns.add("question_course_name");
            } else if (sql.contains("course_name")) {
                columns.add("course_name");
            } else {
                columns.add("explanation");
            }
        }
    }
}
