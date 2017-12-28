package sdk.voxeet.com.toolkit.main;

import android.app.Activity;
import android.app.Application;

/**
 * Created by romainbenmansour on 24/03/2017.
 */
public class VoxeetToolkit {

    private static VoxeetLifeCycleListener lifeCycleListener;

    private static Application app;

    private static boolean isInit = false;

    /**
     * This function initializes the UI Toolkit SDK. It should be called as early as possible.
     *
     * @param application The voxeet sdk instance
     */
    public static synchronized void initialize(Application application) {
        app = application;

        app.registerActivityLifecycleCallbacks(lifeCycleListener = new VoxeetLifeCycleListener(app.getApplicationContext()));

        isInit = true;
    }

    /**
     * Enables or disables the voxeet conference view. This custom view will appear and disappear
     * when join/leaving conference.
     *
     * @param enabled
     */
    public static void enableOverlay(boolean enabled) {
        isInitialized();

        lifeCycleListener.onOverlayEnabled(enabled);
    }

    /**
     * Gets current activity. Useful when requiring new permissions. Can be null if current activity
     * is finishing or event the app.
     *
     * @return the current activity
     */
    public static Activity getCurrentActivity() {
        return lifeCycleListener.getCurrentActivity();
    }

    /**
     * @throws IllegalStateException if SDK is not initialized.
     */
    static void isInitialized() {
        if (!isInit) {
            throw new IllegalStateException(
                    "The UI Toolkit has not been initialized, make sure to call " +
                            "Toolkit.validate() first.");
        }
    }

    /**
     * @return wether overlay is enabled or not
     */
    public static boolean isEnabled() {
        return lifeCycleListener.isOverlayEnabled();
    }
}
