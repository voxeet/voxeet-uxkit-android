package com.voxeet.toolkit.service;

import android.os.Binder;
import android.support.annotation.NonNull;

import com.voxeet.sdk.utils.Annotate;

@Annotate
public abstract class SDKBinder<CLASS extends AbstractSDKService> extends Binder {

    @NonNull
    abstract CLASS getService();
}
