package com.voxeet.toolkit.service;

import android.support.annotation.Nullable;

@Deprecated
public class SystemServiceFactory {

    private SystemServiceFactory() {

    }

    public static void registerSDKServiceClass(@Nullable Class<? extends com.voxeet.uxkit.service.AbstractSDKService> sdkServiceKlass) {
        com.voxeet.uxkit.service.SystemServiceFactory.registerSDKServiceClass(sdkServiceKlass);
    }

    public static boolean hasSDKServiceClass() {
        return com.voxeet.uxkit.service.SystemServiceFactory.hasSDKServiceClass();
    }

    @Nullable
    public static Class<? extends com.voxeet.uxkit.service.AbstractSDKService> getSDKServiceClass() {
        return com.voxeet.uxkit.service.SystemServiceFactory.getSDKServiceClass();
    }

    @Nullable
    public static Class<? extends com.voxeet.uxkit.activities.VoxeetAppCompatActivity> getAppCompatActivity() {
        return com.voxeet.uxkit.service.SystemServiceFactory.getAppCompatActivity();
    }

    public static void setForcedAppCompatActivity(@Nullable Class<? extends com.voxeet.uxkit.activities.VoxeetAppCompatActivity> forcedKlass) {
        com.voxeet.uxkit.service.SystemServiceFactory.setForcedAppCompatActivity(forcedKlass);
    }

    public static void setLastAppCompatActivity(@Nullable Class<? extends com.voxeet.uxkit.activities.VoxeetAppCompatActivity> klass) {
        com.voxeet.uxkit.service.SystemServiceFactory.setLastAppCompatActivity(klass);
    }
}
