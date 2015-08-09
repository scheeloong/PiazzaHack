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

    private boolean moved_back, looped;
    int seconds_prev;
    float speed; // ratio *
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        moved_back = false;
        looped = false;
        seconds_prev = 0;
        speed = 1;
        init();


        Button liveBtn=(Button) findViewById(R.id.liveButton);
        liveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                seconds_prev = 0;
            }
        });

        // Start playing using the Audio Track
        Button prevBtn=(Button) findViewById(R.id.prevButton);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Play from few seconds before
                seconds_prev = 5; // this is gonna be changed
            }
        });

        Button forwBtn=(Button) findViewById(R.id.forwButton);
        forwBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Play from few seconds before
                seconds_prev = 5;
            }
        });

        Button halfBtn=(Button) findViewById(R.id.lessSpeed);
        halfBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (speed > 0.75) {
                    speed -= 0.25;
                }
            }
        });


        Button doubleBtn=(Button) findViewById(R.id.moreSpeed);
        doubleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (speed < 2) {
                    speed += 0.25;
                }
            }
        });

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

    private void recordAndPlay() {
        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Start recording using the Audio Recorder
        record.startRecording();
        track.play();

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

            // play normally
            if (speed == 1 && seconds_prev == 0) {
                offset = count;
            }
            else {
                // if we're moving faster, update offset differently
                if (speed != 1) {
                    offset += speed*num;
                    offset = offset % BUFFER_SIZE_TRIAL;
                }

                // handle moving back
                if (!moved_back) {
                    offset = count - (1024 * 400);
                    moved_back = true;

                    // this might make offset negative
                    if (offset < 0) {
                        if (looped) {
                            offset += BUFFER_SIZE_TRIAL;
                        } else {
                            offset = 0;
                        }
                    }
                }

                // make sure offset is behind count
                if (offset >= count) {
                    speed = 1;
                    seconds_prev = 0;
                    // update UI?
                    moved_back = false;
                    offset = count;
                }
            }
            // play clip at the current offset point
            track.write(lin, offset, num);
            Log.d("OFFSET IS", String.valueOf(offset));

            // increment count the amount we just read
            count += num;
        }
    }
}
