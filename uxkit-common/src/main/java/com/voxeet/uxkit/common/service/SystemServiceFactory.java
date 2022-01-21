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
    public static Class<? extends Activity> getAppCompatActivity() {
        if (null != forcedKlass) return forcedKlass;
        return klass;
    }

    public static void setForcedAppCompatActivity(@Nullable Class<? extends Activity> forcedKlass) {
        SystemServiceFactory.forcedKlass = forcedKlass;
    }

    public static void setLastAppCompatActivity(@Nullable Class<? extends Activity> klass) {
        SystemServiceFactory.klass = klass;
    }
}
