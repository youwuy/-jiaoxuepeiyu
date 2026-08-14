USE `jiaoxuepeiyu`;

ALTER TABLE `training_attempt_step`
  ADD COLUMN `max_score` DECIMAL(6,2) DEFAULT NULL AFTER `score`;
