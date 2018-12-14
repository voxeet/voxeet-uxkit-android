package com.voxeet.toolkit.implementation.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.R;

import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;

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