package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays;

import android.content.Context;
import android.support.annotation.NonNull;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.providers.logics.IVoxeetSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.AbstractVoxeetOverlayView;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;

public class VoxeetOverlayToggleView extends AbstractVoxeetOverlayView {

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param listener the listener used to create the sub view
     * @param provider
     * @param context  the context
     * @param overlay
     */
    public VoxeetOverlayToggleView(@NonNull IExpandableViewProviderListener listener,
                                   @NonNull IVoxeetSubViewProvider provider,
                                   @NonNull Context context,
                                   @NonNull OverlayState overlay) {
        super(listener, provider, context, overlay);
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