package com.voxeet.uxkit.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.voxeet.VoxeetSDK;
import com.voxeet.android.media.MediaStream;
import com.voxeet.android.media.stream.MediaStreamType;
import com.voxeet.sdk.media.camera.CameraContext;
import com.voxeet.sdk.models.Participant;
import com.voxeet.sdk.views.VideoView;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;
import com.voxeet.uxkit.implementation.VoxeetConferenceView;

import java.lang.ref.WeakReference;

public class ConferenceViewRendererControl {

    private final static ShortLogger Log = UXKitLogger.createLogger(ConferenceViewRendererControl.class);

    @NonNull
    private WeakReference<VoxeetConferenceView> parent;

    @NonNull
    private WeakReference<VideoView> selfVideoView;

    @NonNull
    private WeakReference<VideoView> otherVideoView;

    private View.OnClickListener selectedFromSelf = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            VideoView view = otherVideoView.get();

            if (null != view) {
                switchCamera();
            }
        }
    };
    private boolean clickEnabled;


    private ConferenceViewRendererControl() {

    }

    public ConferenceViewRendererControl(@NonNull VoxeetConferenceView parent,
                                         @NonNull VideoView selfVideoView,
                                         @NonNull VideoView otherVideoView) {
        this();
        this.parent = new WeakReference<>(parent);
        this.selfVideoView = new WeakReference<>(selfVideoView);
        this.otherVideoView = new WeakReference<>(otherVideoView);
    }

    @Nullable
    private VoxeetConferenceView getParent() {
        return parent.get();
    }

    @NonNull
    private VideoView getSelfVideoView() {
        return selfVideoView.get();
    }

    @NonNull
    private VideoView getOtherVideoView() {
        return otherVideoView.get();
    }

    public void attachStreamToSelected(@NonNull String peerId,
                                       @NonNull MediaStream stream) {
        VideoView selectedView = getOtherVideoView();

        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";

        if (ownUserId.equals(peerId)) {
            attachStreamToSelf(stream);
        } else if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
            attachStreamToSelf(stream);

            if (!ownUserId.equals(peerId)) {
                selectedView.setOnClickListener(null);
                selectedView.setClickable(false);
                selectedView.unAttach();
                selectedView.setVisibility(View.VISIBLE);
                selectedView.attach(peerId, stream);
            } else {
                selectedView.unAttach();
                selectedView.setVisibility(View.GONE);
            }
        } else {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
            selectedView.unAttach();
            selectedView.setVisibility(View.VISIBLE);
            selectedView.attach(peerId, stream);
        }
    }

    public void detachStreamFromSelected() {
        VideoView selectedView = getOtherVideoView();
        VideoView selfVideoView = getSelfVideoView();

        String ownUserId = VoxeetSDK.session().getParticipantId();
        Participant user = VoxeetSDK.conference().findParticipantById(ownUserId);

        selectedView.unAttach();

        MediaStream stream = null != user ? user.streamsHandler().getFirst(MediaStreamType.Camera) : null;

        if (!ToolkitUtils.hasParticipants() && null != stream && stream.videoTracks().size() > 0) {
            attachStreamToSelf(stream);
        } else {
            selectedView.setVisibility(View.GONE);
            getParent().showSpeakerView();
        }
    }

    public void attachStreamToSelf(@Nullable MediaStream stream) {
        VideoView selectedView = getOtherVideoView();
        VideoView selfView = getSelfVideoView();

        CameraContext provider = VoxeetSDK.mediaDevice().getCameraContext();

        if (null != stream && stream.videoTracks().size() > 0) {
            String ownUserId = VoxeetSDK.session().getParticipantId();
            if (!ToolkitUtils.hasParticipants()) {
                selfView.unAttach();
                selfView.setVisibility(View.GONE);

                selectedView.setVideoFill();
                selectedView.setVisibility(View.VISIBLE);
                selectedView.attach(ownUserId, stream);
                setClickForSelectedIfNecessary();
                getParent().hideSpeakerView();
            } else {
                if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
                    selectedView.setOnClickListener(null);
                    selectedView.setClickable(false);
                    selectedView.unAttach();
                    selectedView.setVisibility(View.GONE);
                    getParent().showSpeakerView();
                }
                selfView.attach(VoxeetSDK.session().getParticipantId(), stream);
                selfView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setClickForSelectedIfNecessary() {
        VideoView selectedView = getOtherVideoView();
        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";

        if (clickEnabled && ownUserId.equals(selectedView.getPeerId())) {
            selectedView.setOnClickListener(selectedFromSelf);
        } else {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
        }
    }

    public void detachStreamFromSelf() {
        VideoView selectedView = getOtherVideoView();
        VideoView selfView = getSelfVideoView();

        if (selfView.isAttached()) {
            selfView.unAttach();
            selfView.setVisibility(View.GONE);
        }

        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (selectedView.isAttached() && ownUserId.equals(selectedView.getPeerId())) {
            selectedView.setOnClickListener(null);
            selectedView.setClickable(false);
            selectedView.unAttach();
            selectedView.setVisibility(View.GONE);
            getParent().showSpeakerView();
        }
    }

    public void switchCamera() {
        String ownUserId = VoxeetSDK.session().getParticipantId();
        if (null == ownUserId) ownUserId = "";

        VideoView self = selfVideoView.get();
        VideoView other = otherVideoView.get();

        VideoView finalVideoView = null;

        if (null != self && ownUserId.equals(self.getPeerId())) {
            finalVideoView = self;
        } else if (null != other && ownUserId.equals(other.getPeerId())) {
            finalVideoView = other;
        }

        if (null != finalVideoView && finalVideoView == self) { //force for now the effect only in the SELF VIDEO VIEW
            //until a proper animation is found

            //switchCamera should not trigger crash since it is only possible
            //to click when already capturing and ... rendering the camera

            ObjectAnimator animationFlip = ObjectAnimator.ofFloat(finalVideoView, View.ROTATION_Y, -180f, -360f);
            animationFlip.setInterpolator(new AccelerateDecelerateInterpolator());

            ObjectAnimator animationGrow = ObjectAnimator.ofFloat(finalVideoView, View.SCALE_Y, 1f, 1.15f, 1f);
            animationGrow.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(450).setStartDelay(450);
            animatorSet.playTogether(animationFlip, animationGrow);
            animatorSet.start();
        }

        VoxeetSDK.mediaDevice().switchCamera()
                .error(Log::e);
    }

    public void enableClick(boolean state) {
        clickEnabled = state;

        setClickForSelectedIfNecessary();
    }
}
