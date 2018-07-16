package fr.voxeet.sdk.sample.application;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.BuildConfig;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.activities.IncomingCallActivity;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.utils.EventDebugger;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.core.preferences.VoxeetPreferences;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by RomainBenmansour on 06,April,2016
 */
public class SampleApplication extends MultiDexApplication {
    private static final int ONE_MINUTE = 60 * 1000;

    private static final String TAG = SampleApplication.class.getSimpleName();

    @NonNull
    private List<Recording> recordedConference = new ArrayList<>();
    private UserInfo _current_user;
    private EventDebugger mEventDebugger;

    @Override
    public void onCreate() {
        super.onCreate();


        mEventDebugger = new EventDebugger();
        mEventDebugger.register();

        VoxeetToolkit.initialize(this, EventBus.getDefault());
        VoxeetToolkit.getInstance().enableOverlay(true);

        //change the overlay used by default
        VoxeetToolkit.getInstance().getConferenceToolkit().setDefaultOverlayState(OverlayState.EXPANDED);
        VoxeetToolkit.getInstance().getReplayMessageToolkit().setDefaultOverlayState(OverlayState.EXPANDED);

        //prevent close from
        //init the SDK
        VoxeetSdk.initialize(this,
                BuildConfig.CONSUMER_KEY,
                BuildConfig.CONSUMER_SECRET,
                _current_user); //can be null - will be removed in a later version

        VoxeetSdk.getInstance().getConferenceService().setTimeOut(ONE_MINUTE);

        VoxeetPreferences.setDefaultActivity(IncomingCallActivity.class.getCanonicalName());
        //register the Application and add at least one subscriber
        VoxeetSdk.getInstance().register(this, this);
    }

    public void saveRecordingConference(Recording newRecording) {
        for (Recording recording : recordedConference) {
            if (recording.conferenceId.equalsIgnoreCase(newRecording.conferenceId))
                return;
        }

        recordedConference.add(newRecording);
    }

    @NonNull
    public List<Recording> getRecordedConferences() {
        return recordedConference;
    }

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
        if (_current_user == null) {
            logSelectedUser();
        } else {
            //we have an user
            VoxeetSdk.getInstance()
                    .logout()
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
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
        Log.d("MainActivity", "logSelectedUser " + _current_user.toString());
        VoxeetSdk.getInstance().logUser(_current_user);
    }

    public UserInfo getCurrentUser() {
        return _current_user;
    }
}
