package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

public class VoxeetOverlayBackView extends AbstractVoxeetOverlayView {

    public VoxeetOverlayBackView(@NonNull IExpandableViewProviderListener listener, @NonNull Context context) {
        super(listener, context);
    }

    @Override
    final protected void onActionButtonClicked() {
        getExpandableViewProviderListener().onActionButtonClicked();
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_back_view;
    }
}