package com.voxeet.toolkit.service;

import android.app.Service;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.utils.Annotate;

import javax.annotation.Nullable;

@Annotate
public abstract class AbstractSDKService<BINDER extends SDKBinder> extends Service {

    @Nullable
    public final VoxeetSdk sdk() {
        return VoxeetSdk.instance();
    }

    @NonNull
    public abstract BINDER onBind(@NonNull Intent intent);
}
