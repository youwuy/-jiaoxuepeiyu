USE `jiaoxuepeiyu`;

SET @chapter_deleted_flag_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_chapter'
    AND COLUMN_NAME = 'deleted_flag'
);
SET @chapter_deleted_flag_sql = IF(
  @chapter_deleted_flag_exists = 0,
  'ALTER TABLE `course_chapter` ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `sort_order`',
  'SELECT 1'
);
PREPARE chapter_deleted_flag_stmt FROM @chapter_deleted_flag_sql;
EXECUTE chapter_deleted_flag_stmt;
DEALLOCATE PREPARE chapter_deleted_flag_stmt;

SET @content_deleted_flag_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'course_content'
    AND COLUMN_NAME = 'deleted_flag'
);
SET @content_deleted_flag_sql = IF(
  @content_deleted_flag_exists = 0,
  'ALTER TABLE `course_content` ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `sort_order`',
  'SELECT 1'
);
PREPARE content_deleted_flag_stmt FROM @content_deleted_flag_sql;
EXECUTE content_deleted_flag_stmt;
DEALLOCATE PREPARE content_deleted_flag_stmt;
