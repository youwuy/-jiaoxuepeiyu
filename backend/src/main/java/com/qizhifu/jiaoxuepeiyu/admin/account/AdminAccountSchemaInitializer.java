package com.qizhifu.jiaoxuepeiyu.admin.account;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminAccountSchemaInitializer implements ApplicationRunner {

    private static final String SYS_USER = "sys_user";
    private static final List<ColumnPatch> ACCOUNT_COLUMNS = Arrays.asList(
            new ColumnPatch("password_hash", "ALTER TABLE `sys_user` ADD COLUMN `password_hash` VARCHAR(100) NULL AFTER `status`"),
            new ColumnPatch("org_id", "ALTER TABLE `sys_user` ADD COLUMN `org_id` BIGINT NULL AFTER `password_hash`"),
            new ColumnPatch("class_id", "ALTER TABLE `sys_user` ADD COLUMN `class_id` BIGINT NULL AFTER `org_id`"),
            new ColumnPatch("face_file_id", "ALTER TABLE `sys_user` ADD COLUMN `face_file_id` BIGINT NULL AFTER `class_id`"),
            new ColumnPatch("fingerprint_file_id", "ALTER TABLE `sys_user` ADD COLUMN `fingerprint_file_id` BIGINT NULL AFTER `face_file_id`"),
            new ColumnPatch("id_card", "ALTER TABLE `sys_user` ADD COLUMN `id_card` VARCHAR(32) NULL AFTER `phone`"),
            new ColumnPatch("job_title", "ALTER TABLE `sys_user` ADD COLUMN `job_title` VARCHAR(32) NULL AFTER `id_card`"));
    private static final List<IndexPatch> ACCOUNT_INDEXES = Arrays.asList(
            new IndexPatch("idx_sys_user_phone", "phone", "CREATE INDEX `idx_sys_user_phone` ON `sys_user` (`phone`)"),
            new IndexPatch("idx_sys_user_type_status", "user_type", "CREATE INDEX `idx_sys_user_type_status` ON `sys_user` (`user_type`, `status`)"),
            new IndexPatch("idx_sys_user_class", "class_id", "CREATE INDEX `idx_sys_user_class` ON `sys_user` (`class_id`)"),
            new IndexPatch("idx_sys_user_org", "org_id", "CREATE INDEX `idx_sys_user_org` ON `sys_user` (`org_id`)"));

    private final AdminAccountSchemaOperations operations;

    public AdminAccountSchemaInitializer(AdminAccountSchemaOperations operations) {
        this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureSysUserCompatibility();
    }

    void ensureSysUserCompatibility() {
        if (!operations.tableExists(SYS_USER)) {
            return;
        }
        for (ColumnPatch column : ACCOUNT_COLUMNS) {
            if (!operations.columnExists(SYS_USER, column.columnName)) {
                operations.execute(column.sql);
            }
        }
        for (IndexPatch index : ACCOUNT_INDEXES) {
            if (operations.columnExists(SYS_USER, index.requiredColumn)
                    && !operations.indexExists(SYS_USER, index.indexName)) {
                operations.execute(index.sql);
            }
        }
    }

    private static class ColumnPatch {
        private final String columnName;
        private final String sql;

        private ColumnPatch(String columnName, String sql) {
            this.columnName = columnName;
            this.sql = sql;
        }
    }

    private static class IndexPatch {
        private final String indexName;
        private final String requiredColumn;
        private final String sql;

        private IndexPatch(String indexName, String requiredColumn, String sql) {
            this.indexName = indexName;
            this.requiredColumn = requiredColumn;
            this.sql = sql;
        }
    }
}

interface AdminAccountSchemaOperations {

    boolean tableExists(String tableName);

    boolean columnExists(String tableName, String columnName);

    boolean indexExists(String tableName, String indexName);

    void execute(String sql);
}

@Component
class JdbcAdminAccountSchemaOperations implements AdminAccountSchemaOperations {

    private final JdbcTemplate jdbcTemplate;

    JdbcAdminAccountSchemaOperations(JdbcTemplate jdbcTemplate) {
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
