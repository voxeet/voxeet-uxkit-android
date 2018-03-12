package sdk.voxeet.com.toolkit.main;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import sdk.voxeet.com.toolkit.controllers.AbstractConferenceToolkitController;
import sdk.voxeet.com.toolkit.controllers.ConferenceToolkitController;
import sdk.voxeet.com.toolkit.controllers.ReplayMessageToolkitController;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;

/**
 * Created by romainbenmansour on 24/03/2017.
 */
public class VoxeetToolkit implements Application.ActivityLifecycleCallbacks{

    private final static String TAG = VoxeetToolkit.class.getSimpleName();

    private static VoxeetToolkit sInstance;
    private EventBus mEventBus;

    private boolean mIsOverEnabled;
    private Activity mCurrentActivity;
    private List<AbstractConferenceToolkitController> mConferenceToolkitControllers;
    private boolean mIsActivityResumed;


    /**
     * This function initializes the UI Toolkit SDK. It should be called as early as possible.
     *
     * @param application The voxeet sdk instance
     */
    public static synchronized VoxeetToolkit initialize(Application application, EventBus eventBus) {
        sInstance = new VoxeetToolkit(application, eventBus);

        return sInstance;
    }

    public static VoxeetToolkit getInstance() {
        return sInstance;
    }

    private Application mApp;

    private boolean mIsInit = false;

    /**
     * Constructor of the VoxeetToolkit
     * @param application
     */

    private VoxeetToolkit(@NonNull Application application, EventBus eventBus) {
        mEventBus = eventBus;
        mIsActivityResumed = false;
        mConferenceToolkitControllers = new ArrayList<>();

        //keeping a reference on the Application should not be an issue
        //since the Application is the only object available right after the application
        //spawn at native level, it is also the last object available
        //right before being killed by the system
        //hence, no leak here
        mApp = application;

        mApp.registerActivityLifecycleCallbacks(this);

        mIsInit = true;

        registerConferenceToolkitController(new ConferenceToolkitController(application, eventBus, OverlayState.MINIMIZED));
        registerConferenceToolkitController(new ReplayMessageToolkitController(application, eventBus, OverlayState.MINIMIZED));
    }

    public ReplayMessageToolkitController getReplayMessageToolkit() {
        return getAbstractToolkit(ReplayMessageToolkitController.class);
    }

    public ConferenceToolkitController getConferenceToolkit() {
        return getAbstractToolkit(ConferenceToolkitController.class);
    }

    private <CF extends AbstractConferenceToolkitController> CF getAbstractToolkit(Class<CF> klass) {
        for (AbstractConferenceToolkitController controller: mConferenceToolkitControllers) {
            if(controller.getClass().isAssignableFrom(klass)) {
                return (CF) controller;
            }
        }
        return null;
    }

    private void registerConferenceToolkitController(AbstractConferenceToolkitController controller) {
        if(mConferenceToolkitControllers.indexOf(controller) < 0) {
            mConferenceToolkitControllers.add(controller);

            //then call the current state of the activity for this one
            if(mCurrentActivity != null) {
                if(!mIsActivityResumed)
                    controller.onActivityPaused(mCurrentActivity);
                else
                    controller.onActivityResumed(mCurrentActivity);
            }
        }
    }

    private void unregisterConferenceToolkitController(AbstractConferenceToolkitController controller) {
        if(mConferenceToolkitControllers.indexOf(controller) >= 0) {
            mConferenceToolkitControllers.remove(controller);


            //then call the current state of the activity for this one
            if(mCurrentActivity != null) {
                controller.onActivityPaused(mCurrentActivity);
                controller.removeView(true);
            }
        }
    }


    /**
     * Enables or disables the voxeet conference view. This custom view will appear and disappear
     * when join/leaving conference.
     *
     * @param enabled
     */
    public void enableOverlay(boolean enabled) {
        isInitialized();

        mIsOverEnabled = enabled;


        for (AbstractConferenceToolkitController controller : mConferenceToolkitControllers) {
            controller.onOverlayEnabled(enabled);
        }
    }

    /**
     * Gets current activity. Useful when requiring new permissions. Can be null if current activity
     * is finishing or event the mApp.
     *
     * @return the current activity
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
    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public ViewGroup getRootView() {
        Log.d(TAG, "getRootView: " + mCurrentActivity);
        if(mCurrentActivity != null) {
            return (ViewGroup) mCurrentActivity.getWindow().getDecorView().getRootView();
        } else {
            return null;
        }
    }

    /**
     * @throws IllegalStateException if SDK is not initialized.
     */
    private void isInitialized() {
        if (!mIsInit) {
            throw new IllegalStateException(
                    "The UI Toolkit has not been initialized, make sure to call " +
                            "Toolkit.validate() first.");
        }
    }

    /**
     * @return wether overlay is enabled or not
     */
    public boolean isEnabled() {
        return mIsOverEnabled;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mIsActivityResumed = true;
        setCurrentActivity(activity);

        for (AbstractConferenceToolkitController controller : mConferenceToolkitControllers) {
            controller.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mIsActivityResumed = false;
        for (AbstractConferenceToolkitController controller : mConferenceToolkitControllers) {
            controller.onActivityPaused(activity);
        }
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
