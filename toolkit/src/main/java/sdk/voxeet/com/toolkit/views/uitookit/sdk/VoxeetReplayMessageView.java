package sdk.voxeet.com.toolkit.views.uitookit.sdk;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.voxeet.android.media.MediaStream;
import com.voxeet.toolkit.R;

import java.util.HashMap;
import java.util.Map;

import sdk.voxeet.com.toolkit.utils.CornerHelper;
import sdk.voxeet.com.toolkit.views.uitookit.nologic.VideoView;
import sdk.voxeet.com.toolkit.utils.IParticipantViewListener;
import voxeet.com.sdk.core.VoxeetPreferences;
import voxeet.com.sdk.models.impl.DefaultConferenceUser;
import voxeet.com.sdk.utils.ScreenHelper;

/**
 * Created by romainbenmansour on 11/08/16.
 */
public class VoxeetConferenceView extends VoxeetView implements IParticipantViewListener {
    private final String TAG = VoxeetConferenceView.class.getSimpleName();

    private final int defaultWidth = getResources().getDimensionPixelSize(R.dimen.conference_view_width);

    private final int defaultHeight = getResources().getDimensionPixelSize(R.dimen.conference_view_height);

    private boolean isMaxedOut;

    private VoxeetParticipantView participantView;

    private VoxeetConferenceBarView conferenceBarView;

    private VoxeetCurrentSpeakerView speakerView;

    private ImageView toggleSize;

    private AnimationHandler animationHandler;

    private ViewGroup layoutTimer;

    private VideoView selectedView;

    private VideoView selfView;

    private ViewGroup container;

    private ViewGroup layoutParticipant;

    private GestureDetector gestureDetector;

    private DisplayMetrics dm;

    private WindowManager windowManager;
    private Map<String, MediaStream> mMediaStreams;
    private VoxeetTimer voxeetTimer;

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     */
    public VoxeetConferenceView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new Voxeet conference view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VoxeetConferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMediaStreamsUpdated(Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamsUpdated(mediaStreams);

        mMediaStreams = mediaStreams;
    }

    @Override
    public void onMediaStreamUpdated(String userId, Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamUpdated(userId, mediaStreams);

        MediaStream mediaStream = mediaStreams.get(userId);
        if (userId.equalsIgnoreCase(VoxeetPreferences.id()) && mediaStream != null) {
            if (mediaStream.hasVideo()) {
                selfView.setVisibility(VISIBLE);
                selfView.attach(userId, mediaStream);
            } else {
                selfView.setVisibility(GONE);
                selfView.unAttach();
            }
        }
    }

    @Override
    public void onMediaStreamsListUpdated(Map<String, MediaStream> mediaStreams) {
        super.onMediaStreamsListUpdated(mediaStreams);

        mMediaStreams = mediaStreams;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int previousWidth = dm.widthPixels;
        int previousHeight = dm.heightPixels;

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);

        if (isMaxedOut)
            animationHandler.toLandScape(250, previousWidth, dm.widthPixels, previousHeight, dm.heightPixels);
        else
            CornerHelper.sendToCorner(this, windowManager, getContext());
    }

    @Override
    public void init() {
        mMediaStreams = new HashMap<>();

        animationHandler = new AnimationHandler();

        dm = new DisplayMetrics();

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);

        gestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        setOnTouchListener(new OnTouchListener() {
            private float dX;

            private float dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    toggleSize();
                } else if (!isMaxedOut) { // drag n drop only when minimized
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = getX() - event.getRawX();
                            dY = getY() - event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float x = event.getRawX() + dX;
                            float y = event.getRawY() + dY;
                            if (x < 0)
                                x = 0;
                            if (y < ScreenHelper.getStatusBarHeight(getContext()))
                                y = ScreenHelper.getStatusBarHeight(getContext());

                            animate().x(event.getRawX() + dX).y(event.getRawY() + dY)
                                    .setDuration(0).start();
                            break;
                        case MotionEvent.ACTION_UP:
                            CornerHelper.sendToCorner(VoxeetConferenceView.this, windowManager, getContext());
                        default:
                            return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Toggles view's size to full screen or default size.
     */
    private void toggleSize() {
        isMaxedOut = getWidth() > defaultWidth && getHeight() > defaultHeight;

        if (!isMaxedOut) { // maximize
            if(selfView.isAttached()) {
                selfView.setVisibility(View.VISIBLE);
            }
            maxOutView();
        } else { // minimize
            selfView.setVisibility(View.GONE);
            minimizeView();
        }
    }

    private void onViewToggled() {
        isMaxedOut = !isMaxedOut;

        toggleBackground();

        layoutTimer.setVisibility(isMaxedOut ? GONE : VISIBLE);

        layoutParticipant.setVisibility(isMaxedOut ? VISIBLE : GONE);

        conferenceBarView.onToggleSize(isMaxedOut);
    }

    private void maxOutView() {
        ViewGroup view = (ViewGroup) getParent();
        if (view != null)
            animationHandler.expand(1000, view.getWidth(), view.getHeight());
    }

    private void minimizeView() {
        animationHandler.collapse(1000, defaultWidth, defaultHeight);
    }

    private void toggleBackground() {
        if (isMaxedOut)
            container.setBackgroundResource(R.drawable.background_conference_view_maxed_out);
        else
            container.setBackgroundResource(R.drawable.background_conference_view);
    }

    @Override
    protected int layout() {
        return R.layout.voxeet_conference_view;
    }

    @Override
    protected void bindView(View view) {
        container = (ViewGroup) view.findViewById(R.id.conference_view_container);

        layoutParticipant = (ViewGroup) view.findViewById(R.id.layout_participant);

        speakerView = (VoxeetCurrentSpeakerView) view.findViewById(R.id.current_speaker_view);

        selectedView = (VideoView) view.findViewById(R.id.selected_video_view);
        selectedView.setAutoUnAttach(true);

        selfView = (VideoView) view.findViewById(R.id.self_video_view);

        layoutTimer = (ViewGroup) view.findViewById(R.id.layout_timer);

        toggleSize = (ImageView) view.findViewById(R.id.toggle_size);
        toggleSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSize();
            }
        });

        conferenceBarView = (VoxeetConferenceBarView) view.findViewById(R.id.conference_bar_view);

        participantView = (VoxeetParticipantView) view.findViewById(R.id.participant_view);
        participantView.setParticipantListener(this);

        voxeetTimer = view.findViewById(R.id.voxeet_timer);

        //addListeners for voxeet dispatch events
        addListener(speakerView);
        addListener(conferenceBarView);
        addListener(participantView);
        addListener(voxeetTimer);
    }

    @Override
    public void onParticipantSelected(DefaultConferenceUser user) {
        speakerView.lockScreen(user.getUserId());

        MediaStream mediaStream = mMediaStreams.get(user.getUserId());
        if (mediaStream != null) {
            if (mediaStream.hasVideo()) {
                selectedView.setVisibility(VISIBLE);
                selectedView.attach(user.getUserId(), mediaStream);

                speakerView.setVisibility(GONE);
                speakerView.onPause();
            }
        }
    }

    @Override
    public void onParticipantUnselected(DefaultConferenceUser user) {
        selectedView.setVisibility(GONE);
        selectedView.unAttach();

        speakerView.unlockScreen();
        speakerView.setVisibility(VISIBLE);
    }

    private boolean isOverlay() {
        return getParent() != null && getParent() == getRootView();
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    /**
     * Animation class
     */
    private class AnimationHandler {
        private final long animatonDuration = 200;

        /**
         * Animation when orientation changed to landscape.
         *
         * @param duration       the duration
         * @param previousWidth  the previous width
         * @param targetWidth    the target width
         * @param previousHeight the previous height
         * @param targetHeight   the target height
         */
        void toLandScape(int duration, final int previousWidth, final int targetWidth, final int previousHeight, final int targetHeight) {
            animate().x(0).y(0).setDuration(0).start();

            ValueAnimator height = ValueAnimator.ofInt(previousHeight, targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().height = value;

                    container.getLayoutParams().height = value;

                    requestLayout();
                }
            });

            ValueAnimator width = ValueAnimator.ofInt(previousWidth, targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().width = value;

                    container.getLayoutParams().width = value;

                    requestLayout();
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);
            animatorSet.start();
        }

        /**
         * Expand animation.
         *
         * @param duration     the duration
         * @param targetWidth  the target width
         * @param targetHeight the target height
         */
        void expand(int duration, final int targetWidth, final int targetHeight) {
            animate().x(0).y(0).setDuration(300).start();

            ValueAnimator height = ValueAnimator.ofInt(getHeight(), targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().height = value;

                    container.getLayoutParams().height = value;

                    requestLayout();
                }
            });

            ValueAnimator width = ValueAnimator.ofInt(getWidth(), targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().width = value;

                    container.getLayoutParams().width = value;

                    requestLayout();
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    onViewToggled();
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);
            animatorSet.start();
        }

        /**
         * Collapse the view to default size.
         *
         * @param duration     the duration
         * @param targetWidth  the target width
         * @param targetHeight the target height
         */
        void collapse(int duration, final int targetWidth, final int targetHeight) {
            if (isOverlay()) {
                animate().x(dm.widthPixels - defaultWidth).y(ScreenHelper.actionBar(getContext()) + ScreenHelper.getStatusBarHeight(getContext())).setDuration(300).start();
            } else if (getParent() != null) {
                ViewGroup view = (ViewGroup) getParent();
                animate().x(dm.widthPixels - defaultWidth - view.getPaddingRight()).y(view.getPaddingTop()).setDuration(200).start();
            }

            ValueAnimator height = ValueAnimator.ofInt(getHeight(), targetHeight);
            height.setDuration(duration);
            height.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().height = value;

                    container.getLayoutParams().height = value;

                    requestLayout();
                }
            });

            ValueAnimator width = ValueAnimator.ofInt(getWidth(), targetWidth);
            width.setDuration(duration);
            width.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();

                    getLayoutParams().width = value;

                    container.getLayoutParams().width = value;

                    requestLayout();
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    onViewToggled();
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.setDuration(animatonDuration);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(width, height);
            animatorSet.start();
        }
    }
}