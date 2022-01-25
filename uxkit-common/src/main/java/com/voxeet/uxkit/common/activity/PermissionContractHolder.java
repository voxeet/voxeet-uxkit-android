package com.voxeet.uxkit.common.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.audio.utils.__Call;
import com.voxeet.uxkit.common.permissions.IRequestPermissions;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionContractHolder {

    @Nullable
    private __Call<Map<String, Boolean>> tempRequestCallback;
    @Nullable
    private List<String> tempPermissions;

    private ActivityResultLauncher<String[]> multiplePermissions;
    private ActivityResultLauncher<String> singlePermission;

    public <T extends AbstractSDKService<? extends SDKBinder<T>>> PermissionContractHolder(AppCompatActivity appCompatActivity) {


        ActivityResultContracts.RequestMultiplePermissions multipleContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissions = appCompatActivity.registerForActivityResult(multipleContract, permissionCallback::apply);
        ActivityResultContracts.RequestPermission singleContract = new ActivityResultContracts.RequestPermission();
        singlePermission = appCompatActivity.registerForActivityResult(singleContract, result -> {
            if (null == tempPermissions || tempPermissions.size() == 0) {
                return;
            }
            Map<String, Boolean> map = new HashMap<>();
            map.put(tempPermissions.get(0), result);
            permissionCallback.apply(map);
        });
    }

    public IRequestPermissions getRequestPermissions() {
        return requestPermissions;
    }

    @NonNull
    private __Call<Map<String, Boolean>> permissionCallback = new __Call<Map<String, Boolean>>() {
        @Override
        public void apply(Map<String, Boolean> update) {
            if (null != tempRequestCallback) tempRequestCallback.apply(update);
        }
    };

    private IRequestPermissions requestPermissions = (permissions, callback) -> {
        tempRequestCallback = callback;
        tempPermissions = permissions;

        if (permissions.size() > 1) {
            multiplePermissions.launch(permissions.toArray(new String[0]));
        } else {
            String perm = permissions.get(0);
            singlePermission.launch(perm);
        }
    };
}
