package com.voxeet.toolkit.activities.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.VoxeetSDK;
import com.voxeet.promise.Promise;
import com.voxeet.promise.solve.ErrorPromise;
import com.voxeet.promise.solve.PromiseExec;
import com.voxeet.promise.solve.Solver;
import com.voxeet.sdk.json.ParticipantInfo;
import com.voxeet.sdk.models.Conference;
import com.voxeet.sdk.preferences.VoxeetPreferences;
import com.voxeet.sdk.push.center.management.Constants;
import com.voxeet.sdk.utils.AndroidManifest;
import com.voxeet.toolkit.incoming.factory.IVoxeetActivity;
import com.voxeet.toolkit.incoming.factory.IncomingCallFactory;

public class IncomingBundleChecker {

    private final static String TAG = IncomingBundleChecker.class.getSimpleName();

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

    private IncomingBundleChecker() {
        mIntent = new Intent();
    }

    public IncomingBundleChecker(@NonNull Intent intent, @Nullable IExtraBundleFillerListener filler_listener) {
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
        if (mConferenceId != null) {
            ParticipantInfo info = new ParticipantInfo(getUserName(),
                    getExternalUserId(),
                    getAvatarUrl());

            Log.d(TAG, "onAccept: mConferenceId := " + mConferenceId);
            //join the conference
            Promise<Conference> join = VoxeetSDK.conference().join(mConferenceId);
            //only when error() is called

            Log.d(TAG, "onAccept: isSocketOpen := " + VoxeetSDK.session().isSocketOpen());
            if (!VoxeetSDK.session().isSocketOpen()) {
                ParticipantInfo userInfo = VoxeetPreferences.getSavedUserInfo();

                if (null != userInfo) {
                    VoxeetSDK.session().open(userInfo)
                            .then(new PromiseExec<Boolean, Conference>() {
                                @Override
                                public void onCall(@Nullable Boolean result, @NonNull Solver<Conference> solver) {
                                    Log.d(TAG, "onCall: log user info := " + result);
                                    solver.resolve(join);
                                }
                            })
                            .then(new PromiseExec<Conference, Object>() {
                                @Override
                                public void onCall(@Nullable Conference result, @NonNull Solver<Object> solver) {
                                    Log.d(TAG, "onCall: join conference := " + result);
                                }
                            })
                            .error(new ErrorPromise() {
                                @Override
                                public void onError(@NonNull Throwable error) {
                                    error.printStackTrace();
                                }
                            });
                } else {
                    Log.d(TAG, "onAccept: unable to log the user");
                }
            } else if (VoxeetSDK.conference().isLive()) {
                VoxeetSDK.conference()
                        .leave()
                        .then(new PromiseExec<Boolean, Object>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                                Log.d(TAG, "onCall: previous conference left, joining the new conference");
                                solver.resolve(join.then(new PromiseExec<Conference, Object>() {
                                    @Override
                                    public void onCall(@Nullable Conference result, @NonNull Solver<Object> solver) {
                                        Log.d(TAG, "onCall: resolved 1");
                                    }
                                }));
                            }
                        })
                        .error(new ErrorPromise() {
                            @Override
                            public void onError(@NonNull Throwable error) {
                                error.printStackTrace();
                            }
                        });
            } else {
                join.then(new PromiseExec<Boolean, Object>() {
                    @Override
                    public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                        Log.d(TAG, "onCall: resolved");
                    }
                }).error(new ErrorPromise() {
                    @Override
                    public void onError(@NonNull Throwable error) {
                        error.printStackTrace();
                    }
                });
            }
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
        Class<? extends IVoxeetActivity> klass = IncomingCallFactory.getAcceptedIncomingActivityKlass();
        if (null == klass) {
            Log.d(TAG, "createActivityAccepted: no klass defined ! we'll now try to load from the AndroidManifest");
            String klass_fully_qualified = AndroidManifest.readMetadata(caller, "voxeet_incoming_accepted_class", null);
            if (null != klass_fully_qualified) {
                try {
                    klass = (Class<? extends IVoxeetActivity>) Class.forName(klass_fully_qualified);
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, "createActivityAccepted: ERROR !! IS THE KLASS VALID AND INHERITING VoxeetAppCompatActivity");
                    e.printStackTrace();
                }
            }
        }

        //we have an invalid klass, returning null
        if (null == klass) return null;

        Intent intent = new Intent(caller, klass);

        //inject the extras from the current "loaded" activity
        Bundle extras = IncomingCallFactory.getAcceptedIncomingActivityExtras();
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
        return extra;
    }

    public static interface IExtraBundleFillerListener {

        @Nullable
        Bundle createExtraBundle();
    }
}
