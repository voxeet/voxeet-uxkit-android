package sdk.voxeet.com.toolkit.providers.containers;

import android.content.Context;
import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.providers.logics.IVoxeetSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;

public interface IVoxeetOverlayViewProvider {

    @NonNull
    AbstractVoxeetOverlayView createView(@NonNull Context context,
                                         @NonNull IVoxeetSubViewProvider provider,
                                         @NonNull OverlayState overlayState);

}
