package com.voxeet.toolkit.implementation.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

@Deprecated
public class VoxeetOverlayBackView extends com.voxeet.uxkit.implementation.overlays.VoxeetOverlayBackView {

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param provider
     * @param context  the context
     * @param overlay
     */
    public VoxeetOverlayBackView(@NonNull IExpandableViewProviderListener listener, @NonNull IVoxeetSubViewProvider provider, @NonNull Context context, @NonNull OverlayState overlay) {
        super(listener, provider, context, overlay);
    }
}