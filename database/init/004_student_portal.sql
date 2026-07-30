USE `jiaoxuepeiyu`;

ALTER TABLE `sys_user`
  ADD COLUMN `id_card` VARCHAR(32) NULL AFTER `phone`;

CREATE INDEX `idx_sys_user_class` ON `sys_user` (`class_id`);

CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `class_id` BIGINT NOT NULL,
  `course_name` VARCHAR(128) NOT NULL,
  `academic_term` VARCHAR(32) DEFAULT NULL,
  `teacher_names` VARCHAR(255) DEFAULT NULL,
  `courseware_count` INT NOT NULL DEFAULT 0,
  `assignment_count` INT NOT NULL DEFAULT 0,
  `publish_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  `open_start_time` DATETIME DEFAULT NULL,
  `open_end_time` DATETIME DEFAULT NULL,
  `created_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_class_status` (`class_id`, `publish_status`),
  KEY `idx_course_open_time` (`open_start_time`, `open_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student visible courses';

CREATE TABLE IF NOT EXISTS `course_learning_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `completed_items` INT NOT NULL DEFAULT 0,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_learning_progress` (`course_id`, `student_id`),
  KEY `idx_course_learning_progress_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student course learning progress';

CREATE TABLE IF NOT EXISTS `res_public_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_name` VARCHAR(128) NOT NULL,
  `resource_type` VARCHAR(32) NOT NULL COMMENT 'COURSEWARE/VIDEO/DOCUMENT/OTHER',
  `cover_url` VARCHAR(512) DEFAULT NULL,
  `preview_url` VARCHAR(512) DEFAULT NULL,
  `major_id` BIGINT DEFAULT NULL,
  `uploader_id` BIGINT DEFAULT NULL,
  `public_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLIC/OFFLINE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_res_public_resource_status` (`public_status`, `updated_at`),
  KEY `idx_res_public_resource_major` (`major_id`),
  KEY `idx_res_public_resource_type` (`resource_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='public learning resources';
