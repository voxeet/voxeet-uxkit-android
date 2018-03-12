package sdk.voxeet.com.toolkit.providers.rootview;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import sdk.voxeet.com.toolkit.main.VoxeetToolkit;

/**
 * Created by kevinleperf on 12/03/2018.
 */

public class DefaultRootViewProvider extends AbstractRootViewProvider {

    private static final String TAG = DefaultRootViewProvider.class.getSimpleName();

    /**
     * @param application a valid application which be called to obtain events
     * @param toolkit
     */
    public DefaultRootViewProvider(@NonNull Application application, @NonNull VoxeetToolkit toolkit) {
        super(application, toolkit);
    }

    @NonNull
    @Override
    public ViewGroup getRootView() {
        Activity activity = getCurrentActivity();
        Log.d(TAG, "getRootView: " + activity);
        if (null != activity) {
            return (ViewGroup) activity.getWindow().getDecorView().getRootView();
        } else {
            return null;
        }
    }
}
