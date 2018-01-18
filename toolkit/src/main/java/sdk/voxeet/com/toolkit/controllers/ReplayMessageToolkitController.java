package sdk.voxeet.com.toolkit.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.android.media.Media;

import org.greenrobot.eventbus.EventBus;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetReplayMessageView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayBackView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ReplayMessageToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {
    private Activity mActivity;

    public ReplayMessageToolkitController(Context context, EventBus eventbus) {
        super(context, eventbus);

    }

    @Override
    protected VoxeetView createMainView(final Activity activity) {
        return new VoxeetOverlayBackView(this, activity);
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
     * @param confAliasOrId the message or conf to replay
     * @param offset        the offset in seconds from the start
     */
    public static final void replay(String confAliasOrId, long offset) {
        VoxeetSdk.getInstance().getConferenceService().setAudioRoute(Media.AudioRoute.ROUTE_SPEAKER);
        VoxeetSdk.getInstance().getConferenceService().replay(confAliasOrId, 0);
    }

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createSubVoxeetView() {
        return new VoxeetReplayMessageView(getContext());
    }

    @Override
    public void onActionButtonClicked() {
        //leave the current conference
        VoxeetSdk.getInstance().getConferenceService().leave();
    }

}
