package com.voxeet.uxkit.providers.logics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

public interface IVoxeetSubViewProvider {

    @NonNull
    AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState);

}
