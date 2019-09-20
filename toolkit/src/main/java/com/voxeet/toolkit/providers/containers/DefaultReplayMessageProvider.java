package com.voxeet.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.activities.VoxeetEventCallBack;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.VoxeetOverlayBackView;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;

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
                                                @NonNull VoxeetEventCallBack mVoxeetEventCallBack,
                                                @NonNull OverlayState overlayState) {
        return new VoxeetOverlayBackView(mListener, provider,mVoxeetEventCallBack, context, overlayState);
    }
}
