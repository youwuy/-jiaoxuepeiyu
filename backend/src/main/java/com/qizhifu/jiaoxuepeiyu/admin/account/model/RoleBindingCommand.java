package com.qizhifu.jiaoxuepeiyu.admin.account.model;

import java.util.ArrayList;
import java.util.List;

public class RoleBindingCommand {

    private List<Long> roleIds = new ArrayList<Long>();

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds == null ? new ArrayList<Long>() : roleIds;
    }
}
