package sdk.voxeet.com.toolkit.providers.logics;

import android.content.Context;
import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetReplayMessageView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;

/**
 * Created by kevinleperf on 04/02/2018.
 */

public class DefaultReplayMessageSubViewProvider implements IVoxeetSubViewProvider {

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState) {
        return new VoxeetReplayMessageView(context);
    }
}
