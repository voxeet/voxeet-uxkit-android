package com.voxeet.uxkit.implementation.overlays;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.R;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

public class VoxeetOverlayBackView extends AbstractVoxeetOverlayView {

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param provider
     * @param context  the context
     * @param overlay
     */
    public VoxeetOverlayBackView(@NonNull IExpandableViewProviderListener listener,
                                 @NonNull IVoxeetSubViewProvider provider,
                                 @NonNull Context context,
                                 @NonNull OverlayState overlay) {
        super(listener, provider, context, overlay);
    }

    @Override
    final protected void onActionButtonClicked() {
        getExpandableViewProviderListener().onActionButtonClicked();
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_back_view;
    }
}