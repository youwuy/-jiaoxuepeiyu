USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `training_student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `training_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_training_student` (`training_id`, `student_id`),
  KEY `idx_training_student_student` (`student_id`, `training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='direct student bindings for training courses';
