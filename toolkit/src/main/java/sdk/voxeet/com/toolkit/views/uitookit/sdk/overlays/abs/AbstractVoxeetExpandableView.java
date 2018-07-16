package sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs;

import android.content.Context;
import android.util.AttributeSet;

import sdk.voxeet.com.toolkit.views.uitookit.sdk.VoxeetView;

/**
 * Created by kevinleperf on 18/01/2018.
 */

public abstract class AbstractVoxeetExpandableView extends VoxeetView implements IExpandableViewListener {


    public AbstractVoxeetExpandableView(Context context) {
        super(context);
    }

    public AbstractVoxeetExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractVoxeetExpandableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
