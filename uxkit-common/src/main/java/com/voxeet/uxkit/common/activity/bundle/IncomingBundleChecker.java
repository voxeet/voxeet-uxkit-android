package com.voxeet.uxkit.common.activity.bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IncomingBundleChecker {

    void onAccept();

    void onAccept(@Nullable OnAcceptCallback onAcceptCallback);

    boolean isBundleValid();

    @Nullable
    String getExternalUserId();

    @Nullable
    String getUserId();

    @Nullable
    String getUserName();

    @Nullable
    String getConferenceAlias();

    @Nullable
    String getAvatarUrl();

    @Nullable
    String getConferenceId();

    @Nullable
    Bundle getExtraBundle();

    boolean isSameConference(String conferenceId);

    /**
     * Create an intent to start the activity you want after an "accept" call
     *
     * @param caller the non null caller
     * @return a valid intent
     */
    @NonNull
    Intent createActivityAccepted(@NonNull Activity caller);

    /**
     * Remove the specific bundle call keys from the intent
     * Needed if you do not want to pass over and over in this method
     * in onResume/onPause lifecycle
     */
    void flushIntent();

    @NonNull
    Bundle createExtraBundle();

}
