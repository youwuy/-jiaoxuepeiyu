package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminRolePermissionCommand {

    private List<Long> permissionIds = new ArrayList<Long>();

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds == null ? new ArrayList<Long>() : permissionIds;
    }
}
