USE `jiaoxuepeiyu`;

ALTER TABLE `room_camera`
  ADD COLUMN `nvr_host` VARCHAR(64) NULL AFTER `camera_name`,
  ADD COLUMN `nvr_port` INT NULL AFTER `nvr_host`,
  ADD COLUMN `nvr_channel` VARCHAR(64) NULL AFTER `password`,
  MODIFY COLUMN `rtsp_url` VARCHAR(512) NULL;

CREATE UNIQUE INDEX `uk_room_camera_nvr_channel`
  ON `room_camera` (`room_id`, `nvr_host`, `nvr_channel`);

CREATE TABLE IF NOT EXISTS `edu_score_grade_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `grade_name` VARCHAR(32) NOT NULL,
  `min_score` DECIMAL(5,2) NOT NULL,
  `max_score` DECIMAL(5,2) NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_score_grade_rule_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='score grade rules';
