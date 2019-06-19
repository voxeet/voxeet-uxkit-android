package com.voxeet.toolkit.views.internal.rounded;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.voxeet.toolkit.R;

/**
 * RoundedFrameLayout :: simple view to manage corners
 * <p>
 * not as optimised as a Shadder/Program in the Renderer but will ensure compatibilities
 * in the first releases of this feature
 */
@Deprecated
public class RoundedFrameLayout extends com.voxeet.sdk.views.RoundedFrameLayout {
    public RoundedFrameLayout(@NonNull Context context) {
        super(context);
    }

    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
