package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import com.voxeet.toolkit.implementation.overlays.OverlayState;

/**
 * Please switch to the new implementation
 *
 * will be removed later
 */
@Deprecated
public class ReplayMessageToolkitController extends com.voxeet.toolkit.controllers.ReplayMessageToolkitController {
    public ReplayMessageToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus, overlay);
    }
}
