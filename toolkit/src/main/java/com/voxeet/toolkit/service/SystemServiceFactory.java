package com.voxeet.toolkit.service;

import com.voxeet.sdk.utils.Annotate;

import javax.annotation.Nullable;

@Annotate
public class SystemServiceFactory {

    private static Class<? extends AbstractSDKService> sdk_service_klass;

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
}
