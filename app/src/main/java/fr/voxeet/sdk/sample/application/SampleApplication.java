package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.Solver;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.push.center.NotificationCenter;
import com.voxeet.sdk.push.center.management.EnforcedNotificationMode;
import com.voxeet.sdk.push.center.management.NotificationMode;
import com.voxeet.sdk.push.center.management.VersionFilter;
import com.voxeet.sdk.sample.BuildConfig;
import com.voxeet.uxkit.activities.notification.DefaultIncomingCallActivity;
import com.voxeet.uxkit.controllers.ConferenceToolkitController;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.incoming.IncomingFullScreen;
import com.voxeet.uxkit.incoming.IncomingNotification;

import org.greenrobot.eventbus.EventBus;

public class SampleApplication extends MultiDexApplication {
    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleApplication.class.getSimpleName();

    private ParticipantInfo _current_user;

    @Override
    public void onCreate() {
        super.onCreate();

        VoxeetSDK.initialize(
                BuildConfig.CONSUMER_KEY,
                BuildConfig.CONSUMER_SECRET
        ); //can be null - will be removed in a later version

        VoxeetToolkit.initialize(this, EventBus.getDefault())
                .enableOverlay(true);

        //change the overlay used by default
        VoxeetToolkit.instance().getConferenceToolkit().setScreenShareEnabled(true)
                .setDefaultOverlayState(OverlayState.EXPANDED);

        VoxeetToolkit.instance().enable(ConferenceToolkitController.class);

        //the default case of this SDK is to have the SDK with consumerKey and consumerSecret embedded
        onSdkInitialized();
    }

    //when enabling multidex in your app
    /*
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }*/

    /**
     * Select an user, tells the sdk to wether validate or
     * log the selected user
     *
     * @param user_info The user info selected in our UI
     * @return true if it was the first log
     */
    public boolean selectUser(ParticipantInfo user_info) {
        //first case, the user was disconnected
        _current_user = user_info;
        if (_current_user == null) {
            logSelectedUser();
        } else {
            //we have an user
            VoxeetSDK.session()
                    .close()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                            Log.d(TAG, "onCall: user disconnected");
                            logSelectedUser();
                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(Throwable error) {
                            logSelectedUser();
                        }
                    });
        }
        return false;
    }

    /**
     * Call this method to log the current selected user
     */
    public void logSelectedUser() {
        VoxeetSDK.session().open(_current_user)
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {

                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
    }

    public ParticipantInfo getCurrentUser() {
        return _current_user;
    }

    private void onSdkInitialized() {
        //it's possible to use the meta-data in the AndroidManifest to directly control the default incoming activity
        NotificationCenter.instance.register(NotificationMode.FULLSCREEN_INCOMING_CALL, new IncomingFullScreen(DefaultIncomingCallActivity.class));
        NotificationCenter.instance.register(NotificationMode.OVERHEAD_INCOMING_CALL, new IncomingNotification());
        NotificationCenter.instance.setEnforcedNotificationMode(EnforcedNotificationMode.MIXED_INCOMING_CALL);

        //add filter to excluse fullscreen from Android Q
        NotificationCenter.instance.register(NotificationMode.FULLSCREEN_INCOMING_CALL, new VersionFilter(VersionFilter.ALL, 29));
    }
}
