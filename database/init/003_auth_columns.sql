USE `jiaoxuepeiyu`;

ALTER TABLE `sys_user`
  ADD COLUMN `password_hash` VARCHAR(100) NULL AFTER `status`,
  ADD COLUMN `org_id` BIGINT NULL AFTER `password_hash`,
  ADD COLUMN `class_id` BIGINT NULL AFTER `org_id`,
  ADD COLUMN `face_file_id` BIGINT NULL AFTER `class_id`,
  ADD COLUMN `fingerprint_file_id` BIGINT NULL AFTER `face_file_id`;

CREATE INDEX `idx_sys_user_phone` ON `sys_user` (`phone`);
CREATE INDEX `idx_sys_user_type_status` ON `sys_user` (`user_type`, `status`);
