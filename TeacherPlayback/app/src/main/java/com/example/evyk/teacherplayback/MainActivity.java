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
import android.widget.TextView;

public class MainActivity extends Activity {
    AudioManager am = null;
    AudioRecord record = null;
    AudioTrack track = null;
    private int BUFFER_SIZE_TRIAL = 9999999;

    private boolean time_changed, looped;
    int seconds_prev;
    float speed; // ratio *
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        looped = false;
        time_changed = false;
        seconds_prev = 0;
        speed = 1;
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

    /*
    public void reset() {
        seconds_prev = 0;
        speed = 1;
        TextView t=(TextView)findViewById(R.id.secondsBack);
        t.setText("0 sec");
        TextView t2=(TextView)findViewById(R.id.speedGrade);
        t2.setText("1x");
        time_changed = false;
    }*/

    private void recordAndPlay() {
        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        final TextView t=(TextView)findViewById(R.id.secondsBack);
        final TextView t2=(TextView)findViewById(R.id.speedGrade);

        Button liveBtn=(Button) findViewById(R.id.liveButton);
        liveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                seconds_prev = 0;
                speed = 1;
                t.setText("0 sec");
                t2.setText("1x");
                time_changed = false;
            }
        });

        // Start playing using the Audio Track
        Button prevBtn=(Button) findViewById(R.id.prevButton);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                seconds_prev++;
                time_changed = true;
                if (seconds_prev != 0) {
                    t.setText("-" + String.valueOf(seconds_prev) + " sec");
                } else {
                    t.setText(String.valueOf(seconds_prev) + " sec");
                }
            }
        });

        Button forwBtn=(Button) findViewById(R.id.forwButton);
        forwBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (seconds_prev > 0) {
                    seconds_prev--;
                    time_changed = true;
                }
                if(seconds_prev != 0) {
                    t.setText("-" + String.valueOf(seconds_prev) + " sec");
                } else {
                    t.setText(String.valueOf(seconds_prev) + " sec");
                }
            }
        });

        Button lessBtn=(Button) findViewById(R.id.lessSpeed);
        lessBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (speed > 1) {
                    speed -= 0.25;
                }
                t2.setText(String.valueOf(speed) + " x");
            }
        });


        Button moreBtn=(Button) findViewById(R.id.moreSpeed);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (speed < 2) {
                    speed += 0.25;
                }
                t2.setText(String.valueOf(speed) + " x");
            }
        });

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
            if (seconds_prev == 0) {
                speed = 1;
                time_changed = false;
                offset = count;
            } else {
                // if we're moving faster, update offset differently
                offset += speed*num;
                offset = offset % BUFFER_SIZE_TRIAL;

                // handle moving back
                if (seconds_prev != 0 && time_changed) {
                    time_changed = false;
                    offset = count - (10000 * seconds_prev);

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
                    seconds_prev = 0;
                    speed = 1;
                    //liveBtn.performClick();
                    time_changed = false;
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
