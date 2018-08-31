package sdk.voxeet.com.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.providers.logics.IVoxeetSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayBackView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

/**
 * Created by kevinleperf on 26/01/2018.
 */

public class DefaultReplayMessageProvider implements IVoxeetOverlayViewProvider {

    private IExpandableViewProviderListener mListener;

    private DefaultReplayMessageProvider() {

    }

    public DefaultReplayMessageProvider(IExpandableViewProviderListener listener) {
        this();

        mListener = listener;
    }

    @NonNull
    @Override
    public AbstractVoxeetOverlayView createView(@NonNull Context context,
                                                @NonNull IVoxeetSubViewProvider provider,
                                                @NonNull OverlayState overlayState) {
        return new VoxeetOverlayBackView(mListener, provider, context, overlayState);
    }
}
