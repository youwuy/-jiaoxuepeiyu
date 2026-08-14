USE `jiaoxuepeiyu`;

ALTER TABLE `training_attempt`
  ADD COLUMN `deleted_flag` TINYINT NOT NULL DEFAULT 0 AFTER `recording_url`;

CREATE INDEX `idx_training_attempt_active_topic`
  ON `training_attempt` (`training_id`, `topic_id`, `deleted_flag`, `student_id`, `submitted_at`);
