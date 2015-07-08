package emit.esy.es.spyphone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.BrokerService;
import emit.esy.es.spyphone.util.NetworkUtil;

/**
 * Created by Emil Makovac on 22/04/2015.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NetworkChangeReceiver";
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        boolean internetStatus = NetworkUtil.getConnectivityStatus(context);

        if(internetStatus){
            if(isRunning()) {
                Log.d(LOG_TAG, "Internet available. Starting service");
                context.stopService(new Intent(context, BrokerService.class));
                context.startService(new Intent(context, BrokerService.class));
            }
        } else {
            //stop broker service
            Log.d(LOG_TAG, "Internet not available. Stopping service");
            context.stopService(new Intent(context, BrokerService.class));

        }
    }

    private boolean isRunning() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedPrefs), context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isRunning", false);
    }
}
