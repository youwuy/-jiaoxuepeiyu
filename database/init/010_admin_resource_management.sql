USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `res_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_name` VARCHAR(128) NOT NULL,
  `resource_type` VARCHAR(32) NOT NULL COMMENT 'DOCUMENT/PRESENTATION/IMAGE/VIDEO/AUDIO',
  `cover_url` VARCHAR(512) NOT NULL,
  `file_url` VARCHAR(512) NOT NULL,
  `preview_url` VARCHAR(512) DEFAULT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `major_id` BIGINT NOT NULL,
  `course_name` VARCHAR(64) DEFAULT NULL,
  `uploader_id` BIGINT NOT NULL,
  `public_status` VARCHAR(16) NOT NULL DEFAULT 'NOT_APPLIED' COMMENT 'NOT_APPLIED/PENDING/PUBLIC/REJECTED',
  `current_version` INT NOT NULL DEFAULT 1,
  `public_version` INT DEFAULT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_res_resource_uploader` (`uploader_id`, `deleted_flag`),
  KEY `idx_res_resource_major` (`major_id`, `deleted_flag`),
  KEY `idx_res_resource_type` (`resource_type`, `deleted_flag`),
  KEY `idx_res_resource_public_status` (`public_status`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='admin personal resources';

CREATE TABLE IF NOT EXISTS `res_resource_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL,
  `resource_version` INT NOT NULL,
  `resource_name` VARCHAR(128) NOT NULL,
  `resource_type` VARCHAR(32) NOT NULL,
  `cover_url` VARCHAR(512) NOT NULL,
  `file_url` VARCHAR(512) NOT NULL,
  `preview_url` VARCHAR(512) DEFAULT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `major_id` BIGINT NOT NULL,
  `course_name` VARCHAR(64) DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_res_resource_version` (`resource_id`, `resource_version`),
  KEY `idx_res_resource_version_resource` (`resource_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='resource version snapshots';

CREATE TABLE IF NOT EXISTS `res_public_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL,
  `resource_version` INT NOT NULL,
  `applicant_id` BIGINT NOT NULL,
  `reviewer_id` BIGINT DEFAULT NULL,
  `public_status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `review_comment` VARCHAR(500) DEFAULT NULL,
  `applied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_res_public_application_resource` (`resource_id`, `resource_version`),
  KEY `idx_res_public_application_status` (`public_status`, `applied_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='resource public review applications';

CREATE TABLE IF NOT EXISTS `res_resource_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_res_resource_log_resource` (`resource_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='resource operation logs';

ALTER TABLE `res_public_resource`
  ADD COLUMN `source_resource_id` BIGINT DEFAULT NULL AFTER `id`,
  ADD COLUMN `resource_version` INT NOT NULL DEFAULT 1 AFTER `source_resource_id`,
  ADD COLUMN `file_url` VARCHAR(512) DEFAULT NULL AFTER `preview_url`,
  ADD COLUMN `file_name` VARCHAR(255) DEFAULT NULL AFTER `file_url`,
  ADD COLUMN `file_size` BIGINT NOT NULL DEFAULT 0 AFTER `file_name`,
  ADD COLUMN `course_name` VARCHAR(64) DEFAULT NULL AFTER `major_id`;

CREATE UNIQUE INDEX `uk_res_public_resource_source` ON `res_public_resource` (`source_resource_id`);
CREATE INDEX `idx_res_public_resource_uploader` ON `res_public_resource` (`uploader_id`);
