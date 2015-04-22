package emit.esy.es.spyphone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Emil Makovac on 21/04/2015.
 */
public class BrokerService extends Service {

    private final String LOG_TAG = "BrokerService";
    String IMEI, username, password;
    volatile String id;
    Firebase ref, userRef, connRef;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        Firebase.setAndroidContext(this);
        password = getString(R.string.password);
        username = createUsername();

        //Authenticate user to firebase
        authenticateUser();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");

        setUpChildRef();

        return START_STICKY;
    }

    private void setUpChildRef() {
        Log.d(LOG_TAG, "setUpChildRef");
        if(id != null){

            Log.d(LOG_TAG, "setUpChildRef - id: " + id);

            setOnline(id, true);

            userRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String child = dataSnapshot.getValue().toString();
                    Log.d("ChildAdded", dataSnapshot.getKey() +" : "+child);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    String child = dataSnapshot.getValue().toString();
                    Log.d("ChildChanged", child);

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }
    }

    private String createUsername() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        return buildUserName(IMEI);
    }

    private String buildUserName(String imei) {
        return "device" + imei + "@spyphone.emit";
    }

    private void authenticateUser() {
        ref = new Firebase(getString(R.string.domain));
        ref.authWithPassword(username, password, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                id = authData.getUid();
                Log.d(LOG_TAG, "Authenticate user ID- " + id);
                setUpChildRef();
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
                id= result.get("uid").toString();
                setOnline(id, false);
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
        userRef = ref.child(id);
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("isOnline", b);
        Map<String, Object> conn = new HashMap<String, Object>();
        conn.put("isOnline", false);
        if(b){
           userRef.updateChildren(user);
           userRef.onDisconnect().updateChildren(conn);
        } else {
           userRef.setValue(user);
           userRef.onDisconnect().updateChildren(conn);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }



    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

}
