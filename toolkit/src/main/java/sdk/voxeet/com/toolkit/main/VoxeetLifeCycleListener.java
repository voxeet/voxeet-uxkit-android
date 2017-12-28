package sdk.voxeet.com.toolkit.main;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.voxeet.toolkit.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sdk.voxeet.com.toolkit.views.uitookit.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.VoxeetView;
import voxeet.com.sdk.events.success.ConferenceDestroyedPushEvent;
import voxeet.com.sdk.events.success.ConferenceEndedEvent;
import voxeet.com.sdk.events.success.ConferenceLeftSuccessEvent;
import voxeet.com.sdk.events.success.ConferencePreJoinedEvent;
import voxeet.com.sdk.utils.ScreenHelper;

/**
 * Life cycle listener allowing to monitor currents activity and display the voxeet overlay if requested
 * Created by romainbenmansour on 20/02/2017.
 */
public class VoxeetLifeCycleListener implements Application.ActivityLifecycleCallbacks {
    private final String TAG = VoxeetLifeCycleListener.class.getSimpleName();

    private Activity currentActivity;

    private EventBus eventBus = EventBus.getDefault();

    private FrameLayout.LayoutParams params;

    private VoxeetView currentView = null;

    private boolean isOverlayEnabled = false;

    private Handler handler;

    /**
     * Instantiates a new Voxeet life cycle listener.
     */
    VoxeetLifeCycleListener(Context context) {
        handler = new Handler(Looper.getMainLooper());

        params = new FrameLayout.LayoutParams(
                context.getResources().getDimensionPixelSize(R.dimen.dimen_100),
                context.getResources().getDimensionPixelSize(R.dimen.dimen_140));
        params.gravity = Gravity.END | Gravity.TOP;
        params.topMargin = ScreenHelper.actionBar(context) + ScreenHelper.getStatusBarHeight(context);

        register();
    }

    /**
     * Method to call to release the object typically when done with it (app shutting down).
     */
    public void onDestroy() {
        unregister();
    }

    void register() {
        if (!eventBus.isRegistered(this))
            eventBus.register(this);
    }

    private void unregister() {
        if (eventBus.isRegistered(this))
            eventBus.unregister(this);
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityStarted(Activity activity) {
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;

        if (currentView != null) { // conf is live
            displayView(currentView, params);
        }
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityPaused(Activity activity) {
        if (currentView != null)
            removeView(currentView, false);
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityStopped(Activity activity) {
    }

    /**
     * @exclude
     */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    /**
     * @exclude
     */
    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    private ViewGroup rootView() {
        return (ViewGroup) currentActivity.getWindow().getDecorView().getRootView();
    }

    private synchronized void displayView(final View view, final FrameLayout.LayoutParams params) {
        if (isOverlayEnabled) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (view != null) {
                        ViewGroup viewHolder = (ViewGroup) view.getParent();
                        if (viewHolder != null)
                            viewHolder.removeView(view);

                        if (currentActivity != null && !currentActivity.isFinishing())
                            rootView().addView(view, params);
                    }
                }
            });
        }
    }

    private synchronized void removeView(final View view, final boolean shouldRelease) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (currentView != null) {
                    ViewGroup viewHolder = (ViewGroup) view.getParent();
                    if (viewHolder != null)
                        viewHolder.removeView(view);

                    if (shouldRelease) {
                        currentView.onDestroy();
                        currentView = null;
                    }
                }
            }
        });
    }

    public boolean isOverlayEnabled() {
        return isOverlayEnabled;
    }

    /**
     * Gets current activity.
     *
     * @return the current activity
     */
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Toggles overlay visibility.
     */
    void onOverlayEnabled(boolean enabled) {
        isOverlayEnabled = enabled;

        if (enabled)
            displayView(currentView, params);
        else
            removeView(currentView, false);
    }

    /**
     * Display the conference view when the user is creating/joining a conference.
     *
     * @param event the event
     * @exclude
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferencePreJoinedEvent event) {
        if (currentActivity != null) {
            if (currentView == null)
                currentView = new VoxeetConferenceView(currentActivity.getApplicationContext());

            params = new FrameLayout.LayoutParams(
                    currentActivity.getResources().getDimensionPixelSize(R.dimen.dimen_100),
                    currentActivity.getResources().getDimensionPixelSize(R.dimen.dimen_140));
            params.gravity = Gravity.END | Gravity.TOP;
            params.topMargin = ScreenHelper.actionBar(currentActivity) + ScreenHelper.getStatusBarHeight(currentActivity);

            displayView(currentView, params);
        }
    }

    /**
     * Removing view (if visible) if the user left the conference.
     *
     * @param event the event
     * @exclude
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceLeftSuccessEvent event) {
        removeView(currentView, true);
    }

    /**
     * Removing view (if visible) if the conference has ended.
     *
     * @param event the event
     * @exclude
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEndedEvent event) {
        removeView(currentView, true);
    }

    /**
     * Removing view (if visible) if the conference has been destroyed.
     *
     * @param event the event
     * @exclude
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPushEvent event) {
        removeView(currentView, true);
    }
}
