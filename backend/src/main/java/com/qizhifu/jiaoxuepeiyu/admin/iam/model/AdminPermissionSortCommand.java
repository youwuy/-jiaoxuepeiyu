package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminPermissionSortCommand {

    private List<AdminPermissionSortItem> items = new ArrayList<AdminPermissionSortItem>();

    public List<AdminPermissionSortItem> getItems() {
        return items;
    }

    public void setItems(List<AdminPermissionSortItem> items) {
        this.items = items == null ? new ArrayList<AdminPermissionSortItem>() : items;
    }
}
