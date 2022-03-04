package com.voxeet.uxkit.common.activity;

import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.voxeet.audio.utils.__Call;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.common.permissions.IRequestPermissions;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionContractHolder implements IPermissionContractHolder {

    private final static ShortLogger Log = UXKitLogger.createLogger(PermissionContractHolder.class);

    private AppCompatActivity appCompatActivity;
    @Nullable
    private __Call<Map<String, Boolean>> tempRequestCallback;
    @Nullable
    private List<String> tempPermissions;

    private ActivityResultLauncher<String[]> multiplePermissions;
    private ActivityResultLauncher<String> singlePermission;

    private Map<String, Boolean> permissionRequestResults = new HashMap<>();

    @Override
    public boolean isPermissionNeverAskAgain(@NonNull String permission) {
        if (!permissionRequestResults.containsKey(permission)) return false;
        Boolean granted = permissionRequestResults.get(permission);
        if (null == granted) granted = false;
        return !granted && !shouldShowRequestPermissionRationale(permission);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return appCompatActivity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    public <T extends AbstractSDKService<? extends SDKBinder<T>>> PermissionContractHolder(AppCompatActivity appCompatActivity) {

        this.appCompatActivity = appCompatActivity;

        ActivityResultContracts.RequestMultiplePermissions multipleContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissions = appCompatActivity.registerForActivityResult(multipleContract, result -> {
            for (String permission : result.keySet()) {
                permissionRequestResults.put(permission, result.get(permission));
            }
            permissionCallback.apply(result);
        });
        ActivityResultContracts.RequestPermission singleContract = new ActivityResultContracts.RequestPermission();
        singlePermission = appCompatActivity.registerForActivityResult(singleContract, result -> {
            if (null == tempPermissions || tempPermissions.size() == 0) {
                return;
            }

            Map<String, Boolean> map = new HashMap<>();
            map.put(tempPermissions.get(0), result);
            permissionRequestResults.put(tempPermissions.get(0), result);
            permissionCallback.apply(map);
        });
    }

    @Override
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

    private IRequestPermissions requestPermissions = new IRequestPermissions() {
        @Override
        public void requestPermissions(@NonNull List<String> permissions, @NonNull __Call<Map<String, Boolean>> callback) {
            for (String permission : permissions) {
                boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, permission);
                Log.d(permission + " " + shouldShowRationale);
            }
            tempRequestCallback = callback;
            tempPermissions = permissions;

            if (permissions.size() > 1) {
                multiplePermissions.launch(permissions.toArray(new String[0]));
            } else {
                String perm = permissions.get(0);
                singlePermission.launch(perm);
            }
        }

        @Override
        public boolean hasPermission(@NonNull String permission) {
            return ContextCompat.checkSelfPermission(appCompatActivity, permission)
                    == PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public boolean isPermissionNeverAskAgain(@NonNull String permission) {
            return PermissionContractHolder.this.isPermissionNeverAskAgain(permission);
        }

        @Override
        public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
            return PermissionContractHolder.this.shouldShowRequestPermissionRationale(permission);
        }
    };
}
