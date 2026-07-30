USE `jiaoxuepeiyu`;

ALTER TABLE `sys_user`
  ADD COLUMN `id_card` VARCHAR(32) NULL AFTER `phone`,
  ADD COLUMN `job_title` VARCHAR(32) NULL AFTER `id_card`;

CREATE INDEX `idx_sys_user_org` ON `sys_user` (`org_id`);
CREATE INDEX `idx_sys_user_class` ON `sys_user` (`class_id`);
