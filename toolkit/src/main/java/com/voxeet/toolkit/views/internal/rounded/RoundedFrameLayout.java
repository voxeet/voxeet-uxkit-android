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

public class RoundedFrameLayout extends FrameLayout {
    private static final String TAG = RoundedFrameLayout.class.getSimpleName();
    private Path path = new Path();
    private RectF rect = new RectF();
    private boolean isCircle;
    private float cornerRadius;

    public RoundedFrameLayout(@NonNull Context context) {
        super(context);

        init(context, null);
    }

    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RoundedFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    public void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedFrameLayout);
        isCircle = attributes.getBoolean(R.styleable.RoundedFrameLayout_roundedCircle, false);
        cornerRadius = attributes.getDimension(R.styleable.RoundedFrameLayout_roundedCornerRadius, 0);
        attributes.recycle();

        Log.d(TAG, "init: " + cornerRadius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        updateSize(w, h);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    public RoundedFrameLayout setIsCircle(boolean isCircle) {
        this.isCircle = isCircle;

        invalidatePath();
        return this;
    }

    public RoundedFrameLayout setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;

        invalidatePath();
        return this;
    }

    private void invalidatePath() {
        int width = getWidth();
        int height = getHeight();

        if (width > 0 && height > 0) {
            updateSize(width, height);
            postInvalidate();
        }
    }

    private void updateSize(int width, int height) {
        if (isCircle) {
            float halfWidth = width / 2f;
            float halfHeight = height / 2f;
            path.reset();
            path.addCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), Path.Direction.CW);
            path.close();
        } else {
            path.reset();
            float[] radii = {cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius};
            rect.set(0, 0, width, height);
            path.addRoundRect(rect, radii, Path.Direction.CW);
            path.close();
        }
    }
}
