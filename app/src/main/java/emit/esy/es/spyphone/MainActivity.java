package emit.esy.es.spyphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import emit.esy.es.spyphone.fragments.LoginFragment;


public class MainActivity extends Activity {

    private final static String LOG_TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(userExist()){
            startActivity(new Intent(getBaseContext(), StartStopActivity.class));
        } else {
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.mainActivityContainer, new LoginFragment())
                        .commit();
            }
        }
    }

    private boolean userExist() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.sharedPrefs), Context.MODE_PRIVATE);
        String uname = sp.getString("username", null);
        if(uname != null){
            return true;
        }
        return false;
    }
}
