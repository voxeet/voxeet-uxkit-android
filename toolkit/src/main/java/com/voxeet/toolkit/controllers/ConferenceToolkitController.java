package com.voxeet.toolkit.controllers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.services.conference.information.ConferenceInformation;
import com.voxeet.sdk.events.promises.NotInConferenceException;
import com.voxeet.sdk.events.success.ConferenceRefreshedEvent;
import com.voxeet.sdk.json.UserInfo;
import com.voxeet.sdk.json.internal.MetadataHolder;
import com.voxeet.sdk.json.internal.ParamsHolder;
import com.voxeet.sdk.models.ConferenceResponse;
import com.voxeet.toolkit.configuration.Configuration;
import com.voxeet.toolkit.implementation.overlays.OverlayState;
import com.voxeet.toolkit.implementation.overlays.abs.IExpandableViewProviderListener;
import com.voxeet.toolkit.providers.containers.DefaultConferenceProvider;
import com.voxeet.toolkit.providers.logics.DefaultConferenceSubViewProvider;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.PromiseSolver;
import eu.codlab.simplepromise.solve.Solver;

/**
 * Created by kevinleperf on 15/01/2018.
 */

public class ConferenceToolkitController extends AbstractConferenceToolkitController implements IExpandableViewProviderListener {

    private boolean mScreenShareEnabled = false;
    public final Configuration Configuration = new Configuration();

    public ConferenceToolkitController(Context context, EventBus eventbus, OverlayState overlay) {
        super(context, eventbus);

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

        return VoxeetSdk.conference().join(conferenceId);
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
                VoxeetSdk.conference().create(conferenceAlias, metadataHolder, paramsHolder)
                        .then(new PromiseExec<ConferenceResponse, Boolean>() {
                            @Override
                            public void onCall(@Nullable ConferenceResponse result, @NonNull Solver<Boolean> s) {
                                Log.d("ConferenceToolkitController", "onCall: creating conference done");
                                s.resolve(VoxeetSdk.conference().join(result.getConfId()));
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

    public Promise<List<ConferenceRefreshedEvent>> invite(@NonNull String conferenceId, @NonNull List<UserInfo> to_invite) {
        return VoxeetSdk.conference().inviteUserInfos(conferenceId, to_invite);
    }

    @Deprecated
    public Promise<List<ConferenceRefreshedEvent>> invite(@NonNull List<UserInfo> to_invite) {
        ConferenceInformation information = VoxeetSdk.conference().getCurrentConferenceInformation();
        if (null != information) {
            String conferenceId = information.getConference().getConferenceId();

            return VoxeetSdk.conference().inviteUserInfos(conferenceId, to_invite);
        }

        return new Promise<>(new PromiseSolver<List<ConferenceRefreshedEvent>>() {
            @Override
            public void onCall(@NonNull Solver<List<ConferenceRefreshedEvent>> solver) {
                try {
                    throw new NotInConferenceException();
                } catch (@NonNull NotInConferenceException e) {
                    solver.reject(e);
                }
            }
        });
    }


    /*@Nullable
    public UserInfo getInvitedUserFromCache(@NonNull String externalId) {
        return (null != mCachedInvited) ? mCachedInvited.get(externalId) : null;
    }*/

    public boolean isScreenShareEnabled() {
        return mScreenShareEnabled;
    }

    public ConferenceToolkitController setScreenShareEnabled(boolean state) {
        mScreenShareEnabled = state;
        return this;
    }

    private void internalJoin(@Nullable UserInfo from_invitation) {
        //mCachedInvited.clear();
        //if (null != from_invitation) {
        //    mCachedInvited.put(from_invitation.getExternalId(), from_invitation);
        //}

        VoxeetToolkit.getInstance().enable(this);
        enable(true);
    }
}
