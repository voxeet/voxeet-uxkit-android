package com.voxeet.toolkit.providers.rootview;

import android.app.Application;
import android.support.annotation.NonNull;

import com.voxeet.uxkit.controllers.VoxeetToolkit;

@Deprecated
public class DefaultRootViewProvider extends com.voxeet.uxkit.providers.rootview.DefaultRootViewProvider {

    /**
     * @param application a valid application which be called to obtain events
     * @param toolkit
     */
    public DefaultRootViewProvider(@NonNull Application application, @NonNull VoxeetToolkit toolkit) {
        super(application, toolkit);
    }
}
