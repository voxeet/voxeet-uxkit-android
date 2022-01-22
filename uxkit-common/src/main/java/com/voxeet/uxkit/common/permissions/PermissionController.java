package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

import com.voxeet.promise.Promise;

import java.util.List;

public class PermissionController {

    private static PermissionManager permissionManager = new PermissionManager();

    public static void register(@NonNull IRequestPermissions callback) {
        permissionManager.register(callback);
    }

    @NonNull
    public static Promise<List<PermissionResult>> requestPermissions(@NonNull List<String> permissions) {
        return permissionManager.requestPermissions(permissions);
    }
}
