package com.voxeet.toolkit.activities.notification;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.voxeet.toolkit.activities.VoxeetAppCompatActivity;

/**
 * Factory use to store and make the integration of the VoxeetActivities easier
 */

public class IncomingCallFactory {

    @Nullable
    private static Class<? extends VoxeetAppCompatActivity> sAcceptedIncomingActivityKlass;

    @Nullable
    private static Bundle sAcceptedIncomingActivityExtras;

    /**
     * Store in memory the current activity to call when incoming call is accepted
     *
     * @param klass a nullable klass (null = reset)
     */
    public static void setTempAcceptedIncomingActivity(@Nullable Class<? extends VoxeetAppCompatActivity> klass) {
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
    public static Class<? extends VoxeetAppCompatActivity> getAcceptedIncomingActivityKlass() {
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
}
