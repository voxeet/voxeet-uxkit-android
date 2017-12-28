package fr.voxeet.sdk.sample.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import fr.voxeet.sdk.sample.BuildConfig;
import fr.voxeet.sdk.sample.Recording;
import fr.voxeet.sdk.sample.activities.CreateConfActivity;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.core.VoxeetSdk;
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

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "firebase...");
        FirebaseApp.initializeApp(this);

        VoxeetToolkit.initialize(this);
        VoxeetToolkit.enableOverlay(true);
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
     * @param user_info The user info selected in our UI
     * @return true if it was the first log
     */
    public boolean selectUser(UserInfo user_info) {
        if(_current_user == null) {
            _current_user = user_info;
            if(VoxeetSdk.getInstance() == null) {
                VoxeetSdk.initialize(this,
                        BuildConfig.CONSUMER_KEY,
                        BuildConfig.CONSUMER_SECRET,
                        _current_user);

                //when we are sure the preferences are now in stable state
                VoxeetPreferences.setDefaultActivity(CreateConfActivity.class.getName());

                VoxeetSdk.getInstance().getEventBus().register(this);
                return true;
            } else {
                VoxeetSdk.getInstance().logUser(_current_user);
            }
            _log_after_closing_event = false;
        } else {
            _current_user = user_info;
            if(VoxeetSdk.getInstance().isSocketOpen()) {
                //logout is called because in our example
                //the main activity will login when triggered
                //TODO a possible improvement is to migrate the logic into a specific layer
                //Application+Activity will be able to use this layer through an Application's accessor

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
        Log.d("MainActivity","logSelectedUser " + _current_user.toString());
        VoxeetSdk.getInstance().logUser(_current_user);
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketStateChangeEvent event) {
        Log.d("SampleApplication", "SocketStateChangeEvent -"+event.message()+"-"+_log_after_closing_event);

        switch(event.message()) {
            case "CLOSING":
                if(_log_after_closing_event) {
                    _log_after_closing_event = false;
                    logSelectedUser();
                }
        }
    }

    public UserInfo getCurrentUser() {
        return _current_user;
    }
}
