package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayToggleView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus, overlay);
    }

    @Override
    protected AbstractVoxeetOverlayView createMainView(final Activity activity) {
        return new VoxeetOverlayToggleView(this, activity, getDefaultOverlayState());
    }

    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createSubVoxeetView() {
        return new VoxeetConferenceView(getContext());
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
