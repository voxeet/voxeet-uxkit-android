package com.voxeet.uxkit.providers.rootview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.controllers.VoxeetToolkit;
import com.voxeet.uxkit.views.internal.VoxeetOverlayContainerFrameLayout;

/**
 * Abstract class which can manage the state of its parent activity
 * and give a ViewGroup in which incorporate the conference calls
 */

public abstract class AbstractRootViewProvider implements Application.ActivityLifecycleCallbacks {

    private static final ShortLogger Log = UXKitLogger.createLogger(AbstractRootViewProvider.class);

    @NonNull
    private Application mApp; //initialized by constructor

    private boolean mIsActivityResumed;

    @Nullable
    private Activity mCurrentActivity;

    @Nullable
    private Application.ActivityLifecycleCallbacks mListener;

    private AbstractRootViewProvider() {
        mIsActivityResumed = false;
        mCurrentActivity = null;
    }

    /**
     * @param application a valid application which be called to obtain events
     */
    protected AbstractRootViewProvider(@NonNull Application application,
                                       @NonNull VoxeetToolkit toolkit) {
        this();

        //keeping a reference on the Application should not be an issue
        //since the Application is the only object available right after the application
        //spawn at native level, it is also the last object available
        //right before being killed by the system
        //hence, no leak here
        mApp = application;

        mApp.registerActivityLifecycleCallbacks(this);
    }

    public void registerLifecycleListener(Application.ActivityLifecycleCallbacks listener) {
        Log.d("registerLifecycleListener: " + listener);
        mListener = listener;
    }

    @Nullable
    public abstract FrameLayout getRootView();

    public abstract void addRootView(VoxeetOverlayContainerFrameLayout.OnSizeChangedListener listener);

    public abstract void onReleaseRootView();

    public abstract void detachRootViewFromParent();

    //TODO should this be managed here ?
    public abstract boolean isSameActivity();

    /**
     * Sets the current activity. Useful to retrieve later for more permissions.
     * Can be null if current activity is finishing or event the mApp.
     */
    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    /**
     * Gets current activity. Useful when requiring new permissions. Can be null if current activity
     * is finishing or event the mApp.
     *
     * @return the current activity
     */
    @Nullable
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    /**
     * Is the current activity resumed ?
     *
     * @return true of false depending
     */
    public boolean isCurrentActivityResumed() {
        return mIsActivityResumed;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mIsActivityResumed = true;
        setCurrentActivity(activity);

        if (mListener != null) {
            mListener.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        mIsActivityResumed = false;

        if (mListener != null) {
            mListener.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
