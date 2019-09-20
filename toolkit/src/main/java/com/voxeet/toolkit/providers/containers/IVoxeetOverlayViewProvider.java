package com.voxeet.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.activities.VoxeetEventCallBack;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetOverlayView;
import com.voxeet.toolkit.providers.logics.IVoxeetSubViewProvider;

public interface IVoxeetOverlayViewProvider {

    @NonNull
    AbstractVoxeetOverlayView createView(@NonNull Context context,
                                         @NonNull IVoxeetSubViewProvider provider,
                                         @NonNull VoxeetEventCallBack mVoxeetEventCallBack,
                                         @NonNull OverlayState overlayState);

}
