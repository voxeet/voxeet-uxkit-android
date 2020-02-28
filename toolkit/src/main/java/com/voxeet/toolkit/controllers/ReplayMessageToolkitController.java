package com.voxeet.toolkit.controllers;

import android.content.Context;

import com.voxeet.uxkit.implementation.overlays.OverlayState;

import org.greenrobot.eventbus.EventBus;

@Deprecated
public class ReplayMessageToolkitController extends com.voxeet.uxkit.controllers.ReplayMessageToolkitController {

    public ReplayMessageToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus, overlay);
    }
}
