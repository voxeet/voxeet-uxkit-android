package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

public class PermissionResult {
    @NonNull
    public String permission;
    public boolean isGranted;

    public PermissionResult(@NonNull String permission, Boolean isGranted) {
        this.permission = permission;
        this.isGranted = null != isGranted && isGranted;
    }
}
