package com.voxeet.toolkit.controllers;

import android.content.Context;

import com.voxeet.sdk.utils.Annotate;
import com.voxeet.toolkit.configuration.Configuration;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.containers.DefaultConferenceProvider;
import com.voxeet.toolkit.providers.logics.DefaultConferenceSubViewProvider;

import org.greenrobot.eventbus.EventBus;

@Annotate
public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private boolean mScreenShareEnabled = false;
    public final Configuration Configuration = new Configuration();

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

    public boolean isScreenShareEnabled() {
        return mScreenShareEnabled;
    }

    public ConferenceToolkitController setScreenShareEnabled(boolean state) {
        mScreenShareEnabled = state;
        return this;
    }
}
