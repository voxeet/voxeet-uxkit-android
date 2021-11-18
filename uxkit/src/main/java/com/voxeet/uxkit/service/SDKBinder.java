package com.voxeet.uxkit.service;

import android.os.Binder;
import androidx.annotation.NonNull;

public abstract class SDKBinder<CLASS extends AbstractSDKService> extends Binder {

    @NonNull
    public abstract CLASS getService();
}
