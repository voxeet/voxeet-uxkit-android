package fr.voxeet.sdk.sample.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import fr.voxeet.sdk.sample.BuildConfig;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.activities.CreateConfActivity;
import sdk.voxeet.com.toolkit.controllers.AbstractConferenceToolkitController;
import sdk.voxeet.com.toolkit.controllers.ConferenceToolkitController;
import sdk.voxeet.com.toolkit.controllers.ReplayMessageToolkitController;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.error.SdkLogoutErrorEvent;
import voxeet.com.sdk.events.success.SdkLogoutSuccessEvent;
import voxeet.com.sdk.events.success.SocketStateChangeEvent;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by RomainBenmansour on 06,April,2016
 */
public class SampleApplication extends Application {

    private static final String TAG = SampleApplication.class.getSimpleName();

    @NonNull
    private List<Recording> recordedConference = new ArrayList<>();
    private UserInfo _current_user;
    private boolean _log_after_closing_event;
    private AbstractConferenceToolkitController mVoxeetToolkitConferenceController;

    @Override
    public void onCreate() {
        super.onCreate();


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

        //register the Application and add at least one subscriber
        VoxeetSdk.getInstance().register(this, this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
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
        if (_current_user == null) {
            _current_user = user_info;
            logSelectedUser();
            _log_after_closing_event = false;
        } else {
            //we have an user
            _current_user = user_info;
            if (VoxeetSdk.getInstance().isSocketOpen()) {
                _log_after_closing_event = true;
                VoxeetSdk.getInstance().logout();
            } else {
                logSelectedUser();
            }
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SdkLogoutSuccessEvent event) {
        afterLogoutEvent();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SdkLogoutErrorEvent event) {
        afterLogoutEvent();
    }

    private void afterLogoutEvent() {
        if (_log_after_closing_event) {
            _log_after_closing_event = false;
            logSelectedUser();
        }
    }

    public UserInfo getCurrentUser() {
        return _current_user;
    }
}
