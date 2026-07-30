USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `device_code` VARCHAR(64) NOT NULL,
  `device_name` VARCHAR(128) NOT NULL,
  `device_type` VARCHAR(32) NOT NULL DEFAULT 'TRAINING_TERMINAL' COMMENT 'TRAINING_TERMINAL/VR/CONTROL_DESK/OTHER',
  `classroom_id` BIGINT DEFAULT NULL,
  `device_status` VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT 'OFFLINE/IDLE/IN_USE/FAULT',
  `last_heartbeat_at` DATETIME DEFAULT NULL,
  `deleted_flag` TINYINT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_device_classroom_status` (`classroom_id`, `device_status`, `deleted_flag`),
  KEY `idx_device_type` (`device_type`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='training device inventory';

CREATE TABLE IF NOT EXISTS `device_usage_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `device_id` BIGINT NOT NULL,
  `training_id` BIGINT DEFAULT NULL,
  `student_id` BIGINT DEFAULT NULL,
  `event_type` VARCHAR(32) NOT NULL COMMENT 'START/END/HEARTBEAT/FAULT',
  `started_at` DATETIME DEFAULT NULL,
  `ended_at` DATETIME DEFAULT NULL,
  `usage_minutes` INT NOT NULL DEFAULT 0,
  `event_time` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_device_usage_event_device_time` (`device_id`, `event_time`),
  KEY `idx_device_usage_event_training` (`training_id`, `event_time`),
  KEY `idx_device_usage_event_student` (`student_id`, `event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='raw device usage events';

CREATE TABLE IF NOT EXISTS `device_usage_daily_summary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `device_id` BIGINT NOT NULL,
  `usage_date` DATE NOT NULL,
  `usage_minutes` INT NOT NULL DEFAULT 0,
  `usage_count` INT NOT NULL DEFAULT 0,
  `fault_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_usage_daily` (`device_id`, `usage_date`),
  KEY `idx_device_usage_daily_date` (`usage_date`, `usage_minutes`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='daily device usage rollup';
