package sdk.voxeet.com.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.providers.logics.IVoxeetSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.VoxeetOverlayToggleView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

/**
 * Created by kevinleperf on 26/01/2018.
 */

public class DefaultConferenceProvider implements IVoxeetOverlayViewProvider {

    private IExpandableViewProviderListener mListener;

    protected IExpandableViewProviderListener getListener() {
        return mListener;
    }

    private DefaultConferenceProvider() {

    }

    public DefaultConferenceProvider(IExpandableViewProviderListener listener) {
        this();

        mListener = listener;
    }

    @Override
    public AbstractVoxeetOverlayView createView(@NonNull Context context,
                                                @NonNull IVoxeetSubViewProvider provider,
                                                @NonNull OverlayState overlayState) {
        return new VoxeetOverlayToggleView(mListener, provider, context, overlayState);
    }
}
