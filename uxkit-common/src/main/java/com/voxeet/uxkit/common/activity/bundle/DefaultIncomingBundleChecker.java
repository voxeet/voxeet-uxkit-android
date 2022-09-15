package com.voxeet.uxkit.common.activity.bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ThenPromise;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.preferences.VoxeetPreferences;
import com.voxeet.sdk.push.center.management.Constants;
import com.voxeet.sdk.services.builders.ConferenceJoinOptions;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.activity.ActivityInfoHolder;
import com.voxeet.uxkit.common.logging.ShortLogger;

public class DefaultIncomingBundleChecker implements IncomingBundleChecker {

    private final static ShortLogger Log = UXKitLogger.createLogger(DefaultIncomingBundleChecker.class.getSimpleName());

    private final static String BUNDLE_EXTRA_BUNDLE = "BUNDLE_EXTRA_BUNDLE";

    @Nullable
    private IExtraBundleFillerListener mFillerListener;

    @NonNull
    private Intent mIntent;

    @Nullable
    private String mUserName;

    @Nullable
    private String mUserId;

    @Nullable
    private String mExternalUserId;

    @Nullable
    private String mAvatarUrl;

    @Nullable
    private String mConferenceId;

    private DefaultIncomingBundleChecker() {
        mIntent = new Intent();
    }

    public DefaultIncomingBundleChecker(@NonNull Intent intent, @Nullable IExtraBundleFillerListener filler_listener) {
        this();
        mFillerListener = filler_listener;
        mIntent = intent;

        mUserName = mIntent.getStringExtra(Constants.INVITER_NAME);
        mExternalUserId = mIntent.getStringExtra(Constants.INVITER_EXTERNAL_ID);
        mUserId = mIntent.getStringExtra(Constants.INVITER_ID);
        mAvatarUrl = mIntent.getStringExtra(Constants.INVITER_URL);
        mConferenceId = mIntent.getStringExtra(Constants.CONF_ID);
    }

    /**
     * Call accepted invitation
     * <p>
     * this must be called from the activity launched
     * not from the incoming call activity (!)
     */
    public void onAccept() {
        if (mConferenceId == null) {
            Log.d("onAccept: the conferenceId is invalid, dismissing...");
            return;
        }
        Log.d("onAccept: mConferenceId := " + mConferenceId);
        //join the conference
        Conference conference = VoxeetSDK.conference().getConference(mConferenceId);
        Promise<Conference> join = VoxeetSDK.conference().join(new ConferenceJoinOptions.Builder(conference).build());
        //only when error() is called

        Log.d("onAccept: isSocketOpen := " + VoxeetSDK.session().isOpen());
        if (!VoxeetSDK.session().isOpen()) {
            ParticipantInfo userInfo = VoxeetPreferences.getSavedUserInfo();

            if (null != userInfo) {
                VoxeetSDK.session().open(userInfo)
                        .then((ThenPromise<Boolean, Conference>) aBoolean -> join)
                        .then(result -> {
                            Log.d("onCall: join conference := " + result);
                        })
                        .error(Log::e);
            } else {
                Log.d("onAccept: unable to log the user");
            }
        } else if (VoxeetSDK.conference().isLive()) {
            VoxeetSDK.conference()
                    .leave()
                    .then((ThenPromise<Boolean, Conference>) aBoolean -> join)
                    .then(joined -> {
                        Log.d("onCall: resolved 1");
                    })
                    .error(Log::e);
        } else {
            join.then(result -> {
                Log.d("onCall: resolved");
            }).error(Log::e);
        }
    }

    /**
     * Check the current intent
     *
     * @return true if the intent has notification keys
     */
    final public boolean isBundleValid() {
        return null != mIntent && mIntent.hasExtra(Constants.INVITER_NAME)
                && mIntent.hasExtra(Constants.INVITER_EXTERNAL_ID)
                && mIntent.hasExtra(Constants.INVITER_ID)
                //&& mIntent.hasExtra(Constants.INVITER_URL)
                && mIntent.hasExtra(Constants.CONF_ID);
    }

    @Nullable
    final public String getExternalUserId() {
        return mExternalUserId;
    }

    @Nullable
    final public String getUserId() {
        return mUserId;
    }

    @Nullable
    final public String getUserName() {
        return mUserName;
    }

    @Nullable
    final public String getAvatarUrl() {
        return mAvatarUrl;
    }

    @Nullable
    final public String getConferenceId() {
        return mConferenceId;
    }

    @Nullable
    final public Bundle getExtraBundle() {
        return mIntent.getBundleExtra(BUNDLE_EXTRA_BUNDLE);
    }

    final public boolean isSameConference(String conferenceId) {
        return mConferenceId != null && mConferenceId.equals(conferenceId);
    }


    /**
     * Create an intent to start the activity you want after an "accept" call
     *
     * @param caller the non null caller
     * @return a valid intent
     */
    @NonNull
    final public Intent createActivityAccepted(@NonNull Activity caller) {
        Class<? extends AppCompatActivity> klass = ActivityInfoHolder.getAcceptedIncomingActivityKlass();
        if (null == klass) {
            Log.d("createActivityAccepted: no klass defined ! we'll now try to load from the AndroidManifest");
            String klass_fully_qualified = AndroidManifest.readMetadata(caller, "voxeet_incoming_accepted_class", null);
            if (null != klass_fully_qualified) {
                try {
                    klass = (Class<? extends AppCompatActivity>) Class.forName(klass_fully_qualified);
                    Log.d("createActivityAccepted: " + klass.getSimpleName() + " obtained");
                } catch (ClassNotFoundException e) {
                    Log.e("createActivityAccepted: ERROR !! IS THE KLASS VALID AND INHERITING VoxeetAppCompatActivity", e);
                }
            }
        }

        //we have an invalid klass, returning null
        if (null == klass) return null;

        Intent intent = new Intent(caller, klass);

        //inject the extras from the current "loaded" activity
        Bundle extras = ActivityInfoHolder.getAcceptedIncomingActivityExtras();
        if (null != extras) {
            intent.putExtras(extras);
        }

        intent.putExtra(BUNDLE_EXTRA_BUNDLE, createExtraBundle());

        intent.putExtra(Constants.CONF_ID, getConferenceId())
                .putExtra(Constants.INVITER_NAME, getUserName())
                .putExtra(Constants.INVITER_ID, getExternalUserId())
                .putExtra(Constants.INVITER_EXTERNAL_ID, getExternalUserId())
                .putExtra(Constants.INVITER_URL, getAvatarUrl());

        //deprecated
        intent.putExtra("join", true);
        intent.putExtra("callMode", 0x0001);

        //TODO check usefullness
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);

        return intent;
    }

    /**
     * Remove the specific bundle call keys from the intent
     * Needed if you do not want to pass over and over in this method
     * in onResume/onPause lifecycle
     */
    public void flushIntent() {
        Log.d("flushIntent");
        mIntent.removeExtra(Constants.INVITER_ID);
        mIntent.removeExtra(Constants.INVITER_EXTERNAL_ID);
        mIntent.removeExtra(Constants.CONF_ID);
        mIntent.removeExtra(Constants.INVITER_URL);
        mIntent.removeExtra(Constants.INVITER_NAME);
    }

    @NonNull
    public Bundle createExtraBundle() {
        Bundle extra = null;

        if (null != mFillerListener)
            extra = mFillerListener.createExtraBundle();

        if (null == extra) extra = new Bundle();
        Log.d("createExtraBundle created");
        return extra;
    }

}
