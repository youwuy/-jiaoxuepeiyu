USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `assignment_training` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignment_id` BIGINT NOT NULL,
  `training_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_assignment_training` (`assignment_id`, `training_id`),
  KEY `idx_assignment_training_training` (`training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course assignment training bindings';
