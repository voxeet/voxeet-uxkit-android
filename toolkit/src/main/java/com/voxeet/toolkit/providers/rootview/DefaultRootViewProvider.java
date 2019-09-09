package com.voxeet.toolkit.providers.rootview;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.voxeet.sdk.exceptions.ExceptionManager;
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

        //if has a parent and not the root view -> remove then add
        //if has a parent and is the root view -> nothing
        //if !has a parent -> add

        try {
            if (null != containerFrameLayout.getParent() && group != containerFrameLayout.getParent()) {
                ViewParent view = containerFrameLayout.getParent();
                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).removeView(containerFrameLayout);
                }
                group.addView(containerFrameLayout);
            } else if (null == containerFrameLayout.getParent()) {
                group.addView(containerFrameLayout);
            }
        } catch (Exception e) {
            //log internally the exception - normally none should happen
            ExceptionManager.sendException(e);
        }

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
