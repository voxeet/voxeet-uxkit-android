package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayToggleView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {
    public ConferenceToolkitController(Context context, EventBus eventbus) {
        super(context, eventbus);
    }

    @Override
    protected VoxeetView createMainView(final Activity activity) {
        return new VoxeetOverlayToggleView(this, activity);
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
}
