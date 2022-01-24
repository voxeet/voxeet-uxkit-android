package com.voxeet.uxkit.controllers;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.sdk.services.TelemetryService;
import com.voxeet.sdk.services.telemetry.SdkEnvironment;
import com.voxeet.sdk.utils.Map;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.BuildConfig;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.implementation.overlays.OverlayState;
import com.voxeet.uxkit.providers.rootview.AbstractRootViewProvider;
import com.voxeet.uxkit.providers.rootview.DefaultRootViewProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple VoxeetToolkit implementation
 */
public class VoxeetToolkit implements Application.ActivityLifecycleCallbacks {

    private static final ShortLogger Log = UXKitLogger.createLogger(VoxeetToolkit.class);

    private static VoxeetToolkit sInstance;

    @Nullable
    private AbstractRootViewProvider mProvider;

    private boolean mIsOverEnabled;
    private List<AbstractConferenceToolkitController> mConferenceToolkitControllers;


    /**
     * This function initializes the UI Toolkit SDK. It should be called as early as possible.
     *
     * @param application The voxeet sdk instance
     */
    public static synchronized VoxeetToolkit initialize(Application application, EventBus eventBus) {

        if (null == sInstance) {
            Log.d("initialize: toolkit initializing");

            TelemetryService.register(SdkEnvironment.UXKIT, BuildConfig.VERSION_NAME);
            sInstance = new VoxeetToolkit();

            DefaultRootViewProvider provider = new DefaultRootViewProvider(application, sInstance);
            provider.registerLifecycleListener(sInstance);
            sInstance.setProvider(provider);

            sInstance.init(application, eventBus);
        }
        return sInstance;
    }

    @Deprecated
    public static VoxeetToolkit getInstance() {
        return sInstance;
    }

    /**
     * Get the instance of the UXKit to use
     *
     * @return the instance of the UXKit
     */
    public static VoxeetToolkit instance() {
        return sInstance;
    }


    private boolean mIsInit = false;

    /**
     * Constructor of the VoxeetToolkit
     * <p>
     * public until the switch from implementation is made to the new package
     */
    public VoxeetToolkit() {
    }

    /**
     * Replace the current provider with an other one
     * <p>
     * TODO send event to "remove" the previous instances
     * For now, setProvider() should be called 1 time at most in production
     *
     * @param provider
     */
    public void setProvider(@NonNull AbstractRootViewProvider provider) {
        mProvider = provider;
    }

    /**
     * Call this method to get the instance of the Replay manager
     *
     * @return the instance
     */
    @NonNull
    public ReplayMessageToolkitController getReplayMessageToolkit() {
        return getAbstractToolkit(ReplayMessageToolkitController.class);
    }

    /**
     * Call this method to get the default instance of the Conference manager
     *
     * @return the instance
     */
    @NonNull
    public ConferenceToolkitController getConferenceToolkit() {
        return getAbstractToolkit(ConferenceToolkitController.class);
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

    @NonNull
    public AbstractRootViewProvider getDefaultRootViewProvider() {
        return mProvider;
    }

    /**
     * Check the UXKit state. Disabling it won't trigger the overlays
     *
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
    public void onActivityResumed(@NonNull Activity activity) {
        for (AbstractConferenceToolkitController controller : mConferenceToolkitControllers) {
            controller.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
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

    /**
     * Disable every com.voxeet.uxkit.controllers
     */
    public void disableAll() {
        for (AbstractConferenceToolkitController internal_controller : mConferenceToolkitControllers) {
            internal_controller.enable(false);
        }
    }

    /**
     * Activate the first controller with the same class, disable all others
     *
     * @return true if the controller was found and activated or already activated
     */
    public <T extends AbstractConferenceToolkitController> boolean enable(@NonNull Class<T> controllerKlass) {
        AbstractConferenceToolkitController firstController = Map.find(mConferenceToolkitControllers,
                controller -> null != controller && controller.getClass().equals(controllerKlass));

        return Opt.of(firstController).then(this::enable).or(false);
    }

    /**
     * Activate a controller, disable all others - only if no controller has been found
     *
     * @param controller a non null controller to try to activate
     * @return true if the controller was found and activated or already activated
     */
    public boolean enable(@NonNull AbstractConferenceToolkitController controller) {
        if (mConferenceToolkitControllers.contains(controller)) {
            for (AbstractConferenceToolkitController internal_controller : mConferenceToolkitControllers) {
                //only check for references
                if (controller != internal_controller) { //if different, disable the current
                    internal_controller.enable(false);
                }
            }
            //only enable if was disabled
            if (!controller.isEnabled()) controller.enable(true);
            return true;
        }
        return false;
    }

    private void init(@NonNull Application application,
                      EventBus eventBus) {

        mConferenceToolkitControllers = new ArrayList<>();

        mIsInit = true;

        registerConferenceToolkitController(new ConferenceToolkitController(application, eventBus, OverlayState.MINIMIZED));
        registerConferenceToolkitController(new ReplayMessageToolkitController(application, eventBus, OverlayState.MINIMIZED));
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

    @Nullable
    private <CF extends AbstractConferenceToolkitController> CF getAbstractToolkit(Class<CF> klass) {
        for (AbstractConferenceToolkitController controller : mConferenceToolkitControllers) {
            if (controller.getClass().isAssignableFrom(klass)) {
                try {
                    return (CF) controller;
                } catch (Exception e) {

                }
            }
        }
        return null;
    }

    /**
     * Register a new ConferenceToolkit controller. Use this to replace the default instances
     * <p>
     * Don't forget to call the _enable_ method to switch the state
     *
     * @param controller the new controller
     */
    public void registerConferenceToolkitController(@NonNull AbstractConferenceToolkitController controller) {
        if (mConferenceToolkitControllers.indexOf(controller) < 0) {
            mConferenceToolkitControllers.add(controller);

            //then call the current state of the activity for this one
            Activity activity = Opt.of(mProvider).then(AbstractRootViewProvider::getCurrentActivity).orNull();
            if (controller.isEnabled() && null != activity) {
                if (!Opt.of(mProvider).then(AbstractRootViewProvider::isCurrentActivityResumed).or(false))
                    controller.onActivityPaused(activity);
                else
                    controller.onActivityResumed(activity);
            }
        }
    }

    private void unregisterConferenceToolkitController(@NonNull AbstractConferenceToolkitController controller) {
        if (mConferenceToolkitControllers.indexOf(controller) >= 0) {
            mConferenceToolkitControllers.remove(controller);


            //then call the current state of the activity for this one
            Activity activity = Opt.of(mProvider).then(AbstractRootViewProvider::getCurrentActivity).orNull();
            if (controller.isEnabled() && null != activity) {
                controller.onActivityPaused(activity);
                controller.removeView(true, RemoveViewType.FROM_HUD);
            }
        }
    }

    public void setCurrentActivity(@NonNull Activity activity) {
        if (null != mProvider)
            mProvider.setCurrentActivity(activity);
    }

    @Nullable
    public Activity getCurrentActivity() {
        return Opt.of(mProvider).then(AbstractRootViewProvider::getCurrentActivity).orNull();
    }
}
