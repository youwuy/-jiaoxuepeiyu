USE `jiaoxuepeiyu`;

ALTER TABLE `training_course`
  ADD COLUMN `recording_enabled` TINYINT NOT NULL DEFAULT 0
  COMMENT 'whether the UE client should record this training' AFTER `app_required`;
