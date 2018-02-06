package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.providers.containers.DefaultConferenceProvider;
import sdk.voxeet.com.toolkit.providers.logics.DefaultConferenceSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

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

    public void join(String conference_id) {
        VoxeetToolkit.getInstance().getReplayMessageToolkit().enable(false);
        enable(true);

        VoxeetSdk.getInstance().getConferenceService().join(conference_id);
    }

    public void demo() {
        VoxeetToolkit.getInstance().getReplayMessageToolkit().enable(false);
        enable(true);

        VoxeetSdk.getInstance().getConferenceService().demo();
    }

    public void create() {
        VoxeetToolkit.getInstance().getReplayMessageToolkit().enable(false);
        enable(true);

        VoxeetSdk.getInstance().getConferenceService().create();
    }
}
