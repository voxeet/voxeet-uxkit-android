package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;

import com.voxeet.android.media.Media;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.providers.containers.DefaultReplayMessageProvider;
import sdk.voxeet.com.toolkit.providers.logics.DefaultReplayMessageSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ReplayMessageToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    public ReplayMessageToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        setDefaultOverlayState(overlay);
        setVoxeetOverlayViewProvider(new DefaultReplayMessageProvider(this));
        setVoxeetSubViewProvider(new DefaultReplayMessageSubViewProvider());
    }

    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    /**
     * Ask for replay a given video
     * <p>
     * TODO this method does not make the replay specific to the current object for now
     * it will in the future if mandatory
     * <p>
     * The implementation would have to make a specific EventBus for the VoxeetSDK Instance
     * and add multiple instance management
     *
     * @param conferenceId the conference id to replay
     * @param offset       the offset in seconds from the start
     */
    public final void replay(String conferenceId, long offset) {
        VoxeetToolkit.getInstance().getConferenceToolkit().enable(false);
        enable(true);

        VoxeetSdk.getInstance().getConferenceService().setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);
        VoxeetSdk.getInstance().getConferenceService().replay(conferenceId, offset);

    }

    @Override
    public void onActionButtonClicked() {
        //leave the current conference
        VoxeetSdk.getInstance().getConferenceService().leave();
    }
}
