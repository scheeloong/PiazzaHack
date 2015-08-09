package com.example.evyk.teacherplayback;

// Media Recorder => Save into File
// Audio Recorder => Save into Buffer

// /* This Demo is for playing back to you whatever is being listened right away, ECHO example
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
    AudioRecord record =null;
    AudioTrack track =null;
    private int BUFFER_SIZE_TRIAL = 9999999;

    private int faster, writeNowHead, bla;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        faster = 0;
        writeNowHead = 1;
        bla = 0;
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

        // Create an audio track to play sound from audioRecord
        //track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

        // Note: Increasing maxJitter above will give you new error!!
        // Error Is: releaseBuffer() track 0xb491b780 disabled due to previous underrun, restarting

        Log.d("maxJitter*value is ", String.valueOf(maxJitter * 400));
    }

    private void recordAndPlay() {
        final short[] lin = new short[BUFFER_SIZE_TRIAL];
        //short[] lin = new short[1024];

        int num = 0;
        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        writeNowHead = 1;

        // Start recording using the Audio Recorder
        record.startRecording();
        track.play();



        Button liveBtn=(Button) findViewById(R.id.liveButton);
        liveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                faster = 0;
            }
        });

        // Start playing using the Audio Track
        Button prevBtn=(Button) findViewById(R.id.prevButton);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                faster = 1;
            }
        });

        Button forwBtn=(Button) findViewById(R.id.forwButton);
        forwBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                faster = 1;
            }
        });

        Button halfBtn=(Button) findViewById(R.id.halfSpeed);
        halfBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                faster = 1;
            }
        });


        Button doubleBtn=(Button) findViewById(R.id.doubleSpeed);
        doubleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                faster = 1;
            }
        });

        int count = 0;
        int offset = count;
        // Infinite loop
        while (true) {
            if (writeNowHead==1) {
                if (count >BUFFER_SIZE_TRIAL ) {
                    count = count % BUFFER_SIZE_TRIAL;
                    bla = 1;
                }
                //
                num = record.read(lin, count, 1024);
                //num = record.read(lin, 0, 1024);
                if (faster == 1) {
                    offset = count - (1024*100);
                    //track.write(lin, count - (1024*100), num);

                    if (offset < 0) {
                        if (bla == 1) {
                            offset += BUFFER_SIZE_TRIAL;
                        } else {
                            offset = 0;
                        }
                    }
                } else {
                    offset = count;
                    //track.write(lin, count, num);
                }
                track.write(lin, offset, num);
                Log.d("OFFSET IS", String.valueOf(offset));
                //track.write(lin, 0, num);
                count += num;
            }
        }
    }
}
// */