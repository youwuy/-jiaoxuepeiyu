USE `jiaoxuepeiyu`;

ALTER TABLE `sys_user`
  ADD COLUMN `job_title` VARCHAR(32) NULL AFTER `id_card`;

CREATE INDEX `idx_sys_user_org` ON `sys_user` (`org_id`);
