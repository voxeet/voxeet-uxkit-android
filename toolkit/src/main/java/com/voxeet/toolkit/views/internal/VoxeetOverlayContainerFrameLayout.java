package com.voxeet.toolkit.views.internal;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class VoxeetOverlayContainerFrameLayout extends FrameLayout {

    @Nullable
    private OnSizeChangedListener listener;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (null != listener)
                listener.onSizedChangedListener(VoxeetOverlayContainerFrameLayout.this);
        }
    };

    public VoxeetOverlayContainerFrameLayout(@NonNull Context context) {
        super(context);
    }

    public VoxeetOverlayContainerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VoxeetOverlayContainerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VoxeetOverlayContainerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw || h != oldh) {
            handler.post(runnable);
        }
    }

    public void setListener(@Nullable OnSizeChangedListener listener) {
        this.listener = listener;
    }

    public interface OnSizeChangedListener {
        void onSizedChangedListener(@NonNull VoxeetOverlayContainerFrameLayout view);
    }
}
