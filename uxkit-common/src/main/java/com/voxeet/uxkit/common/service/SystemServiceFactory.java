package com.voxeet.uxkit.common.service;

import android.app.Activity;

import androidx.annotation.Nullable;

public class SystemServiceFactory {

    private static Class<? extends AbstractSDKService> sdk_service_klass;

    @Nullable
    private static Class<? extends Activity> klass;

    @Nullable
    private static Class<? extends Activity> forcedKlass;

    private SystemServiceFactory() {

    }

    /**
     * Register a class extending AbstractSDKService to be used by the VoxeetCommonAppCompatActivityWrapper
     * Note : it supports only one service class. If an application needs to have possibly various services to register,
     * it will need to have 1 service which has multiple sub implementation for now. If specific use cases are known, please
     * warn us
     *
     * @param sdkServiceKlass the final klass to use
     */
    public static void registerSDKServiceClass(@Nullable Class<? extends AbstractSDKService> sdkServiceKlass) {
        sdk_service_klass = sdkServiceKlass;
    }

    public static boolean hasSDKServiceClass() {
        return null != sdk_service_klass;
    }

    /**
     * Get the AbstractSDKService instantiable class to inflate via bindService/startService
     *
     * @return the AbstractSDKService's class
     */
    @Nullable
    public static Class<? extends AbstractSDKService> getSDKServiceClass() {
        return sdk_service_klass;
    }

    /**
     * Get the Class of activity to use (if non null)
     *
     * @return the klass or null
     */
    @Nullable
    public static Class<? extends Activity> getActivityClass() {
        if (null != forcedKlass) return forcedKlass;
        return klass;
    }

    /**
     * Set a klass which will be used by default even if another Activity extending VoxeetCommonAppCompatActivity was used
     * (or any AppCompatActivity which is using the VoxeetCommonAppCompatActivityWrapper's lifecycle
     *
     * @param forcedKlass the klass extending Activity or null to flush it
     */
    public static void setForcedAppCompatActivity(@Nullable Class<? extends Activity> forcedKlass) {
        SystemServiceFactory.forcedKlass = forcedKlass;
    }

    /**
     * Set a temporary klass representing an activity which got its onResume called (via the
     * VoxeetCommonAppCompatActivityWrapper instance either via the VoxeetCommonAppCompatActivity
     * or directly said activity using a wrapper's instance)
     *
     * @param klass the klass extending Activity or null to flush it
     */
    public static void setLastAppCompatActivity(@Nullable Class<? extends Activity> klass) {
        SystemServiceFactory.klass = klass;
    }
}
