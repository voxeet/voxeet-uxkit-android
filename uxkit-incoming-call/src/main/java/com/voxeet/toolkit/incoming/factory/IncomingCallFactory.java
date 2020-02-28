package com.voxeet.toolkit.incoming.factory;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.voxeet.uxkit.incoming.factory.IVoxeetActivity;

/**
 * Factory use to store and make the integration of the VoxeetActivities easier
 */
@Deprecated
public class IncomingCallFactory {

    /**
     * Store in memory the current activity to call when incoming call is accepted
     *
     * @param klass a nullable klass (null = reset)
     */
    public static void setTempAcceptedIncomingActivity(@Nullable Class<? extends IVoxeetActivity> klass) {
        com.voxeet.uxkit.incoming.factory.IncomingCallFactory.setTempAcceptedIncomingActivity(klass);
    }

    public static void setTempExtras(@Nullable Bundle extras) {
        com.voxeet.uxkit.incoming.factory.IncomingCallFactory.setTempExtras(extras);
    }

    /**
     * Retrieve the in-memory Class to call on call are accepted
     *
     * @return a valid instance of Class<VoxeetAppCompatActivity>
     */
    @Nullable
    public static Class<? extends com.voxeet.uxkit.incoming.factory.IVoxeetActivity> getAcceptedIncomingActivityKlass() {
        return com.voxeet.uxkit.incoming.factory.IncomingCallFactory.getAcceptedIncomingActivityKlass();
    }

    /**
     * Retrieve the exact object corresponding to the call activity
     * which has been saved
     *
     * @return a nullable extras
     */
    @Nullable
    public static Bundle getAcceptedIncomingActivityExtras() {
        return com.voxeet.uxkit.incoming.factory.IncomingCallFactory.getAcceptedIncomingActivityExtras();
    }
}
