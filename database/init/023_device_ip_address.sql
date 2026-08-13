USE `jiaoxuepeiyu`;

ALTER TABLE `device`
  ADD COLUMN `ip_address` VARCHAR(64) DEFAULT NULL AFTER `device_status`;

CREATE INDEX `idx_device_ip_address` ON `device` (`ip_address`);
