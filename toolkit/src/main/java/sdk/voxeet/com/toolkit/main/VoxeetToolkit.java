package sdk.voxeet.com.toolkit.main;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

/**
 * Created by romainbenmansour on 24/03/2017.
 */
public class VoxeetToolkit {

    private static VoxeetToolkit sInstance;


    /**
     * This function initializes the UI Toolkit SDK. It should be called as early as possible.
     *
     * @param application The voxeet sdk instance
     */
    public static synchronized VoxeetToolkit initialize(Application application) {
        sInstance = new VoxeetToolkit(application);

        return sInstance;
    }

    public static VoxeetToolkit getInstance() {
        return sInstance;
    }

    private VoxeetLifeCycleListener lifeCycleListener;

    private Application app;

    private boolean isInit = false;

    /**
     * Constructor of the VoxeetToolkit
     * @param application
     */

    private VoxeetToolkit(@NonNull Application application) {
        app = application;

        lifeCycleListener = new VoxeetLifeCycleListener(app.getApplicationContext());
        app.registerActivityLifecycleCallbacks(lifeCycleListener);

        isInit = true;
    }

    /**
     * Enables or disables the voxeet conference view. This custom view will appear and disappear
     * when join/leaving conference.
     *
     * @param enabled
     */
    public void enableOverlay(boolean enabled) {
        isInitialized();

        lifeCycleListener.onOverlayEnabled(enabled);
    }

    /**
     * Gets current activity. Useful when requiring new permissions. Can be null if current activity
     * is finishing or event the app.
     *
     * @return the current activity
     */
    public Activity getCurrentActivity() {
        return lifeCycleListener.getCurrentActivity();
    }

    /**
     * @throws IllegalStateException if SDK is not initialized.
     */
    private void isInitialized() {
        if (!isInit) {
            throw new IllegalStateException(
                    "The UI Toolkit has not been initialized, make sure to call " +
                            "Toolkit.validate() first.");
        }
    }

    /**
     * @return wether overlay is enabled or not
     */
    public boolean isEnabled() {
        return lifeCycleListener.isOverlayEnabled();
    }
}
