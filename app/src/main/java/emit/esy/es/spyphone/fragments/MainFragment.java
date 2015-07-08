package emit.esy.es.spyphone.fragments;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.BrokerService;

/**
 * Created by Emil Makovac on 23/06/2015.
 */
public class MainFragment extends Fragment {

    boolean isRunning;
    TextView runingTitle;
    Button btn_start_stop;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.main_fragment, container, false);


        isRunning = isMyServiceRunning(BrokerService.class);
        runingTitle = (TextView) rootView.findViewById(R.id.runingTitle);
        btn_start_stop = (Button) rootView.findViewById(R.id.btn_start_stop);

        btn_start_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRunning){
                    getActivity().startService(new Intent(getActivity(), BrokerService.class));
                    isRunning = true;
                    runingTitle.setText("Service is running");
                    btn_start_stop.setBackgroundResource(R.drawable.btn_stop);
                    rootView.setBackgroundResource(R.drawable.stop_background);
                    saveToSP(true);
                } else {
                    getActivity().stopService(new Intent(getActivity(), BrokerService.class));
                    isRunning = false;
                    runingTitle.setText("Service is not running");
                   // btn_start_stop.setText("Start Service");
                    btn_start_stop.setBackgroundResource(R.drawable.btn_start);
                    rootView.setBackgroundResource(R.drawable.start_background);
                    saveToSP(false);
                }
            }
        });

        if(isRunning){
            runingTitle.setText("Service is running");
            btn_start_stop.setBackgroundResource(R.drawable.btn_stop);
            rootView.setBackgroundResource(R.drawable.stop_background);
        } else {
            runingTitle.setText("Service is not running");
            btn_start_stop.setBackgroundResource(R.drawable.btn_start);
            rootView.setBackgroundResource(R.drawable.start_background);
        }

        return rootView;
    }

    private void saveToSP(boolean b) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.sharedPrefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isRunning", b);
        editor.commit();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
