package com.voxeet.uxkit.providers.containers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.uxkit.providers.logics.IVoxeetSubViewProvider;

public interface IVoxeetOverlayViewProvider {

    @NonNull
    AbstractVoxeetOverlayView createView(@NonNull Context context,
                                         @NonNull IVoxeetSubViewProvider provider,
                                         @NonNull OverlayState overlayState);

}
