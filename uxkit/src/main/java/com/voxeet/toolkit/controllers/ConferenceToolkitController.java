package com.voxeet.toolkit.controllers;

import android.content.Context;

import com.voxeet.uxkit.implementation.overlays.OverlayState;

import org.greenrobot.eventbus.EventBus;

/**
 * Holds and manipulate the various elements to display the Overlay for a Conference in any app using it
 */
@Deprecated
public class ConferenceToolkitController extends com.voxeet.uxkit.controllers.ConferenceToolkitController {

    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus, overlay);
    }
}
