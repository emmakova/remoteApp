package emit.esy.es.spyphone;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import emit.esy.es.spyphone.fragments.MainFragment;

/**
 * Created by Emil Makovac on 16/06/2015.
 */
public class StartStopActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_stop_layout);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


}
