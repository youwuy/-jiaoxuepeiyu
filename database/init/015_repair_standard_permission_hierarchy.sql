USE `jiaoxuepeiyu`;

-- Keep the standard navigation hierarchy aligned with the admin requirements.
UPDATE `sys_permission`
SET `parent_id` = 1, `sort_order` = 5, `updated_at` = NOW()
WHERE `permission_code` = 'role';

UPDATE `sys_permission`
SET `parent_id` = 1, `sort_order` = 6, `updated_at` = NOW()
WHERE `permission_code` = 'config';
