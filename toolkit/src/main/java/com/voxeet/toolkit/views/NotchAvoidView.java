package com.voxeet.toolkit.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.voxeet.toolkit.R;
import com.voxeet.toolkit.utils.WindowHelper;

public class NotchAvoidView extends View {
    private static final String TAG = NotchAvoidView.class.getSimpleName();
    private float systemBarHeight;
    private float notchHeight;
    private boolean below_system_bar;

    public NotchAvoidView(Context context) {
        super(context);
        init(null);
    }

    public NotchAvoidView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NotchAvoidView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NotchAvoidView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        checkInfos();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Point size = new Point();
            WindowHelper.getSize(getContext(), size);

            if (size.x < size.y) {
                float height = systemBarHeight;
                if (height < notchHeight) height = notchHeight;

                setMeasuredDimension(300, (int) height);
                return;
            }

        }
        //0 height by default
        setMeasuredDimension(300, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        checkInfos();
    }

    /**
     * Internal init of the view
     *
     * @param attrs possible attributes given to it
     */
    private void init(@Nullable AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.NotchAvoidView);
        below_system_bar = attributes.getBoolean(R.styleable.NotchAvoidView_below_system_bar, false);
        attributes.recycle();

        systemBarHeight = notchHeight = 0f;
        checkInfos();
    }

    /**
     * From the system information, retrieve the systemBar and the possible Notch height
     */
    private void checkInfos() {
        if (isTranslucentStatusBar()) {
            systemBarHeight = WindowHelper.dpToPx(getContext(), 24f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && null != getRootWindowInsets()) {
                DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();

                if (null != displayCutout && null != displayCutout.getBoundingRects()) {
                    for (Rect rect : displayCutout.getBoundingRects()) {
                        //notch on top - bottom = notch's height
                        if (0 == rect.top && notchHeight < rect.bottom) {
                            notchHeight = rect.bottom;
                        }
                    }
                }
            }
        }
    }

    /**
     * Check for the current translucent or forced below sytembar
     *
     * @return true if translucent or forced
     */
    protected boolean isTranslucentStatusBar() {
        if (below_system_bar) {
            Log.d(TAG, "isTranslucentStatusBar: we have below_system_bar, so true in fact");
            return true;
        }

        if (null != getContext() && getContext() instanceof Activity
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Window window = ((Activity) getContext()).getWindow();
            int flags = window.getAttributes().flags;
            int translucent = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            return (flags & translucent) == translucent;
        }
        return false;
    }
}
