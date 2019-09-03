package com.voxeet.toolkit.providers.logics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.toolkit.implementation.VoxeetReplayMessageView;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

/**
 * Created by kevinleperf on 04/02/2018.
 */

public class DefaultReplayMessageSubViewProvider implements IVoxeetSubViewProvider {

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState) {
        return new VoxeetReplayMessageView(context);
    }
}
