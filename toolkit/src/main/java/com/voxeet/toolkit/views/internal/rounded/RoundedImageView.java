package com.voxeet.toolkit.views.internal.rounded;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;


@SuppressWarnings("UnusedDeclaration")
@Deprecated
public class RoundedImageView extends com.voxeet.uxkit.views.internal.rounded.RoundedImageView {

    public static final String TAG = "RoundedImageView";
    public static final float DEFAULT_RADIUS = com.voxeet.uxkit.views.internal.rounded.RoundedImageView.DEFAULT_RADIUS;
    public static final float DEFAULT_BORDER_WIDTH = com.voxeet.uxkit.views.internal.rounded.RoundedImageView.DEFAULT_BORDER_WIDTH;

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
