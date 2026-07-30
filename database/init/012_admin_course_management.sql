USE `jiaoxuepeiyu`;

ALTER TABLE `course`
  ADD COLUMN `academic_year_id` BIGINT DEFAULT NULL AFTER `course_name`,
  ADD COLUMN `semester_id` BIGINT DEFAULT NULL AFTER `academic_year_id`,
  ADD COLUMN `major_id` BIGINT DEFAULT NULL AFTER `academic_term`,
  ADD COLUMN `cover_url` VARCHAR(512) DEFAULT NULL AFTER `major_id`,
  ADD COLUMN `class_names` VARCHAR(512) DEFAULT NULL AFTER `teacher_names`,
  ADD COLUMN `learning_mode` VARCHAR(32) NOT NULL DEFAULT 'SELF_PACED' COMMENT 'SELF_PACED/TEACHER_LED' AFTER `class_names`,
  ADD COLUMN `assignment_completion_rule` VARCHAR(32) NOT NULL DEFAULT 'SUBMIT' COMMENT 'SUBMIT/PASS_SCORE' AFTER `learning_mode`,
  ADD COLUMN `courseware_score_cap` INT NOT NULL DEFAULT 100 AFTER `assignment_completion_rule`,
  ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `created_by`;

CREATE INDEX `idx_course_term_major` ON `course` (`academic_year_id`, `semester_id`, `major_id`, `deleted_flag`);
CREATE INDEX `idx_course_status_deleted` ON `course` (`publish_status`, `deleted_flag`);

CREATE TABLE IF NOT EXISTS `course_teacher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `teacher_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_teacher` (`course_id`, `teacher_id`),
  KEY `idx_course_teacher_teacher` (`teacher_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course teacher bindings';

CREATE TABLE IF NOT EXISTS `course_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_class` (`course_id`, `class_id`),
  KEY `idx_course_class_class` (`class_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course teaching class bindings';

CREATE TABLE IF NOT EXISTS `course_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_log_course` (`course_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course operation logs';
