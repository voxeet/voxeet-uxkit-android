package com.voxeet.toolkit.providers.logics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.voxeet.toolkit.implementation.VoxeetConferenceView;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.AbstractVoxeetExpandableView;

/**
 * Created by kevinleperf on 04/02/2018.
 */

public class DefaultConferenceSubViewProvider implements IVoxeetSubViewProvider {

    @NonNull
    @Override
    public AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState) {
        return new VoxeetConferenceView(context);
    }
}
