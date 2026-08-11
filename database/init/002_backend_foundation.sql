USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `sys_user_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `token_hash` CHAR(64) NOT NULL,
  `portal` VARCHAR(16) NOT NULL COMMENT 'ADMIN/STUDENT',
  `login_ip` VARCHAR(64) DEFAULT NULL,
  `expires_at` DATETIME NOT NULL,
  `invalidated_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_session_token` (`token_hash`),
  KEY `idx_sys_user_session_user` (`user_id`),
  KEY `idx_sys_user_session_expire` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user login sessions';

CREATE TABLE IF NOT EXISTS `sys_org` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT NULL,
  `org_name` VARCHAR(128) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_org_parent` (`parent_id`, `sort_order`),
  KEY `idx_sys_org_name_parent` (`parent_id`, `org_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='organization tree';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(64) NOT NULL,
  `role_code` VARCHAR(64) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='roles';

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT NULL,
  `permission_name` VARCHAR(128) NOT NULL,
  `permission_code` VARCHAR(128) NOT NULL,
  `permission_type` VARCHAR(16) NOT NULL COMMENT 'MENU/PAGE/BUTTON',
  `route_path` VARCHAR(255) DEFAULT NULL,
  `visible` TINYINT NOT NULL DEFAULT 1,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_permission_code` (`permission_code`),
  KEY `idx_sys_permission_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='menu and button permissions';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  `data_scope` VARCHAR(32) NOT NULL DEFAULT 'PERSONAL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role permissions';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user roles';

CREATE TABLE IF NOT EXISTS `sys_user_org_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `org_id` BIGINT NOT NULL,
  `scope_type` VARCHAR(32) NOT NULL COMMENT 'BELONG/MANAGED/TEACHING_CLASS',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_org_scope_user` (`user_id`),
  KEY `idx_user_org_scope_org` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user organization scopes';

CREATE TABLE IF NOT EXISTS `edu_academic_year` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `year_name` VARCHAR(16) NOT NULL COMMENT 'yyyy-yyyy',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_academic_year_name` (`year_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='academic years';

CREATE TABLE IF NOT EXISTS `edu_semester` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `academic_year_id` BIGINT NOT NULL,
  `semester_name` VARCHAR(16) NOT NULL COMMENT 'FIRST/SECOND',
  `current_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_semester_year_name` (`academic_year_id`, `semester_name`),
  KEY `idx_edu_semester_current` (`current_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='semesters';

CREATE TABLE IF NOT EXISTS `edu_major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `major_name` VARCHAR(64) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_major_name` (`major_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='majors';

CREATE TABLE IF NOT EXISTS `edu_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `major_id` BIGINT DEFAULT NULL,
  `class_name` VARCHAR(64) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_edu_class_name` (`class_name`),
  KEY `idx_edu_class_major` (`major_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='classes';

CREATE TABLE IF NOT EXISTS `edu_score_weight` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `semester_id` BIGINT NOT NULL,
  `courseware_weight` INT NOT NULL,
  `training_practice_weight` INT NOT NULL,
  `assignment_weight` INT NOT NULL,
  `exam_weight` INT NOT NULL,
  `effective_from` DATETIME NOT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_edu_score_weight_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='comprehensive score weights';

CREATE TABLE IF NOT EXISTS `msg_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_type` VARCHAR(32) NOT NULL COMMENT 'COURSE/TRAINING/RESOURCE',
  `title` VARCHAR(128) NOT NULL,
  `content` VARCHAR(1024) NOT NULL,
  `source_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_notification_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='notifications';

CREATE TABLE IF NOT EXISTS `msg_user_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `notification_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `read_flag` TINYINT NOT NULL DEFAULT 0,
  `read_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_user_notification` (`notification_id`, `user_id`),
  KEY `idx_msg_user_notification_user` (`user_id`, `read_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user notification read state';
