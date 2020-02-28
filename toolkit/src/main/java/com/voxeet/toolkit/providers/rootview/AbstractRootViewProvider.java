package com.voxeet.toolkit.providers.rootview;

import android.app.Application;
import android.support.annotation.NonNull;

import com.voxeet.uxkit.controllers.VoxeetToolkit;

/**
 * Abstract class which can manage the state of its parent activity
 * and give a ViewGroup in which incorporate the conference calls
 */
@Deprecated
public abstract class AbstractRootViewProvider extends com.voxeet.uxkit.providers.rootview.AbstractRootViewProvider {
    /**
     * @param application a valid application which be called to obtain events
     * @param toolkit
     */
    protected AbstractRootViewProvider(@NonNull Application application, @NonNull VoxeetToolkit toolkit) {
        super(application, toolkit);
    }
}
