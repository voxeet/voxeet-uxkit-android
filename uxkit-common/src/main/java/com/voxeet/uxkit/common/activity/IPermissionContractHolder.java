package com.voxeet.uxkit.common.activity;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.common.permissions.IRequestPermissions;

public interface IPermissionContractHolder {

    boolean isPermissionNeverAskAgain(@NonNull String permission);

    boolean shouldShowRequestPermissionRationale(@NonNull String permission);

    IRequestPermissions getRequestPermissions();
}
