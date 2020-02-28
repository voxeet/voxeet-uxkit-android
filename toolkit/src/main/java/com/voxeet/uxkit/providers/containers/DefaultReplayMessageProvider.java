package com.voxeet.uxkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.VoxeetOverlayBackView;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

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
