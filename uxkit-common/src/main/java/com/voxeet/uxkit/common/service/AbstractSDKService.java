package com.voxeet.uxkit.common.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.voxeet.VoxeetSDK;
import com.voxeet.sdk.events.sdk.ConferenceStatusUpdatedEvent;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.push.utils.NotificationHelper;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceStatus;
import com.voxeet.sdk.utils.Opt;
import com.voxeet.uxkit.common.R;
import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.annotation.Nullable;

public abstract class AbstractSDKService<BINDER extends SDKBinder> extends Service {

    private static final ShortLogger Log = UXKitLogger.createLogger(AbstractSDKService.class);

    @Nullable
    protected EventBus eventBus;
    private Handler handler;

    @StringRes
    private int lastForeground;
    protected ConferenceStatus currentConferenceState;

    @NonNull
    private ConferenceService conferenceService;

    @Nullable
    public final VoxeetSDK sdk() {
        return VoxeetSDK.instance();
    }

    @NonNull
    public abstract BINDER onBind(@NonNull Intent intent);

    public AbstractSDKService() {
    }

    private Runnable stopForeground = () -> stopForeground(true);

    @Override
    public void onCreate() {
        super.onCreate();

        conferenceService = VoxeetSDK.conference();

        lastForeground = -1;

        handler = new Handler();

        checkEventBus();

        NotificationHelper.createNotificationChannel(this);

        if (conferenceService.isLive())
            setForegroundState(ConferenceStatus.JOINED, getConferenceStateJoined());

        currentConferenceState = null;
    }

    @NonNull
    protected ConferenceStatus getConferenceStateFromSDK() {
        ConferenceInformation info = conferenceService.getCurrentConference();
        if (null != info) {
            return info.getConferenceState();
        }

        return ConferenceStatus.DEFAULT;
    }

    @NonNull
    public ConferenceStatus getConferenceState() {
        ConferenceStatus state = currentConferenceState;
        if (null == state) state = getConferenceStateFromSDK();
        return state;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceStatusUpdatedEvent event) {
        switch (event.state) {
            case CREATING:
                setForegroundState(event.state, getConferenceStateCreating());
                break;
            case CREATED:
                setForegroundState(event.state, getConferenceStateCreated());
                break;
            case JOINING:
                setForegroundState(event.state, getConferenceStateJoining());
                break;
            case JOINED:
                setForegroundState(event.state, getConferenceStateJoined());
                break;
            case LEFT:
            case ERROR:
                setForegroundState(event.state, getConferenceStateLeft());
                stopForeground();
                break;
            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPush event) {
        setForegroundState(ConferenceStatus.ENDED, getConferenceStateEnd());
        stopForeground();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEnded event) {
        setForegroundState(ConferenceStatus.ENDED, getConferenceStateEnd());
        stopForeground();
    }

    @StringRes
    protected abstract int getConferenceStateCreating();

    @StringRes
    protected abstract int getConferenceStateCreated();

    @StringRes
    protected abstract int getConferenceStateJoining();

    @StringRes
    protected abstract int getConferenceStateJoined();

    @StringRes
    protected abstract int getConferenceStateJoinedError();

    @StringRes
    protected abstract int getConferenceStateLeft();

    @StringRes
    protected abstract int getConferenceStateEnd();

    protected void beforeNotificationBuild(NotificationCompat.Builder builder,
                                           ConferenceStatus status) {

    }

    protected void afterNotificationBuild(NotificationCompat.Builder builder,
                                           ConferenceStatus status) {

    }

    protected void setForegroundState(@NonNull ConferenceStatus status, @StringRes int string) {
        Class<? extends Activity> activity = getActivityClass();
        if (null != activity) {
            //enable reset notif
            //if (lastForeground == string) return;
            lastForeground = string;

            Intent notificationIntent = new Intent(this, getActivityClass());

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationHelper.getChannelId(this));
            beforeNotificationBuild(builder, status);
            builder.setSmallIcon(getSmallIcon())
                    .setContentTitle(getContentTitle())
                    .setContentText(getString(string))
                    .setContentIntent(pendingIntent);
            afterNotificationBuild(builder, status);

            Notification notification = builder.build();

            startForeground(getNotificationId(), notification);
        } else {
            Log.d("setForegroundState: impossible to set foreground, activity is null");
        }
    }


    protected abstract int getNotificationId();

    @DrawableRes
    protected abstract int getSmallIcon();

    @NonNull
    protected abstract String getContentTitle();

    protected abstract Class<? extends Activity> getActivityClass();

    protected void stopForeground() {
        lastForeground = -1;

        //if (Opt.of(eventBus).then(e -> e.isRegistered(this)).or(false)) {
        //    eventBus.unregister(this);
        //      eventBus = null;
        //}

        handler.removeCallbacks(stopForeground);
        stopForeground.run();
    }

    protected void checkEventBus() {
        eventBus = Opt.of(VoxeetSDK.instance()).then(VoxeetSDK::getEventBus).orNull();
        if (null != eventBus && !eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }
}
