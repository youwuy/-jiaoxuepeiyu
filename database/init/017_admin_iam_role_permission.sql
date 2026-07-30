USE `jiaoxuepeiyu`;

ALTER TABLE `sys_role`
  ADD COLUMN `data_scope` VARCHAR(32) NOT NULL DEFAULT 'PERSONAL' AFTER `role_code`,
  ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL AFTER `data_scope`,
  ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `status`;

CREATE INDEX `idx_sys_role_status_deleted` ON `sys_role` (`status`, `deleted_flag`);
CREATE INDEX `idx_sys_role_deleted_updated` ON `sys_role` (`deleted_flag`, `updated_at`);

CREATE TABLE IF NOT EXISTS `sys_role_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sys_role_log_role` (`role_id`, `created_at`),
  KEY `idx_sys_role_log_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role operation logs';
