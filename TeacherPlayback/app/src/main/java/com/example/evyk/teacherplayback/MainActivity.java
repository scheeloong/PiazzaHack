package com.example.evyk.teacherplayback;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    AudioManager am = null;
    AudioRecord record = null;
    AudioTrack track = null;
    private int BUFFER_SIZE_TRIAL = 9999999;

    private boolean faster, moved_back, looped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        faster = false;
        moved_back = false;
        looped = false;
        init();

        // Start a new thread to run recordAndPlay() function
        (new Thread() {
            @Override
            public void run() {
                recordAndPlay();
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

        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
    }

    private int count = 0;

    private void recordAndPlay() {
        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Start recording using the Audio Recorder
        record.startRecording();
        track.play();

        // Start playing using the Audio Track
        Button playBtn = (Button) findViewById(R.id.playButton);

        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Play from few seconds before and faster
                faster = true;
            }
        });

        Button stopBtn = (Button) findViewById(R.id.stopButton);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // back to normal
                faster = false;
                moved_back = false;
            }
        });

        final short[] lin = new short[BUFFER_SIZE_TRIAL];
        int num;
        int count = 0;
        int offset = count;
        // Infinite loop
        while (true) {
            if (count > BUFFER_SIZE_TRIAL) {
                count = count % BUFFER_SIZE_TRIAL;
                looped = true;
            }
            //
            num = record.read(lin, count, 1024);
            if (faster) {
                if (!moved_back) {
                    offset = count - (1024 * 400);
                    moved_back = true;
                } else {
                    offset += 1.4 * num;
                    offset = offset % BUFFER_SIZE_TRIAL;
                    if (offset >= count) {
                        faster = false;
                        moved_back = false;
                        offset = count;
                    }
                }

                if (offset < 0) {
                    if (looped) {
                        offset += BUFFER_SIZE_TRIAL;
                    } else {
                        offset = 0;
                    }
                }
            } else {
                offset = count;
            }
            track.write(lin, offset, num);
            Log.d("OFFSET IS", String.valueOf(offset));
            count += num;
        }
    }
}