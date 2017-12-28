package fr.voxeet.sdk.sample.connection;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccountHandle;
import android.telecom.RemoteConference;
import android.telecom.RemoteConnection;
import android.telecom.TelecomManager;
import android.util.Log;

/**
 * Created by romainbenmansour on 13/03/2017.
 */

@TargetApi(Build.VERSION_CODES.M)
public class VoxeetConnectionService extends ConnectionService {

    private final String TAG = VoxeetConnectionService.class.getSimpleName();

    private TelecomManager telecomManager;

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        Log.e(TAG, "bindService");
        return super.bindService(service, conn, flags);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "1");
        return super.onUnbind(intent);
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "2");
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.e(TAG, "3");
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onConference(Connection connection1, Connection connection2) {
        Log.e(TAG, "4");
        super.onConference(connection1, connection2);
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference conference) {
        Log.e(TAG, "5");
        super.onRemoteConferenceAdded(conference);
    }

    @Override
    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
        Log.e(TAG, "6");
        super.onRemoteExistingConnectionAdded(connection);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(TAG, "on create ");
    }
}
