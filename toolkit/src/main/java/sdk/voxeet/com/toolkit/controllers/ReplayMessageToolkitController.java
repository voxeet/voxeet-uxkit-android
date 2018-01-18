package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController {
    public ConferenceToolkitController(Context context, EventBus eventbus) {
        super(context, eventbus);
    }

    @Override
    protected VoxeetView createMainView(Activity activity) {
        return new VoxeetConferenceView(activity);
    }
}
