package sdk.voxeet.com.toolkit.views.uitookit.nologic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.voxeet.toolkit.R;

import sdk.voxeet.com.toolkit.views.android.RoundedImageView;

/**
 * Created by romainbenmansour on 20/02/2017.
 */
public class VoxeetIncomingCallButton extends FrameLayout {
    private final String TAG = VoxeetIncomingCallButton.class.getSimpleName();

    private final float scaleRatio = 0.25f;
    private final float scaleUpRatio = 4f;
    private final float defaultScale = 1f;

    private final int scaleAnimationLength = 300;
    private final int instantAnimationLength = 0;

    private RoundedImageView image;

    private View backgroundIncoming;

    private View backgroundIncomingOverlay;

    private Drawable drawable;

    private IncomingCallListener incomingCallListener;

    /**
     * Instantiates a new Voxeet incoming call button.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetIncomingCallButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);

        init();
    }

    /**
     * Instantiates a new Voxeet incoming call button.
     *
     * @param context the context
     */
    public VoxeetIncomingCallButton(final Context context) {
        super(context);

        init();
    }

    /**
     * Sets incoming call listener.
     *
     * @param incomingCallListener the incoming call listener
     */
    public void setIncomingCallListener(IncomingCallListener incomingCallListener) {
        this.incomingCallListener = incomingCallListener;
    }

    /**
     * Sets the image src.
     *
     * @param drawable the drawable
     */
    public void setImageSrc(Drawable drawable) {
        image.setImageDrawable(drawable);
    }

    /**
     * Init.
     */
    private void init() {
        setClipToPadding(false);

        setClipChildren(false);

        View v = inflate(getContext(), R.layout.incoming_call_button, this);

        image = (RoundedImageView) v.findViewById(R.id.image);
        if (drawable != null)
            image.setImageDrawable(drawable);

        backgroundIncoming = v.findViewById(R.id.background_incoming);

        backgroundIncomingOverlay = v.findViewById(R.id.background_overlay_incoming);

        onViewCreated();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VoxeetIncomingCallButton);
        drawable = attributes.getDrawable(R.styleable.VoxeetIncomingCallButton_view_src);
        attributes.recycle();
    }

    private void onViewCreated() {
        setOnTouchListener(new OnTouchListener() {
            private static final int SCALE_UP = 0;
            private static final int SCALE_SAME = 1;
            private static final int SCALE_DOWN = 2;

            private float lastX;
            private float lastY;

            private float centreX;
            private float centreY;

            private boolean hasBeenTriggered = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();

                        centreX = event.getX();
                        centreY = event.getY();

                        backgroundIncoming.animate().scaleX(scaleUpRatio).scaleY(scaleUpRatio).setDuration(scaleAnimationLength).start();

                        image.animate().alpha(instantAnimationLength).setDuration(scaleAnimationLength);
                        break;
                    case MotionEvent.ACTION_UP:
                        backgroundIncoming.animate().scaleX(defaultScale).scaleY(defaultScale).setDuration(scaleAnimationLength).start();

                        backgroundIncomingOverlay.animate().scaleX(defaultScale).scaleY(defaultScale).setDuration(scaleAnimationLength).start();

                        image.animate().alpha(1).setDuration(scaleAnimationLength);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        switch ((shouldScale(event))) {
                            case SCALE_UP:
                                backgroundIncomingOverlay.animate().scaleXBy(scaleRatio).scaleYBy(scaleRatio).setDuration(instantAnimationLength).start();
                                break;
                            case SCALE_DOWN:
                                backgroundIncomingOverlay.animate().scaleXBy(-scaleRatio).scaleYBy(-scaleRatio).setDuration(instantAnimationLength).start();
                                break;
                            case SCALE_SAME:
                            default:
                                break;
                        }

                        if (hasClicked() && !hasBeenTriggered) {
                            hasBeenTriggered = true;
                            incomingCallListener.onIncomingCallSelected();
                        }

                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    default:
                        return false;
                }
                return true;
            }

            private boolean hasClicked() {
                return Math.abs(backgroundIncomingOverlay.getScaleX()) > (scaleUpRatio * 2 + 1) && incomingCallListener != null;
            }

            private int shouldScale(MotionEvent event) {
                float newX = event.getX();
                float newY = event.getY();

                if (lastX < newX && lastY < newY) { // bottom right swipe
                    if (centreX < newX && centreY < newY)
                        return SCALE_UP;
                    return SCALE_DOWN;
                } else if (lastX < newX && lastY > newY) { // top right swipe
                    if (centreX < newX && centreY < newY)
                        return SCALE_UP;
                    return SCALE_DOWN;
                } else if (lastX > newX && lastY > newY) { // top left swipe
                    if (centreX < newX && centreY < newY)
                        return SCALE_DOWN;
                    return SCALE_UP;
                } else if (lastX > newX && lastY < newY) { // bottom left swipe
                    if (centreX < newX && centreY < newY)
                        return SCALE_DOWN;
                    return SCALE_UP;
                }
                return SCALE_SAME;
            }
        });
    }

    /**
     * The interface Incoming call listener.
     */
    public interface IncomingCallListener {
        /**
         * On incoming call selected. Do whatever you want once this method has been called.
         */
        void onIncomingCallSelected();
    }
}