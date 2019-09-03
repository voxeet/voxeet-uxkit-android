package com.voxeet.toolkit.implementation.overlays;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.toolkit.R;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;

public class VoxeetOverlayToggleView extends AbstractVoxeetOverlayView {

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param provider
     * @param context  the context
     * @param overlay
     */
    public VoxeetOverlayToggleView(@NonNull IExpandableViewProviderListener listener,
                                   @NonNull IVoxeetSubViewProvider provider,
                                   @NonNull Context context,
                                   @NonNull OverlayState overlay) {
        super(listener, provider, context, overlay);
    }

    @Override
    final protected void onActionButtonClicked() {
        toggleSize();

        getExpandableViewProviderListener().onActionButtonClicked();
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_toggle_view;
    }
}