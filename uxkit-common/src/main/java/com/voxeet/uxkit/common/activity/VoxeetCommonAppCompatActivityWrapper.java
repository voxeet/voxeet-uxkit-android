package com.voxeet.uxkit.common.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.services.screenshare.RequestScreenSharePermissionEvent;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.activity.bundle.IncomingBundleChecker;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.common.notification.IncomingNotificationHelper;
import com.voxeet.uxkit.common.permissions.PermissionController;
import com.voxeet.uxkit.common.permissions.PermissionResult;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;
import com.voxeet.uxkit.common.service.SystemServiceFactory;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * VoxeetAppCompatActivity manages the call state
 * <p>
 * In the current merged state, this class is not used
 * <p>
 * However, it is extremely easy to use this class now :
 * - manages automatically the bundles to join conferences when "resumed"
 * - automatically registers its subclasses's extra info to propagate to "recreated" instances
 * <p>
 * Few things to consider :
 * - singleTop / singleInstance
 */
public abstract class VoxeetCommonAppCompatActivityWrapper<T extends AbstractSDKService<? extends SDKBinder<T>>> {

    private static final ShortLogger Log = UXKitLogger.createLogger(VoxeetCommonAppCompatActivityWrapper.class);

    @NonNull
    private final AppCompatActivity parentActivity;

    private IncomingBundleChecker incomingBundleChecker;

    @Nullable
    private T sdkService;

    /**
     * Flag set to true when the last request for camera permission failed, use the commit method to restore it to false
     * after the adequate warning has been made to the user
     */
    private boolean cameraPermissionPermaBanned = false;
    private PermissionContractHolder permissionContractHolder;

    public VoxeetCommonAppCompatActivityWrapper(@NonNull AppCompatActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    protected abstract void onSdkServiceAvailable();

    protected abstract void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event);

    protected abstract boolean canBeRegisteredToReceiveCalls();

    public abstract IncomingBundleChecker createIncomingBundleChecker(@Nullable Intent intent);

    @Nullable
    public T getSdkService() {
        return sdkService;
    }

    public void onCreate(Bundle savedInstanceState) {
        //create a check incoming call
        incomingBundleChecker = createIncomingBundleChecker(parentActivity.getIntent());

        permissionContractHolder = new PermissionContractHolder(parentActivity);

        startService();
    }

    public void onResume() {
        PermissionController.register(permissionContractHolder.getRequestPermissions());

        SystemServiceFactory.setLastAppCompatActivity(parentActivity.getClass());
        startService();

        VoxeetSDK.instance().register(this);

        if (canBeRegisteredToReceiveCalls()) {
            ActivityInfoHolder.setTempAcceptedIncomingActivityOnResume(this);
            ActivityInfoHolder.setTempAcceptedIncomingActivity(parentActivity.getClass());
            ActivityInfoHolder.setTempExtras(parentActivity.getIntent().getExtras());
        }

        if (incomingBundleChecker.isBundleValid()) {
            incomingBundleChecker.onAccept();
        }

        VoxeetSDK.screenShare().consumeRightsToScreenShare();
    }

    public void onPause() {
        ActivityInfoHolder.setTempAcceptedIncomingActivityOnPause(this);

        //stop fetching stats if any pending
        if (!VoxeetSDK.conference().isLive()) {
            VoxeetSDK.localStats().stopAutoFetch();
        }

        VoxeetSDK.instance().unregister(this);
    }

    public void onNewIntent(Intent intent) {

        incomingBundleChecker = createIncomingBundleChecker(intent);
        if (incomingBundleChecker.isBundleValid()) {
            incomingBundleChecker.onAccept();
        }

        dismissNotification();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull PermissionRefusedEvent event) {
        switch (event.getPermission()) {
            case CAMERA:
                PermissionController.requestPermissions(Manifest.permission.CAMERA).then((ThenPromise<List<PermissionResult>, Boolean>) ok -> {
                    if (!ok.get(0).isGranted)
                        return Promise.reject(new IllegalStateException("no video permission"));
                    return Promise.resolve(true);
                }).then(o -> {
                    //TODO start camera ?
                    Log.d("camera permission ok");
                }).error(Log::e);
        }
    }

    /**
     * Simple method warning if the last call for the camera permission was unsuccessful and the user did prevent any future popup for permission
     *
     * @return a flag indicating if the permission has been permanently refused
     */
    public boolean isCameraPermissionBanned() {
        if (Validate.hasCameraPermissions(parentActivity)) return false;
        if (cameraPermissionPermaBanned) return true;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //    if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) return true;
        //}
        return false;
    }

    /**
     * when the user has been warned by the developers about the permnantly refused error, simply reset back to normal
     */
    public void commitCameraPermissionBannedWarned() {
        cameraPermissionPermaBanned = false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionRefusedEvent.RESULT_CAMERA) {
            if (isPermissionSet(Manifest.permission.CAMERA, permissions, grantResults)) {
                Log.d("onActivityResult: camera is ok now");
                if (VoxeetSDK.conference().isLive()) {
                    VoxeetSDK.conference().startVideo()
                            .then(result -> {
                            })
                            .error(Log::e);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!parentActivity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    cameraPermissionPermaBanned = true;
                }
            }
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return VoxeetSDK.screenShare().onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestScreenSharePermissionEvent event) {
        VoxeetSDK.screenShare().sendUserPermissionRequest(parentActivity);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEvent(ConferenceStatusUpdatedEvent event) {
        incomingBundleChecker.flushIntent();

        switch (event.state) {
            case JOINING:
            case JOINED:
                dismissNotification();
            default: //nothing
        }

        onConferenceState(event);
    }

    /**
     * Get the current voxeet bundle checker
     * <p>
     * usefull to retrieve info about the notification (if such)
     * - user name
     * - avatar url
     * - conference id
     * - user id
     * - external user id
     * - extra bundle (custom)
     *
     * @return a nullable object
     */
    @Nullable
    public IncomingBundleChecker getExtraVoxeetBundleChecker() {
        return incomingBundleChecker;
    }

    private void startService() {
        try {
            if (SystemServiceFactory.hasSDKServiceClass()) {
                Intent intent = new Intent(parentActivity, SystemServiceFactory.getSDKServiceClass());
                parentActivity.startService(intent);
                parentActivity.bindService(intent, sdkConnection, Context.BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private ServiceConnection sdkConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (binder instanceof SDKBinder) {
                try {
                    sdkService = ((SDKBinder<T>) binder).getService();
                    if (null != sdkService) {
                        onSdkServiceAvailable();
                    }
                } catch (Exception e) {
                    Log.e(e);
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            sdkService = null;
        }
    };

    private void dismissNotification() {
        IncomingNotificationHelper.dismiss(parentActivity);
    }

    private boolean isPermissionSet(@NonNull String permission, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (null == permission) return false;

        if (null != permissions && null != grantResults && permissions.length == grantResults.length) {
            for (int i = 0; i < permissions.length; i++) {
                if (permission.equals(permissions[i])) {
                    return PackageManager.PERMISSION_DENIED != grantResults[i];
                }
            }
        }
        return false;
    }

    public void bringBackParent(@NonNull Context context, @Nullable Intent directIntent) {
        onNewIntent(directIntent);

        Intent bring = new Intent(context, parentActivity.getClass());
        bring.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        parentActivity.startActivity(bring);
    }
}

