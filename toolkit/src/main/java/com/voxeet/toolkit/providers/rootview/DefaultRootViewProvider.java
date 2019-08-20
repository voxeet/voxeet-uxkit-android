package com.voxeet.toolkit.providers.rootview;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.voxeet.toolkit.controllers.VoxeetToolkit;
import com.voxeet.toolkit.views.internal.VoxeetOverlayContainerFrameLayout;

public class DefaultRootViewProvider extends AbstractRootViewProvider {

    private VoxeetOverlayContainerFrameLayout containerFrameLayout;

    private static final String TAG = DefaultRootViewProvider.class.getSimpleName();
    private Activity attachedActivity;

    /**
     * @param application a valid application which be called to obtain events
     * @param toolkit
     */
    public DefaultRootViewProvider(@NonNull Application application, @NonNull VoxeetToolkit toolkit) {
        super(application, toolkit);

    }

    @Nullable
    @Override
    public FrameLayout getRootView() {
        Activity activity = getCurrentActivity();
        Log.d(TAG, "getDefaultRootView: " + activity);
        if (null != activity) {
            if (null == containerFrameLayout) {
                containerFrameLayout = new VoxeetOverlayContainerFrameLayout(activity);
                containerFrameLayout.setLayoutParams(createMatchParams());
            }

            return containerFrameLayout;
        } else {
            return null;
        }
    }

    @Override
    public void addRootView(VoxeetOverlayContainerFrameLayout.OnSizeChangedListener listener) {
        containerFrameLayout.setListener(listener);

        Activity activity = getCurrentActivity();
        attachedActivity = activity;
        ViewGroup group = (ViewGroup) activity.getWindow().getDecorView().getRootView();
        group.addView(containerFrameLayout);

    }

    @Override
    public void onReleaseRootView() {
        attachedActivity = null;
        if (null != containerFrameLayout) containerFrameLayout.setListener(null);
        containerFrameLayout = null;
        //remove from parent ?
    }

    @Override
    public void detachRootViewFromParent() {
        try {
            Activity activity = attachedActivity;
            if (null != activity) {
                ViewGroup group = (ViewGroup) activity.getWindow().getDecorView().getRootView();
                group.removeView(containerFrameLayout);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isSameActivity() {
        return getCurrentActivity() == attachedActivity;
    }

    private FrameLayout.LayoutParams createMatchParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        return params;
    }
}
