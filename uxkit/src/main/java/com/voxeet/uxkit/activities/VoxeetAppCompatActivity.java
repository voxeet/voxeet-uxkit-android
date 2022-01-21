package com.voxeet.uxkit.activities;

import com.voxeet.uxkit.common.activity.VoxeetCommonAppCompatActivity;
import com.voxeet.uxkit.common.service.AbstractSDKService;
import com.voxeet.uxkit.common.service.SDKBinder;
import com.voxeet.uxkit.controllers.VoxeetToolkit;

/**
 * VoxeetAppCompatActivity manages the call state
 * <p>
 * In the current merged state, this class is not used
 * <p>
 * However, it is extremely easy to use this class now :
 * - manages automatically the bundles to join conferences when "resumed"
 * - automatically registers its subclasses's extra info to propagate to "recreated" instances
 * <p>
 * Few things to consider :
 * - singleTop / singleInstance
 */
public class VoxeetAppCompatActivity<T extends AbstractSDKService<? extends SDKBinder<T>>> extends VoxeetCommonAppCompatActivity<T> {

    public VoxeetAppCompatActivity() {
        super();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != VoxeetToolkit.instance()) {
            //to prevent uninitialized toolkit but ... it's highly recommended for future releases to always init
            VoxeetToolkit.instance().getConferenceToolkit().forceReattach();
        }
    }

}

