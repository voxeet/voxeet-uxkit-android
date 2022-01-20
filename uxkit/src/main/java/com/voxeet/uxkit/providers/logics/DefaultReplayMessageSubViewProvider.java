package com.voxeet.uxkit.providers.logics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.uxkit.implementation.VoxeetReplayMessageView;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

public class DefaultReplayMessageSubViewProvider implements IVoxeetSubViewProvider {

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState) {
        return new VoxeetReplayMessageView(context);
    }
}
