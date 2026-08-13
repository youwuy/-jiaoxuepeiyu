USE `jiaoxuepeiyu`;

ALTER TABLE `training_role`
  ADD COLUMN `topic_id` BIGINT DEFAULT NULL AFTER `training_id`,
  ADD COLUMN `ai_fill_enabled` TINYINT NOT NULL DEFAULT 0 AFTER `role_name`;

ALTER TABLE `training_team_room`
  ADD COLUMN `topic_id` BIGINT DEFAULT NULL AFTER `training_id`;

ALTER TABLE `training_team_room_role`
  ADD COLUMN `ai_fill_enabled` TINYINT NOT NULL DEFAULT 0 AFTER `role_name`;

CREATE INDEX `idx_training_role_topic`
  ON `training_role` (`training_id`, `topic_id`, `sort_order`);

CREATE INDEX `idx_training_room_topic_status`
  ON `training_team_room` (`training_id`, `topic_id`, `room_status`);
