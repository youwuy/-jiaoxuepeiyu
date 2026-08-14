USE `jiaoxuepeiyu`;

ALTER TABLE `training_attempt_step`
  ADD COLUMN `video_end_second` INT NOT NULL DEFAULT 0 AFTER `video_start_second`;
