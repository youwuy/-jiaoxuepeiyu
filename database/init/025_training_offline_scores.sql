USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `training_offline_score_import_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `total_count` INT NOT NULL DEFAULT 0,
  `success_count` INT NOT NULL DEFAULT 0,
  `failure_count` INT NOT NULL DEFAULT 0,
  `imported_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_offline_batch_training` (`training_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='training offline score import batches';

CREATE TABLE IF NOT EXISTS `training_offline_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `student_no` VARCHAR(64) NOT NULL,
  `student_name` VARCHAR(64) NOT NULL,
  `class_name` VARCHAR(128) NOT NULL,
  `total_score` DECIMAL(8,2) NOT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `import_batch_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_offline_student` (`training_id`, `student_id`),
  KEY `idx_training_offline_batch` (`import_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='latest offline score per training participant';

CREATE TABLE IF NOT EXISTS `training_offline_topic_score` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `offline_score_id` BIGINT NOT NULL,
  `topic_id` BIGINT NOT NULL,
  `score` DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_offline_topic` (`offline_score_id`, `topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='offline score details by training topic';

CREATE TABLE IF NOT EXISTS `training_offline_score_import_error` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `batch_id` BIGINT NOT NULL,
  `row_number` INT NOT NULL,
  `student_no` VARCHAR(64) DEFAULT NULL,
  `error_message` VARCHAR(1000) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_offline_error_batch` (`batch_id`, `row_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='training offline score import row errors';
