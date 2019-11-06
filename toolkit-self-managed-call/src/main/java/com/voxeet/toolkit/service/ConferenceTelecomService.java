package com.voxeet.toolkit.service;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.RemoteConference;
import android.telecom.RemoteConnection;
import android.util.Log;

import com.voxeet.sdk.utils.Annotate;

@RequiresApi(api = Build.VERSION_CODES.M)
@Annotate
public class ConferenceTelecomService extends ConnectionService {
    private final static String TAG = ConferenceTelecomService.class.getSimpleName();
    private ConferenceConnection connection;

    public ConferenceTelecomService() {
        super();
        Log.d(TAG, "ConferenceTelecomService: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: " + intent);
        return super.onUnbind(intent);
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        connection = new ConferenceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            connection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        Log.d(TAG, "onCreateIncomingConnection: " + connectionManagerPhoneAccount + " " + connectionManagerPhoneAccount);
        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "onCreateIncomingConnectionFailed: ");
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d(TAG, "onCreateOutgoingConnectionFailed: ");
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        connection = new ConferenceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            connection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        Log.d(TAG, "onCreateOutgoingConnection: ");
        return connection;
    }

    @Override
    public Connection onCreateOutgoingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        Log.d(TAG, "onCreateOutgoingHandoverConnection: ");
        return super.onCreateOutgoingHandoverConnection(fromPhoneAccountHandle, request);
    }

    @Override
    public Connection onCreateIncomingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        Log.d(TAG, "onCreateIncomingHandoverConnection: ");
        return super.onCreateIncomingHandoverConnection(fromPhoneAccountHandle, request);
    }

    @Override
    public void onHandoverFailed(ConnectionRequest request, int error) {
        Log.d(TAG, "onHandoverFailed: ");
        super.onHandoverFailed(request, error);
    }

    @Override
    public void onConference(Connection connection1, Connection connection2) {
        Log.d(TAG, "onConference: ");
        super.onConference(connection1, connection2);
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference conference) {
        Log.d(TAG, "onRemoteConferenceAdded: ");
        super.onRemoteConferenceAdded(conference);
    }

    @Override
    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
        Log.d(TAG, "onRemoteExistingConnectionAdded: ");
        super.onRemoteExistingConnectionAdded(connection);
    }

    @Override
    public void onConnectionServiceFocusLost() {
        Log.d(TAG, "onConnectionServiceFocusLost: ");
        super.onConnectionServiceFocusLost();
    }

    @Override
    public void onConnectionServiceFocusGained() {
        Log.d(TAG, "onConnectionServiceFocusGained: ");
        super.onConnectionServiceFocusGained();
    }
}
