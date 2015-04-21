package emit.esy.es.spyphone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Emil Makovac on 21/04/2015.
 */
public class BrokerService extends Service {

    private final String LOG_TAG = "BrokerService";
    String IMEI, username, id;
    String password;
    Firebase ref;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Firebase.setAndroidContext(this);
        password = getString(R.string.password);
        authenticateToFirebase();

    }

    private void authenticateToFirebase() {
        //Get IMEI, this will be username
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        username = buildUserName(IMEI);
        Log.d(LOG_TAG, "IMEI - " + IMEI + "Username - " + username);
        ref = new Firebase(getString(R.string.domain));
        //Authenticate user to firebase

        authenticateUser();

    }



    private String buildUserName(String imei) {
        return "device" + imei + "@spyphone.emit";
    }

    private void authenticateUser() {
        ref.authWithPassword(username, password, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                id = authData.getUid();
                Log.d(LOG_TAG, "Authenticate user ID- " + id);
                setOnline(id, true);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                //If error == no such user, create user
                switch (firebaseError.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        createUser();
                        break;
                }
            }
        });
    }

    private void createUser() {
        ref.createUser(username, password, new Firebase.ValueResultHandler<Map<String, Object>>() {

            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d(LOG_TAG, "CreateUser SUCCESS");
                Log.d(LOG_TAG, "CreateUser ID- " + result.get("uid").toString());
                setOnline(result.get("uid").toString(), false);
                //authenticate user
                authenticateUser();

            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Log.d(LOG_TAG, "CreateUser ERROR");
            }
        });
    }

    private void setOnline(String id, boolean b) {
        Firebase userRef = ref.child(id);
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("isOnline", b);
        if(b){
           userRef.updateChildren(user);
        } else {
           userRef.setValue(user);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        setOnline(id, false);
        super.onDestroy();
    }

}
