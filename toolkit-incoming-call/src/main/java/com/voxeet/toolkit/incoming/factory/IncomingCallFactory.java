package com.voxeet.toolkit.incoming.factory;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Factory use to store and make the integration of the VoxeetActivities easier
 */

public class IncomingCallFactory {

    @Nullable
    private static Class<? extends IVoxeetActivity> sAcceptedIncomingActivityKlass;

    @Nullable
    private static Bundle sAcceptedIncomingActivityExtras;

    /**
     * Store in memory the current activity to call when incoming call is accepted
     *
     * @param klass a nullable klass (null = reset)
     */
    public static void setTempAcceptedIncomingActivity(@Nullable Class<? extends IVoxeetActivity> klass) {
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
    public static Class<? extends IVoxeetActivity> getAcceptedIncomingActivityKlass() {
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
