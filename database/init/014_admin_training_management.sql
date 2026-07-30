USE `jiaoxuepeiyu`;

ALTER TABLE `training_course`
  ADD COLUMN `academic_year_id` BIGINT DEFAULT NULL AFTER `training_name`,
  ADD COLUMN `semester_id` BIGINT DEFAULT NULL AFTER `academic_year_id`,
  ADD COLUMN `major_id` BIGINT DEFAULT NULL AFTER `semester_id`,
  ADD COLUMN `cover_url` VARCHAR(512) DEFAULT NULL AFTER `major_id`,
  ADD COLUMN `training_type` VARCHAR(16) NOT NULL DEFAULT 'PRACTICE' COMMENT 'PRACTICE/EXAM' AFTER `cover_url`,
  ADD COLUMN `paper_mode` VARCHAR(16) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/AUTO' AFTER `training_mode`,
  ADD COLUMN `paper_id` BIGINT DEFAULT NULL AFTER `paper_mode`,
  ADD COLUMN `class_names` VARCHAR(512) DEFAULT NULL AFTER `app_required`,
  ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `created_by`;

CREATE INDEX `idx_training_course_term_major`
  ON `training_course` (`academic_year_id`, `semester_id`, `major_id`, `deleted_flag`);

CREATE INDEX `idx_training_course_type_status`
  ON `training_course` (`training_type`, `training_mode`, `publish_status`, `deleted_flag`);

CREATE TABLE IF NOT EXISTS `training_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_class` (`training_id`, `class_id`),
  KEY `idx_training_class_class` (`class_id`, `training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='training course class bindings';

CREATE TABLE IF NOT EXISTS `training_course_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_course_log_training` (`training_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='training course operation logs';

CREATE TABLE IF NOT EXISTS `training_monitor_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `classroom_id` BIGINT DEFAULT NULL,
  `desk_status` VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT 'OFFLINE/ONLINE/FAULT',
  `progress_status` VARCHAR(32) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/RUNNING/SUBMITTED/ABNORMAL',
  `score` DECIMAL(8,2) DEFAULT NULL,
  `team_score` DECIMAL(8,2) DEFAULT NULL,
  `last_event_at` DATETIME DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_monitor_student` (`training_id`, `student_id`),
  KEY `idx_training_monitor_classroom` (`training_id`, `classroom_id`),
  KEY `idx_training_monitor_progress` (`training_id`, `progress_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='latest training monitor state per student';
