package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

public class VoxeetOverlayToggleView extends AbstractVoxeetOverlayView {

    public VoxeetOverlayToggleView(@NonNull IExpandableViewProviderListener listener, Context context) {
        super(listener, context);
    }

    @Override
    final protected void onActionButtonClicked() {
        toggleSize();

        getExpandableViewProviderListener().onActionButtonClicked();
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_toggle_view;
    }
}