USE `jiaoxuepeiyu`;

CREATE TABLE IF NOT EXISTS `edu_score_grade_rule_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `before_content` TEXT NOT NULL,
  `after_content` TEXT NOT NULL,
  `operator_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_score_grade_rule_log_created` (`created_at`),
  KEY `idx_score_grade_rule_log_operator` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='score grade rule operation logs';
