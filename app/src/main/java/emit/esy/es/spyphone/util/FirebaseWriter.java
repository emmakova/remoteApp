package emit.esy.es.spyphone.util;

import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import emit.esy.es.spyphone.R;

/**
 * Created by Emil Makovac on 11/06/2015.
 */
public class FirebaseWriter {

    Firebase ref, userRef;
    Context mcontext;
    String musername, mpassword;
    Object value;


    public FirebaseWriter(Context context, String username, String password, Object value){
        mcontext = context;
        musername = username;
        mpassword = password;
        if(value != null) {
            this.value = value;
            authenticateUser();
        }
    }

    public void authenticateUser() {
        ref = new Firebase(mcontext.getString(R.string.domain));
        ref.authWithPassword(musername, mpassword, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                userRef = ref.child(authData.getUid()).child("androidRead");
                userRef.setValue(value);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d("FirebaseWriter", firebaseError.toString());
            }
        });

    }
}
