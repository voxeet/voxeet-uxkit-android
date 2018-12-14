package com.voxeet.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;

public interface IVoxeetOverlayViewProvider {

    @NonNull
    AbstractVoxeetOverlayView createView(@NonNull Context context,
                                         @NonNull IVoxeetSubViewProvider provider,
                                         @NonNull OverlayState overlayState);

}
