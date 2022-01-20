package com.voxeet.uxkit.service;

import android.content.Intent;
import android.os.Build;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.RemoteConference;
import android.telecom.RemoteConnection;

import androidx.annotation.RequiresApi;

import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ConferenceTelecomService extends ConnectionService {
    private static final ShortLogger Log = UXKitLogger.createLogger(ConferenceTelecomService.class);

    private ConferenceConnection connection;

    public ConferenceTelecomService() {
        super();
        Log.d("ConferenceTelecomService: ");
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
        Log.d("onUnbind: " + intent);
        return super.onUnbind(intent);
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        connection = new ConferenceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            connection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        Log.d("onCreateIncomingConnection: " + connectionManagerPhoneAccount + " " + connectionManagerPhoneAccount);
        return connection;
    }

    @Override
    public void onCreateIncomingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("onCreateIncomingConnectionFailed: ");
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onCreateOutgoingConnectionFailed(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("onCreateOutgoingConnectionFailed: ");
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        connection = new ConferenceConnection();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            connection.setConnectionProperties(Connection.PROPERTY_SELF_MANAGED);
        }
        Log.d("onCreateOutgoingConnection: ");
        return connection;
    }

    @Override
    public Connection onCreateOutgoingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        Log.d("onCreateOutgoingHandoverConnection: ");
        return super.onCreateOutgoingHandoverConnection(fromPhoneAccountHandle, request);
    }

    @Override
    public Connection onCreateIncomingHandoverConnection(PhoneAccountHandle fromPhoneAccountHandle, ConnectionRequest request) {
        Log.d("onCreateIncomingHandoverConnection: ");
        return super.onCreateIncomingHandoverConnection(fromPhoneAccountHandle, request);
    }

    @Override
    public void onHandoverFailed(ConnectionRequest request, int error) {
        Log.d("onHandoverFailed: ");
        super.onHandoverFailed(request, error);
    }

    @Override
    public void onConference(Connection connection1, Connection connection2) {
        Log.d("onConference: ");
        super.onConference(connection1, connection2);
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference conference) {
        Log.d("onRemoteConferenceAdded: ");
        super.onRemoteConferenceAdded(conference);
    }

    @Override
    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
        Log.d("onRemoteExistingConnectionAdded: ");
        super.onRemoteExistingConnectionAdded(connection);
    }

    @Override
    public void onConnectionServiceFocusLost() {
        Log.d("onConnectionServiceFocusLost: ");
        super.onConnectionServiceFocusLost();
    }

    @Override
    public void onConnectionServiceFocusGained() {
        Log.d("onConnectionServiceFocusGained: ");
        super.onConnectionServiceFocusGained();
    }
}
