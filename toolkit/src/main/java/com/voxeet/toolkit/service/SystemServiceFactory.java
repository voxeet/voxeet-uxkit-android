package com.voxeet.toolkit.service;

import android.support.annotation.Nullable;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.activities.VoxeetAppCompatActivity;

@Annotate
public class SystemServiceFactory {

    private static Class<? extends AbstractSDKService> sdk_service_klass;

    @Nullable
    private static Class<? extends VoxeetAppCompatActivity> klass;

    @Nullable
    private static Class<? extends VoxeetAppCompatActivity> forcedKlass;

    private SystemServiceFactory() {

    }

    public static void registerSDKServiceClass(@Nullable Class<? extends AbstractSDKService> sdkServiceKlass) {
        sdk_service_klass = sdkServiceKlass;
    }

    public static boolean hasSDKServiceClass() {
        return null != sdk_service_klass;
    }

    @Nullable
    public static Class<? extends AbstractSDKService> getSDKServiceClass() {
        return sdk_service_klass;
    }

    @Nullable
    public static Class<? extends VoxeetAppCompatActivity> getAppCompatActivity() {
        if (null != forcedKlass) return forcedKlass;
        return klass;
    }

    public static void setForcedAppCompatActivity(@Nullable Class<? extends VoxeetAppCompatActivity> forcedKlass) {
        SystemServiceFactory.forcedKlass = forcedKlass;
    }

    public static void setLastAppCompatActivity(@Nullable Class<? extends VoxeetAppCompatActivity> klass) {
        SystemServiceFactory.klass = klass;
    }
}
