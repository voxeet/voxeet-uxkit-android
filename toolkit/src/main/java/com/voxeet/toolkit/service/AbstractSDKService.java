package com.voxeet.toolkit.service;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.voxeet.push.utils.NotificationHelper;
import com.voxeet.sdk.VoxeetSdk;
import com.voxeet.sdk.events.sdk.ConferenceStateEvent;
import com.voxeet.sdk.json.ConferenceDestroyedPush;
import com.voxeet.sdk.json.ConferenceEnded;
import com.voxeet.sdk.services.ConferenceService;
import com.voxeet.sdk.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.services.conference.information.ConferenceState;
import com.voxeet.sdk.utils.Annotate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.annotation.Nullable;

@Annotate
public abstract class AbstractSDKService<BINDER extends SDKBinder> extends Service {

    private static final String TAG = AbstractSDKService.class.getSimpleName();
    protected EventBus eventBus;
    private Handler handler;

    @StringRes
    private int lastForeground;
    protected ConferenceState currentConferenceState;

    @NonNull
    private ConferenceService conferenceService;

    @Nullable
    public final VoxeetSdk sdk() {
        return VoxeetSdk.instance();
    }

    @NonNull
    public abstract BINDER onBind(@NonNull Intent intent);

    public AbstractSDKService() {
    }

    private Runnable stopForeground = new Runnable() {
        @Override
        public void run() {
            stopForeground(true);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        ConferenceService temp = VoxeetSdk.conference();
        if (null == temp) {
            stopSelf();
            return;
        }
        conferenceService = temp;

        lastForeground = -1;

        handler = new Handler();

        checkEventBus();

        NotificationHelper.createNotificationChannel(this);

        if (conferenceService.isLive())
            setForegroundState(getConferenceStateJoined());

        currentConferenceState = null;
    }

    @NonNull
    protected ConferenceState getConferenceStateFromSDK() {
        if (null != VoxeetSdk.instance()) {
            ConferenceInformation info = conferenceService.getCurrentConference();
            if (null != info) {
                return info.getConferenceState();
            }
        }

        return ConferenceState.DEFAULT;
    }

    @NonNull
    public ConferenceState getConferenceState() {
        ConferenceState state = currentConferenceState;
        if (null == state) state = getConferenceStateFromSDK();
        return state;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(@NonNull ConferenceStateEvent event) {
        switch (event.state) {
            case CREATING:
                setForegroundState(getConferenceStateCreating());
                break;
            case CREATED:
                setForegroundState(getConferenceStateCreated());
                break;
            case JOINING:
                setForegroundState(getConferenceStateJoining());
                break;
            case JOINED:
                setForegroundState(getConferenceStateJoined());
                break;
            case JOINED_ERROR:
                setForegroundState(getConferenceStateJoinedError());
                stopForeground();
                break;
            case CREATED_ERROR:
                stopForeground();
                break;
            case LEFT:
                setForegroundState(getConferenceStateLeft());
                stopForeground();
                break;
            case LEFT_ERROR:
                setForegroundState(getConferenceStateLeft());
                stopForeground();
                break;
            default:

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceDestroyedPush event) {
        setForegroundState(getConferenceStateEnd());
        stopForeground();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ConferenceEnded event) {
        setForegroundState(getConferenceStateEnd());
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

    protected void setForegroundState(@StringRes int string) {
        Class<? extends Activity> activity = getActivityClass();
        if (null != activity) {
            if (lastForeground == string) return;
            lastForeground = string;

            Intent notificationIntent = new Intent(this, getActivityClass());

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, NotificationHelper.getChannelId(this))
                    .setSmallIcon(getSmallIcon())
                    .setContentTitle(getContentTitle())
                    .setContentText(getString(string))
                    .setContentIntent(pendingIntent).build();

            startForeground(getNotificationId(), notification);
        } else {
            Log.d(TAG, "setForegroundState: impossible to set foreground, activity is null");
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

        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
            //eventBus = null;
        }

        handler.removeCallbacks(stopForeground);
        //handler.postDelayed(stopForeground, 2000);
        stopForeground.run();
        //stopSelf();
    }

    protected void checkEventBus() {
        eventBus = VoxeetSdk.instance().getEventBus();
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }
}
