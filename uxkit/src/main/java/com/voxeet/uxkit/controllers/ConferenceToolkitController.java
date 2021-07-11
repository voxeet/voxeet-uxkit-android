package com.voxeet.uxkit.controllers;

import android.content.Context;

import com.voxeet.uxkit.configuration.Configuration;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.containers.DefaultConferenceProvider;
import com.voxeet.uxkit.providers.logics.DefaultConferenceSubViewProvider;

import org.greenrobot.eventbus.EventBus;

/**
 * Holds and manipulate the various elements to display the Overlay for a Conference in any app using it
 */
public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private boolean mScreenShareEnabled = false;

    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        setDefaultOverlayState(overlay);
        setVoxeetOverlayViewProvider(new DefaultConferenceProvider(this));
        setVoxeetSubViewProvider(new DefaultConferenceSubViewProvider());
    }

    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    @Override
    public void onActionButtonClicked() {
        //nothing to do
    }

    /**
     * Check for the screenshare state : possible or not
     * @return the indicator
     */
    public boolean isScreenShareEnabled() {
        return mScreenShareEnabled;
    }

    /**
     * Change the screenshare capability state
     * @param state the new state
     * @return the current instance
     */
    public ConferenceToolkitController setScreenShareEnabled(boolean state) {
        mScreenShareEnabled = state;
        return this;
    }
}
