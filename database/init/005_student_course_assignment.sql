USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `course_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `parent_chapter_id` BIGINT DEFAULT NULL,
  `chapter_title` VARCHAR(128) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_chapter_course` (`course_id`, `parent_chapter_id`, `sort_order`),
  KEY `idx_course_chapter_parent` (`parent_chapter_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course chapters';

CREATE TABLE IF NOT EXISTS `course_content` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `chapter_id` BIGINT NOT NULL,
  `item_type` VARCHAR(16) NOT NULL COMMENT 'COURSEWARE/ASSIGNMENT',
  `title` VARCHAR(128) NOT NULL,
  `resource_id` BIGINT DEFAULT NULL,
  `assignment_id` BIGINT DEFAULT NULL,
  `required_duration_seconds` INT NOT NULL DEFAULT 0,
  `learning_start_time` DATETIME DEFAULT NULL,
  `learning_end_time` DATETIME DEFAULT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_content_course` (`course_id`, `sort_order`),
  KEY `idx_course_content_chapter` (`chapter_id`, `sort_order`),
  KEY `idx_course_content_assignment` (`assignment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='courseware and assignment content nodes';

CREATE TABLE IF NOT EXISTS `course_content_learning_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `content_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `studied_seconds` INT NOT NULL DEFAULT 0,
  `completed` TINYINT NOT NULL DEFAULT 0,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_content_learning_progress` (`content_id`, `student_id`),
  KEY `idx_content_learning_progress_course` (`course_id`, `student_id`, `completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student courseware content learning progress';

CREATE TABLE IF NOT EXISTS `course_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `course_id` BIGINT NOT NULL,
  `content_id` BIGINT DEFAULT NULL,
  `assignment_title` VARCHAR(128) NOT NULL,
  `assignment_type` VARCHAR(16) NOT NULL COMMENT 'THEORY/TRAINING',
  `deadline` DATETIME DEFAULT NULL,
  `answer_start_time` DATETIME DEFAULT NULL,
  `answer_end_time` DATETIME DEFAULT NULL,
  `completion_rule` VARCHAR(32) NOT NULL DEFAULT 'SUBMIT' COMMENT 'SUBMIT/PASS_SCORE',
  `pass_score` INT DEFAULT NULL,
  `publish_mode` VARCHAR(16) NOT NULL DEFAULT 'PRACTICE' COMMENT 'PRACTICE/EXAM',
  `total_score` INT NOT NULL DEFAULT 0,
  `publish_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  `created_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_assignment_course` (`course_id`, `publish_status`),
  KEY `idx_course_assignment_content` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course assignments';

CREATE TABLE IF NOT EXISTS `assignment_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `source_question_id` BIGINT DEFAULT NULL,
  `question_type` VARCHAR(16) NOT NULL COMMENT 'SINGLE/MULTIPLE/JUDGE/FILL/SHORT',
  `title` VARCHAR(1024) NOT NULL,
  `options_json` TEXT DEFAULT NULL,
  `standard_answer` TEXT DEFAULT NULL,
  `score` INT NOT NULL DEFAULT 0,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assignment_question_assignment` (`assignment_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course assignment questions';

CREATE TABLE IF NOT EXISTS `assignment_attempt` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/SAVED/SUBMITTED/REVIEWED',
  `score` INT DEFAULT NULL,
  `review_comment` VARCHAR(1024) DEFAULT NULL,
  `submitted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment_attempt_student` (`assignment_id`, `student_id`),
  KEY `idx_assignment_attempt_student_status` (`student_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student assignment attempts';

CREATE TABLE IF NOT EXISTS `assignment_answer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attempt_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `answer_content` TEXT DEFAULT NULL,
  `score` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment_answer_question` (`attempt_id`, `question_id`),
  KEY `idx_assignment_answer_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student assignment answers';
