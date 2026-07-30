USE `jiaoxuepeiyu`;

ALTER TABLE `assignment_attempt`
  ADD COLUMN `reviewer_id` BIGINT DEFAULT NULL AFTER `review_comment`,
  ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL AFTER `reviewer_id`;

ALTER TABLE `assignment_answer`
  ADD COLUMN `review_comment` VARCHAR(512) DEFAULT NULL AFTER `score`;

CREATE INDEX `idx_assignment_attempt_assignment_status` ON `assignment_attempt` (`assignment_id`, `status`);
CREATE INDEX `idx_assignment_attempt_reviewer` ON `assignment_attempt` (`reviewer_id`, `reviewed_at`);

CREATE TABLE IF NOT EXISTS `assignment_review_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attempt_id` BIGINT NOT NULL,
  `reviewer_id` BIGINT NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `content` VARCHAR(512) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_assignment_review_log_attempt` (`attempt_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='assignment review operation logs';
