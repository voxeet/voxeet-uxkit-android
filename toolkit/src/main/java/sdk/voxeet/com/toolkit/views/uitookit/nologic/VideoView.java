package sdk.voxeet.com.toolkit.views.uitookit.nologic;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.video.EglBase;
import com.voxeet.android.media.video.RendererCommon;
import com.voxeet.toolkit.R;

import voxeet.com.sdk.core.VoxeetSdk;

/**
 * Created by romainbenmansour on 11/08/16.
 */
public class VideoView extends FrameLayout {
    private final String TAG = VideoView.class.getSimpleName();

    private boolean mIsAttached = false;

    /**
     * The Voxeet renderer.
     */
    protected VoxeetRenderer mRenderer;

    private String mPeerId;

    private MediaStream mMediaStream;

    private boolean autoUnAttach = false;

    private EglBase eglBase;

    private boolean shouldMirror = false;

    /**
     * Instantiates a new Video view.
     *
     * @param context the context
     */
    public VideoView(Context context) {
        super(context);

        init();
    }

    /**
     * Instantiates a new Video view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        updateAttrs(attrs);

        init();
    }

    private void updateAttrs(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.VideoView);
        shouldMirror = attributes.getBoolean(R.styleable.VideoView_mirrored, false);
        attributes.recycle();
    }

    private void init() {
        eglBase = EglBase.create();

        mRenderer = new VoxeetRenderer(getContext());

        addView(mRenderer);

        setSurfaceViewRenderer();
    }

    /**
     * Sets surface view renderer.
     */
    public void setSurfaceViewRenderer() {
        this.mRenderer.init(eglBase.getEglBaseContext(), null);

        this.mRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        this.mRenderer.setMirror(shouldMirror);
    }

    /**
     * Is auto un attach boolean.
     *
     * @return the boolean
     */
    public boolean isAutoUnAttach() {
        return autoUnAttach;
    }

    /**
     * Sets the videoview's behavior when already attached. Should it auto attach or just
     * stay attached to the old stream.
     *
     * @param autoUnAttach the auto un attach
     */
    public void setAutoUnAttach(boolean autoUnAttach) {
        this.autoUnAttach = autoUnAttach;
    }

    /**
     * Releases the renderer.
     */
    public void release() {
        this.mRenderer.release();
    }

    /**
     * Returns the renderer.
     *
     * @return the renderer
     */
    public VoxeetRenderer getRenderer() {
        return mRenderer;
    }

    private void setAttached(boolean attached) {
        mIsAttached = attached;
    }

    /**
     * Is attached boolean.
     *
     * @return the boolean
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Attach the stream associated with the peerId to the videoView.
     *
     * @param peerId      the peer id
     * @param mediaStream the media stream
     */
    public void attach(String peerId, MediaStream mediaStream) {
        if (isAttached() && mPeerId != null && mPeerId.equals(peerId)) // this user is already attached.
            return;

        if (autoUnAttach && isAttached())
            unAttach();

        if (!isAttached() && peerId != null && mediaStream != null && mediaStream.hasVideo()) {
            setAttached(true);

            mPeerId = peerId;

            mMediaStream = mediaStream;

            boolean result = VoxeetSdk.getInstance().getConferenceService().attachMediaStream(mediaStream, mRenderer);

        }
    }

    /**
     * Un attach the stream from the videoView.
     */
    public void unAttach() {
        if (isAttached() && mPeerId != null && mMediaStream != null) {
            VoxeetSdk.getInstance().getConferenceService().unAttachMediaStream(mMediaStream, mRenderer);

            mPeerId = null;

            mMediaStream = null;

            setAttached(false);
        }
    }

    /**
     * Gets the currently attached conference user's peer id.
     *
     * @return the peer id
     */
    public String getPeerId() {
        return mPeerId;
    }
}