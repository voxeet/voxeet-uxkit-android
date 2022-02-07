package com.voxeet.uxkit.utils;

import android.Manifest;
import android.os.Build;

import androidx.annotation.NonNull;

import com.voxeet.promise.Promise;
import com.voxeet.uxkit.common.permissions.PermissionController;

public class ActionBarPermissionHelper {

    public static Promise<Boolean> checkMicrophonePermission() {
        return checkPermission(Manifest.permission.RECORD_AUDIO,
                "checkMicrophonePermission : RECORD_AUDIO permission  _is not_ set in your manifest. Please update accordingly");
    }


    public static Promise<Boolean> checkBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return Promise.resolve(true);
        return checkPermission(Manifest.permission.BLUETOOTH_CONNECT,
                "checkBluetoothConnectPermission: BLUETOOTH_CONNECT permission _is not_ set in your manifest. Please update accordingly");
    }

    public static Promise<Boolean> checkCameraPermission() {
        return checkPermission(Manifest.permission.CAMERA,
                "checkCameraPermission: CAMERA permission _is not_ set in your manifest. Please update accordingly");
    }

    private static Promise<Boolean> checkPermission(@NonNull String permission, @NonNull String error_message) {
        return new Promise<>(solver -> PermissionController.requestPermissions(permission).then(ok -> {
            if (ok.size() <= 0 || !ok.get(0).isGranted) {
                solver.reject(new IllegalStateException(error_message));
                return;
            }
            solver.resolve(true);
        }).error(solver::reject));
    }
}
