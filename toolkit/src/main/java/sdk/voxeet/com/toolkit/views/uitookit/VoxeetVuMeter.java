package sdk.voxeet.com.toolkit.views.uitookit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import com.voxeet.toolkit.R;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;

/**
 * Created by romainbenmansour on 20/02/2017.
 */
public class VoxeetVuMeter extends RoundedImageView {
    private final static int METER_UPDATE_TIMER = 20;
    private final String TAG = VoxeetVuMeter.class.getSimpleName();

    private final int TEMPORAL_SMOOTHING_COUNT = 5;

    private int width;

    @NonNull
    private Queue temporalSmoothing = new CircularFifoQueue(TEMPORAL_SMOOTHING_COUNT);

    /**
     * Instantiates a new Voxeet vu meter.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetVuMeter(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setImageDrawable(new ColorDrawable(getResources().getColor(R.color.grey999)));

        updateAttrs(attrs);

        setAlpha(0.65f);

        setMutateBackground(true);

        setOval(true);
    }

    /**
     * Sets the meter color.
     *
     * @param color the color
     */
    public void setMeterColor(int color) {
        setColorFilter(color);
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

        animate().scaleY(0).scaleX(0).setDuration(METER_UPDATE_TIMER).start();

        requestLayout();
    }

    private void setLevel(float level) {
        temporalSmoothing.add(level);

        float max = 0;
        float factor = 0;
        int count = 0;

        for (Object l : temporalSmoothing) {
            factor += ((count + 1) / (float) TEMPORAL_SMOOTHING_COUNT);
            max += (float) l * ((count + 1) / (float) TEMPORAL_SMOOTHING_COUNT);
            count++;

            if (factor != 0)
                max /= factor;

            scale(max);
        }

        postInvalidate();
    }

    /**
     * Update the vu meter.
     *
     * @param vuMeter the vu meter
     */
    public void updateMeter(int vuMeter) { // minimum is half width, max is full width
        if (vuMeter > 10)
            setLevel((((Math.abs(vuMeter / 32767.0f) * (0.5f * width)) + width / 2) / width));
    }

    private void scale(float scale) {
        animate().scaleY(scale).scaleX(scale).setDuration(METER_UPDATE_TIMER).start();
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
        temporalSmoothing.clear();

        setLevel(0f);
    }

    /**
     * Resets the queue.
     */
    public void reset() {
        temporalSmoothing.clear();

        setLevel(0f);
    }
}