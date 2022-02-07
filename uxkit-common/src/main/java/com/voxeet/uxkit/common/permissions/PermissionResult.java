package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

import com.voxeet.sdk.utils.Opt;

public class PermissionResult {
    @NonNull
    public String permission;
    public boolean isGranted;

    public PermissionResult(@NonNull String permission, Boolean isGranted) {
        this.permission = permission;
        this.isGranted = null != isGranted && isGranted;
    }

    public boolean isFor(@NonNull String permission) {
        return Opt.of(permission).then(p -> p.equals(this.permission)).or(false);
    }
}
