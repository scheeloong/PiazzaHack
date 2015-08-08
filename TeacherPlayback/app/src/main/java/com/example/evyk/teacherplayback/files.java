package com.example.evyk.teacherplayback;



// /* This Demo is for playing back to you whatever is being listened right away, ECHO example
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class files extends Activity {
    AudioManager am = null;
    AudioRecord record =null;
    AudioTrack player =null;
    String LOG_TAG ="LOGLOGLOGOURLOGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        init();

        recording = true;

        // Start playing using the Audio Track
        Button playBtn=(Button) findViewById(R.id.playButton);
        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(LOG_TAG, "playing");
                play();
                //track.play();
            }
        });

        Button stopBtn=(Button) findViewById(R.id.stopButton);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(LOG_TAG, "stopping");
                recording = false;
                if (record != null) {
                    record.stop();
                    record.release();
                }
            }
        });


        // Start a new thread to run recordAndPlay() function
        (new Thread() {
            @Override
            public void run() {
                record();
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void init() {
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, min);
/*
        // Create an audio track to play sound from audioRecord
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 9999999*64, AudioTrack.MODE_STATIC);
*/
    }

    private void record() {
        int audioSource = MediaRecorder.AudioSource.MIC;
        int sampleRateInHz = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        byte Data[] = new byte[bufferSizeInBytes];

        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Start recording using the Audio Recorder
        record.startRecording();

        String filepath = Environment.getExternalStorageDirectory().getPath();
        FileOutputStream os = null;
        try {
            Log.d(LOG_TAG, "path is " + filepath);
            os = new FileOutputStream(filepath+"/record.pcm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(recording) {
            record.read(Data, 0, Data.length);
            try {
                os.write(Data, 0, bufferSizeInBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean recording;

    private void play() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        final String FILENAME = filepath+"/record.pcm";
        File file = new File(FILENAME);
        int audioLength = (int)file.length();
        byte filedata[] = new byte[audioLength];
        try{
            InputStream inputStream = new BufferedInputStream(new FileInputStream(FILENAME));
            int lengthOfAudioClip = inputStream.read(filedata, 0, audioLength);
            player = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, audioLength, AudioTrack.MODE_STATIC);
            player.write(filedata, 0, lengthOfAudioClip); // make this -10 seconds later
            // this is for 1.5x player.setPlaybackRate(player.getNativeOutputSampleRate());
            player.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*boolean isSpeaker = false;
    public void modeChange(View view) {
        Button modeBtn=(Button) findViewById(R.id.stopButton);
        if (isSpeaker == true) {
            am.setSpeakerphoneOn(false);
            isSpeaker = false;
            modeBtn.setText("Call Mode");
        } else {
            am.setSpeakerphoneOn(true);
            isSpeaker = true;
            modeBtn.setText("Speaker Mode");
        }
    }
    boolean isPlaying=true;
    public void play(View view){
        Button playBtn=(Button) findViewById(R.id.playButton);
        if(isPlaying){
            record.stop();
            track.pause();
            isPlaying=false;
            playBtn.setText("Play");
        }else{
            record.startRecording();
            track.play();
            isPlaying=true;
            playBtn.setText("Pause");
        }
    }*/
}