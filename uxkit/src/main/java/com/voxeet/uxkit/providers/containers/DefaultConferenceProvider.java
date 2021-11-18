package com.voxeet.uxkit.providers.containers;

import android.content.Context;
import androidx.annotation.NonNull;

import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.VoxeetOverlayToggleView;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

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

    @NonNull
    @Override
    public AbstractVoxeetOverlayView createView(@NonNull Context context,
                                                @NonNull IVoxeetSubViewProvider provider,
                                                @NonNull OverlayState overlayState) {
        return new VoxeetOverlayToggleView(mListener, provider, context, overlayState);
    }
}
