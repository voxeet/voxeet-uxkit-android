package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

import com.voxeet.promise.Promise;

import java.util.Arrays;
import java.util.List;

public class PermissionController {

    private static PermissionManager permissionManager = new PermissionManager();

    public static void register(@NonNull IRequestPermissions callback) {
        permissionManager.register(callback);
    }

    public static boolean isPermissionNeverAskAgain(@NonNull String permission) throws Exception {
        return permissionManager.isPermissionNeverAskAgain(permission);
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull String permission) throws Exception {
        return permissionManager.shouldShowRequestPermissionRationale(permission);
    }

    @NonNull
    public static Promise<List<PermissionResult>> requestPermissions(@NonNull List<String> permissions) {
        return permissionManager.requestPermissions(permissions);
    }

    @NonNull
    public static Promise<List<PermissionResult>> requestPermissions(@NonNull String ...permissions) {
        return permissionManager.requestPermissions(Arrays.asList(permissions));
    }
}
