package com.qizhifu.jiaoxuepeiyu.admin.iam.model;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAccess {

    private boolean unrestricted;
    private List<String> permissionCodes = new ArrayList<String>();

    public boolean isUnrestricted() {
        return unrestricted;
    }

    public void setUnrestricted(boolean unrestricted) {
        this.unrestricted = unrestricted;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes == null ? new ArrayList<String>() : permissionCodes;
    }
}
