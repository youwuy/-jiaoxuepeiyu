USE `jiaoxuepeiyu`;

CREATE INDEX `idx_sys_user_presence`
  ON `sys_user` (`status`, `user_type`, `last_heartbeat_time`);
