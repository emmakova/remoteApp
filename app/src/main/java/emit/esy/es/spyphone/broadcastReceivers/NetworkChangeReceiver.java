package emit.esy.es.spyphone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import emit.esy.es.spyphone.services.BrokerService;
import emit.esy.es.spyphone.util.NetworkUtil;

/**
 * Created by Emil Makovac on 22/04/2015.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean internetStatus = NetworkUtil.getConnectivityStatus(context);

        if(internetStatus){
            //Start broker service
            Log.d(LOG_TAG, "Internet available. Starting service");
            context.stopService(new Intent(context, BrokerService.class));
            context.startService(new Intent(context, BrokerService.class));
        } else {
            //stop broker service
            Log.d(LOG_TAG, "Internet not available. Stopping service");
            context.stopService(new Intent(context, BrokerService.class));

        }
    }
}
