package com.voxeet.toolkit.implementation;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Simple View to manage how Users are displayed "on top" of the screen (or wherever the default list should be positionned)
 */
@Deprecated
public class VoxeetUsersView extends com.voxeet.uxkit.implementation.VoxeetParticipantsView {

    public VoxeetUsersView(Context context) {
        super(context);
    }

    public VoxeetUsersView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VoxeetUsersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
