package sdk.voxeet.com.toolkit.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.codlab.simplepromise.Promise;
import sdk.voxeet.com.toolkit.main.VoxeetToolkit;
import sdk.voxeet.com.toolkit.providers.containers.DefaultConferenceProvider;
import sdk.voxeet.com.toolkit.providers.logics.DefaultConferenceSubViewProvider;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.OverlayState;
import sdk.voxeet.com.toolkit.views.uitookit.sdk.overlays.abs.IExpandableViewProviderListener;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.json.UserInfo;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private Map<String, UserInfo> mCachedInvited;

    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

        mCachedInvited = new HashMap<>();

        setDefaultOverlayState(overlay);
        setVoxeetOverlayViewProvider(new DefaultConferenceProvider(this));
        setVoxeetSubViewProvider(new DefaultConferenceSubViewProvider());
    }

    @Override
    protected boolean validFilter(String conference) {
        return isEnabled();
    }

    @Override
    public void onActionButtonClicked() {
        //nothing to do
    }

    public Promise<Boolean> join(@NonNull String conference_id, @Nullable UserInfo from_invitation) {
        mCachedInvited.clear();
        if (null != from_invitation) {
            mCachedInvited.put(from_invitation.getExternalId(), from_invitation);
        }

        VoxeetToolkit.getInstance().enable(this);
        enable(true);

        return VoxeetSdk.getInstance().getConferenceService().join(conference_id);
    }

    public Promise<Boolean> join(@NonNull String conference_id) {
        return join(conference_id, null);
    }

    public Promise<List<ConferenceRefreshedEvent>> invite(@NonNull List<UserInfo> to_invite) {
        List<String> to_list = new ArrayList<>();

        for (UserInfo infos : to_invite) {
            if (null != infos) {
                to_list.add(infos.getExternalId());
                mCachedInvited.put(infos.getExternalId(), infos);
            }
        }

        return VoxeetSdk.getInstance().getConferenceService().invite(to_list);
    }

    @Nullable
    public UserInfo getInvitedUserFromCache(@NonNull String externalId) {
        return (null != mCachedInvited) ? mCachedInvited.get(externalId) : null;
    }

    public Promise<Boolean> demo() {
        VoxeetToolkit.getInstance().enable(this);
        enable(true);

        return VoxeetSdk.getInstance().getConferenceService().demo();
    }

    public Promise<Boolean> create() {
        VoxeetToolkit.getInstance().enable(this);
        enable(true);

        return VoxeetSdk.getInstance().getConferenceService().create();
    }
}
