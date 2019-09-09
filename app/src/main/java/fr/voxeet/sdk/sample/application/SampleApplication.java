package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.voxeet.push.firebase.FirebaseController;
import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.json.UserInfo;
import com.voxeet.sdk.sample.BuildConfig;
import com.voxeet.sdk.sample.R;
import com.voxeet.toolkit.activities.notification.DefaultIncomingCallActivity;
import com.voxeet.toolkit.configuration.Overlay;
import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.implementation.overlays.OverlayState;

import org.greenrobot.eventbus.EventBus;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;

public class SampleApplication extends MultiDexApplication {
    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleApplication.class.getSimpleName();

    private UserInfo _current_user;
    private boolean sdkInitialized;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate: starting Voxeet Sample");

        sdkInitialized = false;

        VoxeetToolkit.initialize(this, EventBus.getDefault())
                .enableOverlay(true);

        FirebaseController.getInstance().enable(true);

        //change the overlay used by default
        VoxeetToolkit.getInstance().getConferenceToolkit().setScreenShareEnabled(true)
                .setDefaultOverlayState(OverlayState.EXPANDED);

        //the default case of this SDK is to have the SDK with consumerKey and consumerSecret embedded
        uniqueInitializeSDK().then(new PromiseExec<Boolean, Object>() {
            @Override
            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                Log.d(TAG, "onCall: SDK initialized using keys");
            }
        }).error(new ErrorPromise() {
            @Override
            public void onError(@NonNull Throwable error) {
                error.printStackTrace();
            }
        });
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
    public boolean selectUser(UserInfo user_info) {
        //first case, the user was disconnected
        _current_user = user_info;
        if (_current_user == null || !sdkInitialized) {
            logSelectedUser();
        } else {
            //we have an user
            VoxeetSdk.user()
                    .logout()
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
        VoxeetSdk.user().login(_current_user)
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

    public UserInfo getCurrentUser() {
        return _current_user;
    }


    /**
     * For the example, this method will return a promise compatible with the two different type of usage
     * of this SDK
     *
     * @return a promise in case of the OAuth use case, null otherwise
     */
    @NonNull
    public Promise<Boolean> uniqueInitializeSDK() {
        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                VoxeetSdk.initialize(
                        BuildConfig.CONSUMER_KEY,
                        BuildConfig.CONSUMER_SECRET); //can be null - will be removed in a later version

                onSdkInitialized();
                solver.resolve(true);
            }
        });
    }

    private void onSdkInitialized() {
        VoxeetSdk.conference().setTimeOut(ONE_MINUTE);

        //it's possible to use the meta-data in the AndroidManifest to directly control the default incoming activity
        VoxeetPreferences.setDefaultActivity(DefaultIncomingCallActivity.class.getCanonicalName());
        VoxeetSdk.instance().register( this);

        sdkInitialized = true;
    }
}
