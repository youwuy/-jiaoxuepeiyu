USE `jiaoxuepeiyu`;

SET @sys_org_created_by_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_org'
    AND COLUMN_NAME = 'created_by'
);
SET @add_sys_org_created_by_sql = IF(
  @sys_org_created_by_exists = 0,
  'ALTER TABLE `sys_org` ADD COLUMN `created_by` BIGINT DEFAULT NULL AFTER `status`',
  'SELECT 1'
);
PREPARE add_sys_org_created_by_stmt FROM @add_sys_org_created_by_sql;
EXECUTE add_sys_org_created_by_stmt;
DEALLOCATE PREPARE add_sys_org_created_by_stmt;

SET @sys_org_updated_by_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_org'
    AND COLUMN_NAME = 'updated_by'
);
SET @add_sys_org_updated_by_sql = IF(
  @sys_org_updated_by_exists = 0,
  'ALTER TABLE `sys_org` ADD COLUMN `updated_by` BIGINT DEFAULT NULL AFTER `created_by`',
  'SELECT 1'
);
PREPARE add_sys_org_updated_by_stmt FROM @add_sys_org_updated_by_sql;
EXECUTE add_sys_org_updated_by_stmt;
DEALLOCATE PREPARE add_sys_org_updated_by_stmt;

SET @sys_org_name_parent_index_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_org'
    AND INDEX_NAME = 'idx_sys_org_name_parent'
);
SET @add_sys_org_name_parent_index_sql = IF(
  @sys_org_name_parent_index_exists = 0,
  'CREATE INDEX `idx_sys_org_name_parent` ON `sys_org` (`parent_id`, `org_name`)',
  'SELECT 1'
);
PREPARE add_sys_org_name_parent_index_stmt FROM @add_sys_org_name_parent_index_sql;
EXECUTE add_sys_org_name_parent_index_stmt;
DEALLOCATE PREPARE add_sys_org_name_parent_index_stmt;
