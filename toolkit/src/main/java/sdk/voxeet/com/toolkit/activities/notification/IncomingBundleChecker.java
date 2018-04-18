package sdk.voxeet.com.toolkit.activities.notification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import sdk.voxeet.com.toolkit.activities.workflow.VoxeetAppCompatActivity;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import voxeet.com.sdk.factories.VoxeetIntentFactory;
import voxeet.com.sdk.json.UserInfo;

public class IncomingBundleChecker {

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

        mUserName = mIntent.getStringExtra(VoxeetIntentFactory.INVITER_NAME);
        mExternalUserId = mIntent.getStringExtra(VoxeetIntentFactory.INVITER_EXTERNAL_ID);
        mUserId = mIntent.getStringExtra(VoxeetIntentFactory.INVITER_ID);
        mAvatarUrl = mIntent.getStringExtra(VoxeetIntentFactory.INVITER_URL);
        mConferenceId = mIntent.getStringExtra(VoxeetIntentFactory.CONF_ID);
    }

    /**
     * Call accepted invitation
     * <p>
     * this must be called from the activity launched
     * not from the incoming call activity (!)
     */
    public void onAccept() {
        if (mConferenceId != null) {
            UserInfo info = new UserInfo(getUserName(),
                    getExternalUserId(),
                    getAvatarUrl());

            VoxeetToolkit.getInstance()
                    .getConferenceToolkit()
                    .join(mConferenceId, info)
                    .then(new PromiseExec<Boolean, Object>() {
                        @Override
                        public void onCall(@Nullable Boolean result, @NonNull Solver<Object> solver) {
                            //possible callback to set ?
                        }
                    })
                    .error(new ErrorPromise() {
                        @Override
                        public void onError(Throwable error) {
                            error.printStackTrace();
                        }
                    });
        }
    }

    /**
     * Check the current intent
     *
     * @return true if the intent has notification keys
     */
    final public boolean isBundleValid() {
        return mIntent.hasExtra(VoxeetIntentFactory.INVITER_NAME)
                && mIntent.hasExtra(VoxeetIntentFactory.INVITER_EXTERNAL_ID)
                && mIntent.hasExtra(VoxeetIntentFactory.INVITER_ID)
                && mIntent.hasExtra(VoxeetIntentFactory.INVITER_URL)
                && mIntent.hasExtra(VoxeetIntentFactory.CONF_ID);
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
     * @param caller              the non null caller
     * @return a valid intent
     */
    @NonNull
    final public Intent createActivityAccepted(@NonNull Activity caller) {
        Intent intent = new Intent(caller, IncomingCallFactory.getAcceptedIncomingActivityKlass());

        //inject the extras from the current "loaded" activity
        Bundle extras = IncomingCallFactory.getAcceptedIncomingActivityExtras();
        if (null != extras) {
            intent.putExtras(extras);
        }

        intent.putExtra(BUNDLE_EXTRA_BUNDLE, createExtraBundle());

        intent.putExtra(VoxeetIntentFactory.CONF_ID, getConferenceId())
                .putExtra(VoxeetIntentFactory.INVITER_NAME, getUserName())
                .putExtra(VoxeetIntentFactory.INVITER_ID, getExternalUserId())
                .putExtra(VoxeetIntentFactory.INVITER_EXTERNAL_ID, getExternalUserId())
                .putExtra(VoxeetIntentFactory.INVITER_URL, getAvatarUrl());

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
        mIntent.removeExtra(VoxeetIntentFactory.INVITER_ID);
        mIntent.removeExtra(VoxeetIntentFactory.INVITER_EXTERNAL_ID);
        mIntent.removeExtra(VoxeetIntentFactory.CONF_ID);
        mIntent.removeExtra(VoxeetIntentFactory.INVITER_URL);
        mIntent.removeExtra(VoxeetIntentFactory.INVITER_NAME);
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
