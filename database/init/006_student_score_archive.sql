USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `score_semester_summary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `semester_id` BIGINT NOT NULL,
  `courseware_learning_score` DECIMAL(6,2) NOT NULL DEFAULT 0,
  `training_practice_score` DECIMAL(6,2) NOT NULL DEFAULT 0,
  `course_assignment_score` DECIMAL(6,2) NOT NULL DEFAULT 0,
  `exam_score` DECIMAL(6,2) NOT NULL DEFAULT 0,
  `courseware_weight` INT NOT NULL DEFAULT 0,
  `training_practice_weight` INT NOT NULL DEFAULT 0,
  `assignment_weight` INT NOT NULL DEFAULT 0,
  `exam_weight` INT NOT NULL DEFAULT 0,
  `comprehensive_score` DECIMAL(6,1) DEFAULT NULL,
  `published_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_score_semester_summary_student` (`student_id`, `semester_id`),
  KEY `idx_score_semester_summary_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student semester comprehensive score summaries';

CREATE TABLE IF NOT EXISTS `training_attempt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `training_id` BIGINT DEFAULT NULL,
  `training_name` VARCHAR(128) NOT NULL,
  `training_mode` VARCHAR(16) NOT NULL COMMENT 'SINGLE/TEAM',
  `role_name` VARCHAR(64) DEFAULT NULL,
  `submitted_at` DATETIME DEFAULT NULL,
  `submit_type` VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/ABNORMAL_EXIT/ROOM_DISSOLVED',
  `duration_seconds` INT NOT NULL DEFAULT 0,
  `personal_score` DECIMAL(6,2) DEFAULT NULL,
  `team_score` DECIMAL(6,2) DEFAULT NULL,
  `recording_url` VARCHAR(512) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_attempt_student_time` (`student_id`, `submitted_at`),
  KEY `idx_training_attempt_mode` (`training_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='immutable student training attempts';

CREATE TABLE IF NOT EXISTS `training_attempt_step` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attempt_id` BIGINT NOT NULL,
  `step_name` VARCHAR(128) NOT NULL,
  `standard_operation` VARCHAR(1024) DEFAULT NULL,
  `actual_operation` VARCHAR(1024) DEFAULT NULL,
  `score` DECIMAL(6,2) DEFAULT NULL,
  `duration_seconds` INT NOT NULL DEFAULT 0,
  `video_start_second` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_attempt_step_attempt` (`attempt_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student training archive step records';
