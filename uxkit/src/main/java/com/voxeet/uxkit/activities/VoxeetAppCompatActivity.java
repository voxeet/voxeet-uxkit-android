package com.voxeet.uxkit.activities;

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
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.services.screenshare.RequestScreenSharePermissionEvent;
import com.voxeet.sdk.utils.Validate;
import com.voxeet.uxkit.activities.notification.IncomingBundleChecker;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.incoming.factory.IVoxeetActivity;
import com.voxeet.uxkit.incoming.factory.IncomingCallFactory;
import com.voxeet.uxkit.service.AbstractSDKService;
import com.voxeet.uxkit.service.SDKBinder;
import com.voxeet.uxkit.service.SystemServiceFactory;
import com.voxeet.uxkit.utils.IncomingNotificationHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public class VoxeetAppCompatActivity extends AppCompatActivity implements IVoxeetActivity {

    private static final ShortLogger Log = UXKitLogger.createLogger(VoxeetAppCompatActivity.class);
    private IncomingBundleChecker mIncomingBundleChecker;

    @Nullable
    private AbstractSDKService sdkService;

    /**
     * Flag set to true when the last request for camera permission failed, use the commit method to restore it to false
     * after the adequate warning has been made to the user
     */
    private boolean _camera_permission_permnantly_banned = false;

    public VoxeetAppCompatActivity() {
        super();
    }


    @Nullable
    public AbstractSDKService getSdkService() {
        return sdkService;
    }

    public void onSdkServiceAvailable() {
        //nothing, override to change
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create a check incoming call
        mIncomingBundleChecker = new IncomingBundleChecker(getIntent(), null);

        startService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemServiceFactory.setLastAppCompatActivity(this.getClass());
        startService();

        VoxeetSDK.instance().register(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this); //registering this activity
        }

        if (canBeRegisteredToReceiveCalls()) {
            IncomingCallFactory.setTempAcceptedIncomingActivity(this.getClass());
            IncomingCallFactory.setTempExtras(getIntent().getExtras());
        }

        if (mIncomingBundleChecker.isBundleValid()) {
            mIncomingBundleChecker.onAccept();
        }

        VoxeetSDK.screenShare().consumeRightsToScreenShare();

        if (null != VoxeetToolkit.instance()) {
            //to prevent uninitialized toolkit but ... it's highly recommended for future releases to always init
            VoxeetToolkit.instance().getConferenceToolkit().forceReattach();
        }
    }

    @Override
    protected void onPause() {
        //stop fetching stats if any pending
        if (!VoxeetSDK.conference().isLive()) {
            VoxeetSDK.localStats().stopAutoFetch();
        }

        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mIncomingBundleChecker = new IncomingBundleChecker(intent, null);
        if (mIncomingBundleChecker.isBundleValid()) {
            mIncomingBundleChecker.onAccept();
        }

        dismissNotification();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull PermissionRefusedEvent event) {
        switch (event.getPermission()) {
            case CAMERA:
            case MICROPHONE:
                Validate.requestMandatoryPermissions(this,
                        event.getPermission().getPermissions(),
                        event.getPermission().getRequestCode());
                break;
        }
    }

    /**
     * Simple method warning if the last call for the camera permission was unsuccessful and the user did prevent any future popup for permission
     *
     * @return a flag indicating if the permission has been permanently refused
     */
    public boolean isCameraPermissionBanned() {
        if (Validate.hasCameraPermissions(this)) return false;
        if (_camera_permission_permnantly_banned) return true;
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //    if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) return true;
        //}
        return false;
    }

    /**
     * when the user has been warned by the developers about the permnantly refused error, simply reset back to normal
     */
    public void commitCameraPermissionBannedWarned() {
        _camera_permission_permnantly_banned = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionRefusedEvent.RESULT_CAMERA) {
            if (isPermissionSet(Manifest.permission.CAMERA, permissions, grantResults)) {
                Log.d( "onActivityResult: camera is ok now");
                if (VoxeetSDK.conference().isLive()) {
                    VoxeetSDK.conference().startVideo()
                            .then(result -> {
                            })
                            .error(Log::e);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    _camera_permission_permnantly_banned = true;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean managed = VoxeetSDK.screenShare().onActivityResult(requestCode, resultCode, data);

        if (!managed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestScreenSharePermissionEvent event) {
        VoxeetSDK.screenShare().sendUserPermissionRequest(this);
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEvent(ConferenceStatusUpdatedEvent event) {
        mIncomingBundleChecker.flushIntent();

        switch (event.state) {
            case JOINING:
            case JOINED:
                dismissNotification();
            default: //nothing
        }
        onConferenceState(event);
    }

    protected void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event) {

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
    protected IncomingBundleChecker getExtraVoxeetBundleChecker() {
        return mIncomingBundleChecker;
    }

    /**
     * Method called during the onResume of this
     *
     * @return true by default, override to change behaviour
     */
    protected boolean canBeRegisteredToReceiveCalls() {
        return true;
    }

    private void startService() {
        try {
            if (SystemServiceFactory.hasSDKServiceClass()) {
                Intent intent = new Intent(this, SystemServiceFactory.getSDKServiceClass());
                startService(intent);
                bindService(intent, sdkConnection, Context.BIND_AUTO_CREATE);
            }
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private ServiceConnection sdkConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (binder instanceof SDKBinder) {
                try {
                    sdkService = ((SDKBinder) binder).getService();
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
        IncomingNotificationHelper.dismiss(this);
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
}

