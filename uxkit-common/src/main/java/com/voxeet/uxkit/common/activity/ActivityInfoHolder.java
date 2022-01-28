package com.voxeet.uxkit.common.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Use to store and make the integration of the VoxeetActivities easier
 */

public class ActivityInfoHolder {

    @Nullable
    private static Class<? extends AppCompatActivity> sAcceptedIncomingActivityKlass;

    @Nullable
    private static Bundle sAcceptedIncomingActivityExtras;

    @Nullable
    private static VoxeetCommonAppCompatActivityWrapper currentOnResumeActivity;

    /**
     * Store in memory the current activity to call when incoming call is accepted
     *
     * @param klass a nullable klass (null = reset)
     */
    public static void setTempAcceptedIncomingActivity(@Nullable Class<? extends AppCompatActivity> klass) {
        sAcceptedIncomingActivityKlass = klass;
    }

    public static void setTempExtras(@Nullable Bundle extras) {
        sAcceptedIncomingActivityExtras = extras;
    }

    /**
     * Retrieve the in-memory Class to call on call are accepted
     *
     * @return a valid instance of Class<VoxeetAppCompatActivity>
     */
    @Nullable
    public static Class<? extends AppCompatActivity> getAcceptedIncomingActivityKlass() {
        return sAcceptedIncomingActivityKlass;
    }

    /**
     * Retrieve the exact object corresponding to the call activity
     * which has been saved
     *
     * @return a nullable extras
     */
    @Nullable
    public static Bundle getAcceptedIncomingActivityExtras() {
        return sAcceptedIncomingActivityExtras;
    }

    public static void setTempAcceptedIncomingActivityOnResume(VoxeetCommonAppCompatActivityWrapper parentActivity) {
        currentOnResumeActivity = parentActivity;
    }

    public static void setTempAcceptedIncomingActivityOnPause(VoxeetCommonAppCompatActivityWrapper parentActivity) {
        if (currentOnResumeActivity == parentActivity) {
            currentOnResumeActivity = null;
        }
    }

    public static VoxeetCommonAppCompatActivityWrapper getCurrentAcceptedIncomingCallActivity() {
        return currentOnResumeActivity;
    }
}
