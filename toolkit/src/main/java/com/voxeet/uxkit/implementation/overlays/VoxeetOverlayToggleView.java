package com.voxeet.uxkit.implementation.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.uxkit.R;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

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