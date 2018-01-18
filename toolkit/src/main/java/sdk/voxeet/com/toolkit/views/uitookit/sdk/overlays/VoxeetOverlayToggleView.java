package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetConferenceView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;

public class VoxeetOverlayConferenceView extends AbstractVoxeetOverlayView {

    public VoxeetOverlayConferenceView(Context context) {
        super(context);
    }

    public VoxeetOverlayConferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    final protected void onActionButtonClicked() {
        //TODO action
    }

    @NonNull
    @Override
    protected AbstractVoxeetExpandableView createSubVoxeetView() {
        return new VoxeetConferenceView(getContext());
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_toggle_view;
    }
}