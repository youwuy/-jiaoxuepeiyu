USE `jiaoxuepeiyu`;

SET @chapter_parent_column_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_chapter'
    AND COLUMN_NAME = 'parent_chapter_id'
);
SET @add_chapter_parent_column_sql = IF(
  @chapter_parent_column_exists = 0,
  'ALTER TABLE `course_chapter` ADD COLUMN `parent_chapter_id` BIGINT DEFAULT NULL AFTER `course_id`',
  'SELECT 1'
);
PREPARE add_chapter_parent_column_stmt FROM @add_chapter_parent_column_sql;
EXECUTE add_chapter_parent_column_stmt;
DEALLOCATE PREPARE add_chapter_parent_column_stmt;

SET @chapter_parent_index_exists = (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_chapter'
    AND INDEX_NAME = 'idx_course_chapter_parent'
);
SET @add_chapter_parent_index_sql = IF(
  @chapter_parent_index_exists = 0,
  'CREATE INDEX `idx_course_chapter_parent` ON `course_chapter` (`parent_chapter_id`, `sort_order`)',
  'SELECT 1'
);
PREPARE add_chapter_parent_index_stmt FROM @add_chapter_parent_index_sql;
EXECUTE add_chapter_parent_index_stmt;
DEALLOCATE PREPARE add_chapter_parent_index_stmt;
