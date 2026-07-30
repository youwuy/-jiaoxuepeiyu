USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `training_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_name` VARCHAR(128) NOT NULL,
  `training_mode` VARCHAR(16) NOT NULL COMMENT 'SINGLE/TEAM',
  `publish_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/OFFLINE',
  `open_start_time` DATETIME DEFAULT NULL,
  `open_end_time` DATETIME DEFAULT NULL,
  `team_size` INT NOT NULL DEFAULT 1,
  `app_required` TINYINT NOT NULL DEFAULT 1,
  `created_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_course_status_time` (`publish_status`, `open_start_time`, `open_end_time`),
  KEY `idx_training_course_mode` (`training_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student visible training courses';

CREATE TABLE IF NOT EXISTS `training_participant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_participant_student` (`training_id`, `student_id`),
  KEY `idx_training_participant_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='students assigned to training courses';

CREATE TABLE IF NOT EXISTS `training_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `role_name` VARCHAR(64) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_training_role_training` (`training_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='roles available in a team training';

CREATE TABLE IF NOT EXISTS `training_app_installation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL,
  `installed` TINYINT NOT NULL DEFAULT 0,
  `app_version` VARCHAR(32) DEFAULT NULL,
  `download_url` VARCHAR(512) DEFAULT NULL,
  `install_message` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_app_installation_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student UE training app installation status';

CREATE TABLE IF NOT EXISTS `training_team_room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `owner_student_id` BIGINT NOT NULL,
  `room_code` VARCHAR(64) NOT NULL,
  `room_status` VARCHAR(16) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/STARTED/DISSOLVED',
  `started_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_team_room_code` (`room_code`),
  KEY `idx_training_team_room_training_status` (`training_id`, `room_status`),
  KEY `idx_training_team_room_owner` (`owner_student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='student team training rooms';

CREATE TABLE IF NOT EXISTS `training_team_room_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `role_name` VARCHAR(64) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_team_room_role` (`room_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='room role snapshot';

CREATE TABLE IF NOT EXISTS `training_team_room_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `role_id` BIGINT DEFAULT NULL,
  `member_status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/LEFT',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `left_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_team_room_member` (`room_id`, `student_id`),
  KEY `idx_training_team_room_member_student` (`student_id`, `member_status`),
  KEY `idx_training_team_room_member_role` (`room_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='room active and historical members';
