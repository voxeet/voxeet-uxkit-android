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

import com.voxeet.uxkit.common.UXKitLogger;
import com.voxeet.uxkit.common.logging.ShortLogger;

@TargetApi(Build.VERSION_CODES.M)
public class VoxeetConnectionService extends ConnectionService {

    private final static ShortLogger Log = UXKitLogger.createLogger(VoxeetConnectionService.class);

    private TelecomManager telecomManager;

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        Log.d("bindService");
        return super.bindService(service, conn, flags);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public Connection onCreateIncomingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("onCreateIncomingConnection");
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public Connection onCreateOutgoingConnection(PhoneAccountHandle connectionManagerPhoneAccount, ConnectionRequest request) {
        Log.d("onCreateOutgoingConnection");
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request);
    }

    @Override
    public void onConference(Connection connection1, Connection connection2) {
        Log.d("onConference");
        super.onConference(connection1, connection2);
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference conference) {
        Log.d("onRemoteConferenceAdded");
        super.onRemoteConferenceAdded(conference);
    }

    @Override
    public void onRemoteExistingConnectionAdded(RemoteConnection connection) {
        Log.d("onRemoteExistingConnectionAdded");
        super.onRemoteExistingConnectionAdded(connection);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("onCreate");
    }
}
