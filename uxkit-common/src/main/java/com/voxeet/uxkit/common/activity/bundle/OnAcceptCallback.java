package com.voxeet.uxkit.common.activity.bundle;

import androidx.annotation.NonNull;

import com.voxeet.sdk.services.builders.ConferenceJoinOptions;

/**
 * Give the ability to control and manage options from accepting a conference invitation
 */
public interface OnAcceptCallback {
    void onAcceptConfiguration(@NonNull ConferenceJoinOptions.Builder builder);
}
