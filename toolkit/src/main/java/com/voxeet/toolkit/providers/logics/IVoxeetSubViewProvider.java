package com.voxeet.toolkit.providers.logics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

public interface IVoxeetSubViewProvider {

    @NonNull
    AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState);

}
