USE `jiaoxuepeiyu`;

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '列表', CONCAT(p.`permission_code`, ':list'), 'BUTTON', NULL, p.`visible`, 10, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':list')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':list')
  );

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '新增', CONCAT(p.`permission_code`, ':create'), 'BUTTON', NULL, p.`visible`, 20, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':create')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':create')
  );

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '删除', CONCAT(p.`permission_code`, ':delete'), 'BUTTON', NULL, p.`visible`, 30, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':delete')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':delete')
  );

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '修改', CONCAT(p.`permission_code`, ':update'), 'BUTTON', NULL, p.`visible`, 40, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':update')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':update')
  );

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '启用', CONCAT(p.`permission_code`, ':enable'), 'BUTTON', NULL, p.`visible`, 50, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':enable')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':enable')
  );

INSERT INTO `sys_permission`
  (`parent_id`, `permission_name`, `permission_code`, `permission_type`, `route_path`, `visible`, `sort_order`, `created_at`, `updated_at`)
SELECT p.`id`, '禁用', CONCAT(p.`permission_code`, ':disable'), 'BUTTON', NULL, p.`visible`, 60, NOW(), NOW()
FROM `sys_permission` p
WHERE p.`permission_type` = 'PAGE'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` child
    WHERE child.`parent_id` = p.`id` AND child.`permission_code` = CONCAT(p.`permission_code`, ':disable')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_permission` same_code
    WHERE same_code.`permission_code` = CONCAT(p.`permission_code`, ':disable')
  );

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `data_scope`, `created_at`)
SELECT page_binding.`role_id`, list_action.`id`, page_binding.`data_scope`, NOW()
FROM `sys_role_permission` page_binding
JOIN `sys_permission` page
  ON page.`id` = page_binding.`permission_id` AND page.`permission_type` = 'PAGE'
JOIN `sys_permission` list_action
  ON list_action.`parent_id` = page.`id`
  AND list_action.`permission_type` = 'BUTTON'
  AND list_action.`permission_code` = CONCAT(page.`permission_code`, ':list')
LEFT JOIN `sys_role_permission` existing
  ON existing.`role_id` = page_binding.`role_id`
  AND existing.`permission_id` = list_action.`id`
WHERE existing.`id` IS NULL;
