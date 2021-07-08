package com.voxeet.toolkit.controllers;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

/**
 * Simple VoxeetToolkit implementation
 */
@Deprecated
public class VoxeetToolkit extends com.voxeet.uxkit.controllers.VoxeetToolkit {

    /**
     * This function initializes the UI Toolkit SDK. It should be called as early as possible.
     *
     * @param application The voxeet sdk instance
     */
    public static synchronized com.voxeet.uxkit.controllers.VoxeetToolkit initialize(Application application, EventBus eventBus) {
        return com.voxeet.uxkit.controllers.VoxeetToolkit.initialize(application, eventBus);
    }

    @Deprecated
    public static com.voxeet.uxkit.controllers.VoxeetToolkit getInstance() {
        return com.voxeet.uxkit.controllers.VoxeetToolkit.instance();
    }

    /**
     * Get the instance of the UXKit to use
     *
     * @return the instance of the UXKit
     */
    public static com.voxeet.uxkit.controllers.VoxeetToolkit instance() {
        return com.voxeet.uxkit.controllers.VoxeetToolkit.instance();
    }


}
