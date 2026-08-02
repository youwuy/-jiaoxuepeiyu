package com.qizhifu.jiaoxuepeiyu.admin.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminAccountSchemaInitializerTests {

    @Test
    void addsMissingAccountColumnsAndIndexesToExistingSysUserTable() {
        FakeSchemaOperations operations = new FakeSchemaOperations(
                "id", "username", "real_name", "phone", "user_type", "status", "created_at", "updated_at");
        AdminAccountSchemaInitializer initializer = new AdminAccountSchemaInitializer(operations);

        initializer.ensureSysUserCompatibility();

        assertTrue(operations.columns.containsAll(Arrays.asList(
                "password_hash",
                "org_id",
                "class_id",
                "face_file_id",
                "fingerprint_file_id",
                "id_card",
                "job_title")));
        assertTrue(operations.indexes.containsAll(Arrays.asList(
                "idx_sys_user_phone",
                "idx_sys_user_type_status",
                "idx_sys_user_class",
                "idx_sys_user_org")));
        assertTrue(operations.executedSql.get(0).contains("ADD COLUMN `password_hash`"));
        assertTrue(operations.executedSql.get(operations.executedSql.size() - 1)
                .contains("CREATE INDEX `idx_sys_user_org`"));
    }

    @Test
    void skipsExistingAccountColumnsAndIndexes() {
        FakeSchemaOperations operations = new FakeSchemaOperations(
                "id", "username", "real_name", "phone", "user_type", "status", "password_hash",
                "org_id", "class_id", "face_file_id", "fingerprint_file_id", "id_card", "job_title");
        operations.indexes.addAll(Arrays.asList(
                "idx_sys_user_phone", "idx_sys_user_type_status", "idx_sys_user_class", "idx_sys_user_org"));
        AdminAccountSchemaInitializer initializer = new AdminAccountSchemaInitializer(operations);

        initializer.ensureSysUserCompatibility();

        assertEquals(0, operations.executedSql.size());
    }

    private static class FakeSchemaOperations implements AdminAccountSchemaOperations {
        private final Set<String> columns = new LinkedHashSet<String>();
        private final Set<String> indexes = new LinkedHashSet<String>();
        private final List<String> executedSql = new ArrayList<String>();

        private FakeSchemaOperations(String... columns) {
            this.columns.addAll(Arrays.asList(columns));
        }

        @Override
        public boolean tableExists(String tableName) {
            return "sys_user".equals(tableName);
        }

        @Override
        public boolean columnExists(String tableName, String columnName) {
            return columns.contains(columnName);
        }

        @Override
        public boolean indexExists(String tableName, String indexName) {
            return indexes.contains(indexName);
        }

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
            String normalized = sql.replace("`", "");
            if (normalized.startsWith("ALTER TABLE sys_user ADD COLUMN ")) {
                String[] parts = normalized.substring("ALTER TABLE sys_user ADD COLUMN ".length()).split(" ");
                columns.add(parts[0]);
            }
            if (normalized.startsWith("CREATE INDEX ")) {
                String[] parts = normalized.split(" ");
                indexes.add(parts[2]);
            }
        }
    }
}
