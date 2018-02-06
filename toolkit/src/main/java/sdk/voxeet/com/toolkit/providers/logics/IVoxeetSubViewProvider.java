package sdk.voxeet.com.toolkit.providers.logics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;

public interface IVoxeetSubViewProvider {

    @NonNull
    AbstractVoxeetExpandableView createView(Context context, OverlayState overlayState);

}
