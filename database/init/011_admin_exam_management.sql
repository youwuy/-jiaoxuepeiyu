USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `exam_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_type` VARCHAR(32) NOT NULL COMMENT 'SINGLE/MULTIPLE/JUDGE/FILL_BLANK/SHORT_ANSWER',
  `title` VARCHAR(1000) NOT NULL,
  `standard_answer` TEXT NOT NULL,
  `explanation` TEXT NULL,
  `score` INT NOT NULL DEFAULT 0,
  `enabled_flag` TINYINT NOT NULL DEFAULT 1,
  `creator_id` BIGINT NOT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_question_type` (`question_type`, `enabled_flag`, `deleted_flag`),
  KEY `idx_exam_question_creator` (`creator_id`, `deleted_flag`),
  KEY `idx_exam_question_updated` (`updated_at`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='theory question bank';

CREATE TABLE IF NOT EXISTS `exam_question_option` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `option_key` VARCHAR(16) NOT NULL,
  `option_text` VARCHAR(1000) NOT NULL,
  `correct_flag` TINYINT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_question_option_key` (`question_id`, `option_key`),
  KEY `idx_exam_question_option_question` (`question_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='choice question options';

CREATE TABLE IF NOT EXISTS `exam_question_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_question_log_question` (`question_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='question operation logs';

CREATE TABLE IF NOT EXISTS `exam_question_import_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_name` VARCHAR(255) NOT NULL,
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `valid_count` INT NOT NULL DEFAULT 0,
  `error_count` INT NOT NULL DEFAULT 0,
  `operator_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_question_import_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='question import preview batches';

CREATE TABLE IF NOT EXISTS `exam_paper` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `paper_name` VARCHAR(128) NOT NULL,
  `course_name` VARCHAR(128) NULL,
  `compose_mode` VARCHAR(16) NOT NULL COMMENT 'MANUAL/AUTO',
  `total_score` INT NOT NULL DEFAULT 0,
  `question_count` INT NOT NULL DEFAULT 0,
  `publish_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  `creator_id` BIGINT NOT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_paper_status` (`publish_status`, `deleted_flag`),
  KEY `idx_exam_paper_creator` (`creator_id`, `deleted_flag`),
  KEY `idx_exam_paper_updated` (`updated_at`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='theory papers';

CREATE TABLE IF NOT EXISTS `exam_paper_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `paper_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `question_type` VARCHAR(32) NOT NULL,
  `title` VARCHAR(1000) NOT NULL,
  `options_json` LONGTEXT DEFAULT NULL,
  `standard_answer` TEXT NOT NULL,
  `score` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_paper_question` (`paper_id`, `question_id`),
  KEY `idx_exam_paper_question_paper` (`paper_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='paper question snapshots';

CREATE TABLE IF NOT EXISTS `exam_paper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `paper_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_paper_log_paper` (`paper_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='paper operation logs';

CREATE TABLE IF NOT EXISTS `exam_paper_import_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_name` VARCHAR(255) NOT NULL,
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `valid_count` INT NOT NULL DEFAULT 0,
  `error_count` INT NOT NULL DEFAULT 0,
  `operator_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_exam_paper_import_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='paper import preview batches';
