package com.voxeet.toolkit.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * VideoView implementation
 *
 * @deprecated use {@link com.voxeet.sdk.views.VideoView} instead
 */
@Deprecated
public class VideoView extends com.voxeet.sdk.views.VideoView {
    public VideoView(Context context) {
        super(context);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}