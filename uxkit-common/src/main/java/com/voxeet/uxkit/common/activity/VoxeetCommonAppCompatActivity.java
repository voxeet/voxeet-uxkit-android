package com.voxeet.uxkit.common.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.events.error.PermissionRefusedEvent;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.services.screenshare.RequestScreenSharePermissionEvent;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.activity.bundle.DefaultIncomingBundleChecker;
import com.voxeet.uxkit.common.activity.bundle.IncomingBundleChecker;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;

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
public class VoxeetCommonAppCompatActivity<T extends AbstractSDKService<? extends SDKBinder<T>>> extends AppCompatActivity {

    private static final ShortLogger Log = UXKitLogger.createLogger(VoxeetCommonAppCompatActivity.class);
    private final VoxeetCommonAppCompatActivityWrapper<T> wrapper;

    public VoxeetCommonAppCompatActivity() {
        super();
        wrapper = new VoxeetCommonAppCompatActivityWrapper<T>(this) {
            @Override
            protected void onSdkServiceAvailable() {
                VoxeetCommonAppCompatActivity.this.onSdkServiceAvailable();
            }

            @Override
            protected void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event) {
                VoxeetCommonAppCompatActivity.this.onConferenceState(event);
            }

            @Override
            protected boolean canBeRegisteredToReceiveCalls() {
                return VoxeetCommonAppCompatActivity.this.canBeRegisteredToReceiveCalls();
            }

            @Override
            public IncomingBundleChecker createIncomingBundleChecker(Intent intent) {
                return VoxeetCommonAppCompatActivity.this.createIncomingBundleChecker(intent);
            }
        };
    }

    private IncomingBundleChecker createIncomingBundleChecker(@NonNull Intent intent) {
        return new DefaultIncomingBundleChecker(intent, null);
    }

    @Nullable
    public T getSdkService() {
        return wrapper.getSdkService();
    }

    public void onSdkServiceAvailable() {
        //nothing, override to change
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wrapper.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        VoxeetSDK.instance().register(this);
        wrapper.onResume();
    }

    @Override
    protected void onPause() {
        wrapper.onPause();
        VoxeetSDK.instance().unregister(this);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        wrapper.onNewIntent(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull PermissionRefusedEvent event) {
        //no-op keeping it for retrocompatibility
    }

    /**
     * Simple method warning if the last call for the camera permission was unsuccessful and the user did prevent any future popup for permission
     *
     * @return a flag indicating if the permission has been permanently refused
     */
    public boolean isCameraPermissionBanned() {
        return wrapper.isCameraPermissionBanned();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean managed = wrapper.onActivityResult(requestCode, resultCode, data);

        if (!managed) super.onActivityResult(requestCode, resultCode, data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RequestScreenSharePermissionEvent event) {
        //no-op keeping this for retrocompatibility purposes
    }

    protected void onConferenceState(@NonNull ConferenceStatusUpdatedEvent event) {
        //nothing, override to manage
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * Specific event used to manage the current "incoming" call feature
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onEvent(ConferenceStatusUpdatedEvent event) {
        //no-op keeping for retrocompatibility purposes
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
        return wrapper.getExtraVoxeetBundleChecker();
    }

    /**
     * Method called during the onResume of this
     *
     * @return true by default, override to change behaviour
     */
    protected boolean canBeRegisteredToReceiveCalls() {
        return true;
    }
}

