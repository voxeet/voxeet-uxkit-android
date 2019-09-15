package com.voxeet.toolkit.controllers;

import android.content.Context;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.sdk.utils.NoDocumentation;
import com.voxeet.toolkit.configuration.Configuration;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.containers.DefaultConferenceProvider;
import com.voxeet.toolkit.providers.logics.DefaultConferenceSubViewProvider;

import org.greenrobot.eventbus.EventBus;

/**
 * Holds and manipulate the various elements to display the Overlay for a Conference in any app using it
 */
@Annotate
public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private boolean mScreenShareEnabled = false;
    public final Configuration Configuration = new Configuration();

    @NoDocumentation
    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        setDefaultOverlayState(overlay);
        setVoxeetOverlayViewProvider(new DefaultConferenceProvider(this));
        setVoxeetSubViewProvider(new DefaultConferenceSubViewProvider());
    }

    @NoDocumentation
    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    @NoDocumentation
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
