package com.qizhifu.jiaoxuepeiyu.admin.iam;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AdminPermissionActionInitializer implements ApplicationRunner {

    private static final List<ActionPatch> STANDARD_ACTIONS = Arrays.asList(
            new ActionPatch("列表", "list", 10),
            new ActionPatch("新增", "create", 20),
            new ActionPatch("删除", "delete", 30),
            new ActionPatch("修改", "update", 40),
            new ActionPatch("启用", "enable", 50),
            new ActionPatch("禁用", "disable", 60));

    private final JdbcTemplate jdbcTemplate;

    public AdminPermissionActionInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("sys_permission")) {
            return;
        }
        for (ActionPatch action : STANDARD_ACTIONS) {
            insertMissingAction(action);
        }
        if (tableExists("sys_role_permission")) {
            backfillListBindings();
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName);
        return count != null && count.intValue() > 0;
    }

    private void insertMissingAction(ActionPatch action) {
        jdbcTemplate.update("INSERT INTO sys_permission "
                        + "(parent_id, permission_name, permission_code, permission_type, route_path, visible, sort_order, created_at, updated_at) "
                        + "SELECT p.id, ?, CONCAT(p.permission_code, ?), 'BUTTON', NULL, p.visible, ?, NOW(), NOW() "
                        + "FROM sys_permission p "
                        + "WHERE p.permission_type = 'PAGE' "
                        + "AND NOT EXISTS ("
                        + "SELECT 1 FROM sys_permission child "
                        + "WHERE child.parent_id = p.id AND child.permission_code = CONCAT(p.permission_code, ?)"
                        + ") "
                        + "AND NOT EXISTS ("
                        + "SELECT 1 FROM sys_permission same_code "
                        + "WHERE same_code.permission_code = CONCAT(p.permission_code, ?)"
                        + ")",
                action.name,
                ":" + action.codeSuffix,
                Integer.valueOf(action.sortOrder),
                ":" + action.codeSuffix,
                ":" + action.codeSuffix);
    }

    private void backfillListBindings() {
        jdbcTemplate.update("INSERT INTO sys_role_permission "
                + "(role_id, permission_id, data_scope, created_at) "
                + "SELECT page_binding.role_id, list_action.id, page_binding.data_scope, NOW() "
                + "FROM sys_role_permission page_binding "
                + "JOIN sys_permission page ON page.id = page_binding.permission_id "
                + "AND page.permission_type = 'PAGE' "
                + "JOIN sys_permission list_action ON list_action.parent_id = page.id "
                + "AND list_action.permission_type = 'BUTTON' "
                + "AND list_action.permission_code = CONCAT(page.permission_code, ':list') "
                + "LEFT JOIN sys_role_permission existing ON existing.role_id = page_binding.role_id "
                + "AND existing.permission_id = list_action.id "
                + "WHERE existing.id IS NULL");
    }

    private static class ActionPatch {
        private final String name;
        private final String codeSuffix;
        private final int sortOrder;

        private ActionPatch(String name, String codeSuffix, int sortOrder) {
            this.name = name;
            this.codeSuffix = codeSuffix;
            this.sortOrder = sortOrder;
        }
    }
}
