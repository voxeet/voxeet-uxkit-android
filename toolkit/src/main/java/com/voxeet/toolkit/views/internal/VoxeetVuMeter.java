package com.voxeet.toolkit.views.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.voxeet.sdk.views.RoundedFrameLayout;
import com.voxeet.toolkit.R;

public class VoxeetVuMeter extends RoundedFrameLayout {
    private final static int METER_UPDATE_TIMER = 20;
    private final String TAG = VoxeetVuMeter.class.getSimpleName();
    private final int white;
    private final int yellowOrange;
    private final View view;

    private int width;

    /**
     * Instantiates a new Voxeet vu meter.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetVuMeter(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        //setImageDrawable(new ColorDrawable(getResources().getColor(R.color.grey999)));

        updateAttrs(attrs);

        Resources resources = context.getResources();
        white = resources.getColor(R.color.white);
        yellowOrange = resources.getColor(R.color.yellowOrange);

        view = new View(context);
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(view);
    }

    /**
     * Sets the meter color.
     *
     * @param color the color
     */
    public void setMeterColor(int color) {

    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetVuMeter);
        ColorStateList color = attributes.getColorStateList(R.styleable.VoxeetVuMeter_background_color);
        attributes.recycle();

        if (color != null)
            setMeterColor(color.getColorForState(getDrawableState(), 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w / 2;

        //animate().scaleY(0).scaleX(0).setDuration(METER_UPDATE_TIMER).start();

        requestLayout();
    }

    /**
     * Update the vu meter.
     *
     * @param vuMeter the vu meter
     */
    public void updateMeter(double vuMeter) {
        Log.d(TAG, "updateMeter: vuMeter:=" + vuMeter);
        view.setBackgroundColor(vuMeter > 0.02 ? yellowOrange : white);
    }

    /**
     * On participant selected.
     */
    public void onParticipantSelected() {
        reset();
    }

    /**
     * On participant unselected.
     */
    public void onParticipantUnselected() {
        updateMeter(0);
    }

    /**
     * Resets the queue.
     */
    public void reset() {
        updateMeter(0);
    }
}