package com.voxeet.toolkit;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.voxeet.android.media.Media;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.json.UserInfo;

/**
 * Voxeet implementation for Cordova
 */

public class VoxeetCordova extends CordovaPlugin {

    private final Handler mHandler;

    public VoxeetCordova() {
        super();
        mHandler = new Handler(Looper.getMainLooper());

        Promise.setHandler(mHandler);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action != null) {
            switch (action) {
                case "initialize":
                    initialize(args.getString(0),
                            args.getString(1));
                    callbackContext.success();
                    break;
                case "openSession":
                    openSession(args.getString(0),
                            args.getString(1),
                            args.getString(2),
                            callbackContext);
                    break;
                case "closeSession":
                    closeSession(callbackContext);
                    break;
                case "startConference":
                    String confId = args.getString(0);
                    JSONArray array = args.getJSONArray(1);

                    List<UserInfo> participants = new ArrayList<>();
                    if(null != array) {
                        JSONObject object = null;
                        int index = 0;
                        while(index < array.length()) {
                            object = array.getJSONObject(index);

                            participants.add(new UserInfo(object.getString("name"),
                                    object.getString("externalId"),
                                    object.getString("avatarUrl")));
                        }
                    }

                    startConference(confId, participants, callbackContext);
                    break;
                case "stopConference":
                    stopConference(callbackContext);
                    break;
                case "appearMaximized":
                    appearMaximized(args.getBoolean(0));
                    callbackContext.success();
                    break;
                case "defaultBuildInSpeaker":
                    defaultBuiltInSpeaker(args.getBoolean(0));
                    callbackContext.success();
                    break;
                case "screenAutoLock":
                    screenAutoLock(args.getBoolean(0));
                    callbackContext.success();
                    break;
                default:
                    return false;
            }

            return true; //default return false - so true is ok
        }
        return false;
    }


    private void initialize(String consumerKey,
                            String consumerSecret) {
        Application application = (Application) this.cordova.getActivity().getApplicationContext();

        VoxeetSdk.initialize(application,
                consumerKey, consumerSecret, null);

    }

    private void openSession(String userId,
                             String participantName,
                             String avatarUrl,
                             CallbackContext cb) {
        UserInfo userInfo = new UserInfo(participantName, userId, avatarUrl);

        VoxeetSdk.getInstance().logUser(userInfo);
    }

    private void closeSession(final CallbackContext cb) {
        VoxeetSdk.getInstance()
                .logout()
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean aBoolean, @NonNull Solver<Object> solver) {
                        cb.success();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        cb.error("Error while logging out with the server");
                    }
                });
    }

    private void startConference(String conferenceId,
                                 final List<UserInfo> participants,
                                 final CallbackContext cb) {
        VoxeetSdk.getInstance()
                .getConferenceService()
                .join(conferenceId)
                .then(new PromiseExec<Boolean, List<ConferenceRefreshedEvent>>() {
                    @Override
                    public void onCall(@Nullable Boolean aBoolean, @NonNull final Solver<List<ConferenceRefreshedEvent>> solver) {
                        solver.resolve(VoxeetToolkit.getInstance()
                                .getConferenceToolkit()
                                .invite(participants));
                    }
                })
                .then(new PromiseExec<List<ConferenceRefreshedEvent>, Object>() {
                    @Override
                    public void onCall(@Nullable List<ConferenceRefreshedEvent> conferenceRefreshedEvents, @NonNull Solver<Object> solver) {
                        cb.success();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        cb.error("Error whilte initializing the conference");
                    }
                });
    }

    private void stopConference(final CallbackContext cb) {
        VoxeetSdk.getInstance()
                .getConferenceService()
                .leave()
                .then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean bool, @NonNull Solver<Object> solver) {
                        cb.success();
                    }
                })
                .error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable throwable) {
                        cb.error("Error while leaving");
                    }
                });
    }

    private void add(/* participant */) {
        //TODO not available in the current SDK
    }

    private void update(/* participant */) {
        //TODO not available in the current SDK
    }

    private void remove(/* participant */) {
        //TODO not available in the current SDK
    }

    private void appearMaximized(final Boolean enabled) {
        VoxeetToolkit.getInstance()
                .getConferenceToolkit()
                .setDefaultOverlayState(enabled ? OverlayState.EXPANDED
                        : OverlayState.MINIMIZED);
    }

    private void defaultBuiltInSpeaker(final boolean enabled) {
        Media.AudioRoute route = Media.AudioRoute.ROUTE_PHONE;
        if (enabled) route = Media.AudioRoute.ROUTE_SPEAKER;

        VoxeetSdk.getInstance().getConferenceService()
                .setAudioRoute(route);
    }

    private void screenAutoLock(Boolean enabled) {
        //TODO not available in the current sdk
    }
}
