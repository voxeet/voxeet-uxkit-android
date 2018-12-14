package com.voxeet.toolkit.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.containers.DefaultConferenceProvider;
import com.voxeet.toolkit.providers.logics.DefaultConferenceSubViewProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;
import voxeet.com.sdk.core.VoxeetSdk;
import voxeet.com.sdk.events.success.ConferenceRefreshedEvent;
import voxeet.com.sdk.json.UserInfo;
import voxeet.com.sdk.json.internal.MetadataHolder;
import voxeet.com.sdk.json.internal.ParamsHolder;
import voxeet.com.sdk.models.ConferenceResponse;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private Map<String, UserInfo> mCachedInvited;
    private boolean mScreenShareEnabled;

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

    public Promise<Boolean> joinUsingConferenceId(@NonNull String conferenceId, @Nullable UserInfo from_invitation) {
        Log.d("IncomingBundleChecker", "join: conferenceId := " + conferenceId);
        internalJoin(from_invitation);

        return VoxeetSdk.getInstance().getConferenceService().join(conferenceId);
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias) {
        return join(conferenceAlias, new MetadataHolder(), new ParamsHolder());
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @param from_invitation the user who invited this instance - if any
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias, @Nullable UserInfo from_invitation) {
        return join(conferenceAlias, null, null, from_invitation);
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @param metadataHolder  a possible metadataholder - if any
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias,
                                 @Nullable MetadataHolder metadataHolder) {
        Log.d("IncomingBundleChecker", "join: conferenceAlias := " + conferenceAlias);

        return join(conferenceAlias, metadataHolder, null, null);
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @param metadataHolder  a possible metadataholder - if any
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias,
                                 @Nullable MetadataHolder metadataHolder,
                                 @Nullable ParamsHolder paramsHolder) {
        Log.d("IncomingBundleChecker", "join: conferenceAlias := " + conferenceAlias);

        return join(conferenceAlias, metadataHolder, paramsHolder, null);
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @param metadataHolder  a possible metadataholder - if any
     * @param from_invitation the user who invited this instance - if any
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias,
                                 @Nullable MetadataHolder metadataHolder,
                                 @Nullable UserInfo from_invitation) {
        return join(conferenceAlias, metadataHolder, null, from_invitation);
    }

    /**
     * Join a given conferenceAlias with parametered metadata
     *
     * @param conferenceAlias the valid alias to join
     * @param metadataHolder  a possible metadataholder - if any
     * @param paramsHolder    a possible paramsHolder - if any
     * @param from_invitation the user who invited this instance - if any
     * @return a promise to resolve
     */
    public Promise<Boolean> join(@NonNull String conferenceAlias,
                                 @Nullable MetadataHolder metadataHolder,
                                 @Nullable ParamsHolder paramsHolder,
                                 @Nullable UserInfo from_invitation) {
        Log.d("IncomingBundleChecker", "join: conferenceAlias := " + conferenceAlias);
        internalJoin(from_invitation);

        return new Promise<>(new PromiseSolver<Boolean>() {
            @Override
            public void onCall(@NonNull Solver<Boolean> solver) {
                VoxeetSdk.getInstance().getConferenceService().create(conferenceAlias, metadataHolder, paramsHolder)
                        .then(new PromiseExec<ConferenceResponse, Boolean>() {
                            @Override
                            public void onCall(@Nullable ConferenceResponse result, @NonNull Solver<Boolean> s) {
                                Log.d("ConferenceToolkitController", "onCall: creating conference done");
                                s.resolve(VoxeetSdk.getInstance().getConferenceService().join(result.getConfId()));
                            }
                        })
                        .then(new PromiseExec<Boolean, Object>() {
                            @Override
                            public void onCall(@Nullable Boolean result, @NonNull Solver<Object> s) {
                                Log.d("ConferenceToolkitController", "onCall: joining done " + result);
                                solver.resolve(result);
                            }
                        })
                        .error(new ErrorPromise() {
                            @Override
                            public void onError(@NonNull Throwable error) {
                                solver.reject(error);
                            }
                        });
            }
        });
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

    public boolean isScreenShareEnabled() {
        return mScreenShareEnabled;
    }

    public ConferenceToolkitController setScreenShareEnabled(boolean state) {
        mScreenShareEnabled = state;
        return this;
    }

    private void internalJoin(@Nullable UserInfo from_invitation) {
        mCachedInvited.clear();
        if (null != from_invitation) {
            mCachedInvited.put(from_invitation.getExternalId(), from_invitation);
        }

        VoxeetToolkit.getInstance().enable(this);
        enable(true);
    }
}
