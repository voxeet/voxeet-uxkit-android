package com.voxeet.uxkit.common.permissions;

import androidx.annotation.NonNull;

import com.voxeet.promise.Promise;
import com.voxeet.sdk.utils.Map;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    @NonNull
    private WeakReference<IRequestPermissions> callback;

    public PermissionManager() {
        callback = new WeakReference<>(null);
    }

    public void register(@NonNull IRequestPermissions callback) {
        this.callback = new WeakReference<>(callback);
    }

    public boolean hasPermission(@NonNull String permission) throws Exception {
        IRequestPermissions callback = this.callback.get();

        if (null == callback) {
            throw new Exception("unable to call hasPermission: invalid permission callback");
        }
        return callback.hasPermission(permission);
    }

    public boolean isPermissionNeverAskAgain(@NonNull String permission) throws Exception {
        IRequestPermissions callback = this.callback.get();

        if (null == callback) {
            throw new Exception("unable to call isPermissionNeverAskAgain: invalid permission callback");
        }
        return callback.isPermissionNeverAskAgain(permission);
    }

    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) throws Exception {
        IRequestPermissions callback = this.callback.get();

        if (null == callback) {
            throw new Exception("unable to call shouldShowRequestPermissionRationale: invalid permission callback");
        }
        return callback.shouldShowRequestPermissionRationale(permission);
    }

    @NonNull
    public Promise<List<PermissionResult>> requestPermissions(@NonNull List<String> permissions) {
        return new Promise<>(solver -> {
            IRequestPermissions callback = this.callback.get();

            if (null == callback) {
                Promise.reject(solver, new Throwable("unable to request permissions: invalid permission callback"));
                return;
            }

            List<String> granted_permissions = Map.filter(permissions, callback::hasPermission);
            List<String> ungranted_permissions = Map.filter(permissions, s -> !callback.hasPermission(s));

            if (permissions.size() == 0) {
                solver.resolve(new ArrayList<>());
                return;
            }

            List<PermissionResult> granted = Map.map(granted_permissions, p -> new PermissionResult(p, true));

            if(ungranted_permissions.size() == 0) {
                solver.resolve(granted);
                return;
            }

            callback.requestPermissions(ungranted_permissions, update -> {
                List<PermissionResult> output = com.voxeet.sdk.utils.Map.map(update.keySet(),
                        perm -> new PermissionResult(perm, update.get(perm)));

                if(granted.size() > 0) {
                    output.addAll(granted);
                }

                solver.resolve(output);
            });
        });
    }
}
