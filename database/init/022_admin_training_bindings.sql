USE `jiaoxuepeiyu`;

ALTER TABLE `training_course`
  ADD COLUMN `classroom_id` BIGINT DEFAULT NULL,
  ADD COLUMN `score_basis` VARCHAR(20) NOT NULL DEFAULT 'HIGHEST',
  ADD COLUMN `exam_started_at` DATETIME DEFAULT NULL;

ALTER TABLE `training_monitor_snapshot`
  ADD COLUMN `current_topic_name` VARCHAR(128) NULL AFTER `progress_status`,
  ADD COLUMN `submitted_topic_count` INT NOT NULL DEFAULT 0 AFTER `current_topic_name`,
  ADD COLUMN `desktop_stream_url` VARCHAR(512) NULL AFTER `team_score`;

ALTER TABLE `training_attempt`
  ADD COLUMN `topic_id` BIGINT NULL AFTER `training_id`,
  ADD COLUMN `manual_score` DECIMAL(6,2) NULL AFTER `personal_score`,
  ADD COLUMN `review_comment` VARCHAR(500) NULL AFTER `recording_url`,
  ADD COLUMN `reviewer_id` BIGINT NULL AFTER `review_comment`,
  ADD COLUMN `reviewed_at` DATETIME NULL AFTER `reviewer_id`;

CREATE INDEX `idx_training_attempt_review_topic`
  ON `training_attempt` (`training_id`, `topic_id`, `student_id`, `submitted_at`);

CREATE TABLE IF NOT EXISTS `training_teacher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_training_teacher` (`training_id`, `teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `training_topic_binding` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `topic_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_training_topic` (`training_id`, `topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `training_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `topic_name` VARCHAR(128) NOT NULL,
  `category` VARCHAR(64) DEFAULT NULL,
  `training_mode` VARCHAR(16) NOT NULL COMMENT 'SINGLE/TEAM',
  `duration_minutes` INT NOT NULL DEFAULT 0,
  `score` INT NOT NULL DEFAULT 100,
  `role_names` VARCHAR(512) DEFAULT NULL,
  `enabled_flag` TINYINT NOT NULL DEFAULT 1,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_training_topic_mode` (`training_mode`, `enabled_flag`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
