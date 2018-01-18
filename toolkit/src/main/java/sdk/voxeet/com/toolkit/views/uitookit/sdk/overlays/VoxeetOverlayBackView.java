package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetReplayMessageView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetExpandableView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;

public class VoxeetOverlayToggleView extends AbstractVoxeetOverlayView {

    public VoxeetOverlayToggleView(Context context) {
        super(context);
    }

    public VoxeetOverlayToggleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    final protected void onActionButtonClicked() {
        toggleSize();
    }

    @NonNull
    @Override
    protected AbstractVoxeetExpandableView createSubVoxeetView() {
        return new VoxeetReplayMessageView(getContext());
    }


    @Override
    protected int layout() {
        return R.layout.voxeet_overlay_back_view;
    }
}