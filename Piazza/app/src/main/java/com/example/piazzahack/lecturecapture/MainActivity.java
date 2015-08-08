package com.example.piazzahack.lecturecapture;

// Media Recorder => Save into File
// Audio Recorder => Save into Buffer


/*
// /* This Demo is for recording what is being spoken to MIC and saving to 1 file, then reading from that 1 file till ends

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends Activity {

    private Button startButton, finishButton, playButton, stopButton;

    private MediaPlayer mediaPlayer; // Media player for playing recorded audio
    private MediaRecorder recorder; // MediaRecorder for recordig audio

    private String OUTPUT_FILE; // to store location to save recorded audio

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity_main to be the main xml file
        setContentView(R.layout.activity_main);

        // Link buttons together
        startButton = (Button) findViewById(R.id.startButton);
        finishButton = (Button) findViewById(R.id.finishButton);
        playButton = (Button) findViewById(R.id.playButton);
        stopButton = (Button) findViewById(R.id.stopButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });


        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonTapped(v);
            }
        });
        // 3gpp => 3rd generation partnership format
        // Android can only write to 3gpp format but can play from mp3 formats
        OUTPUT_FILE = Environment.getExternalStorageDirectory()+"/audiorecroded.3gpp";
    }

    // This function handles every button that is tapped
    public void buttonTapped(View view) {
        switch(view.getId()) {
            case R.id.startButton:
                try {
                    beginRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.finishButton:
                try {
                    stopRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.playButton:
                try {
                    playRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stopButton:
                try {
                    stopPlayback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void beginRecording() throws Exception {
        // Release any previous recording if any
        ditchMediaRecorder();

        File outFile = new File(OUTPUT_FILE);
        if(outFile.exists()) {
            // Remove the existing file as going to ovewrite it
            outFile.delete();
        }

        recorder = new MediaRecorder();
        // Record from phone's MIC
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // Adaptive Multi-Rate Narrow Band
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // Set the output file of the recording
        recorder.setOutputFile(OUTPUT_FILE);
        recorder.prepare(); // need handle Exception here
        // Start the recording
        recorder.start();
    }

    private void stopRecording() {
        if (recorder != null)
            recorder.stop();
    }

    // Start playing the recorded audio
    private void playRecording() throws Exception {
        ditchMediaPlayer();
        mediaPlayer = new MediaPlayer();
        // Source of where to play the recorded data
        mediaPlayer.setDataSource(OUTPUT_FILE);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    // Stop playing the recorded audio
    private void stopPlayback() {
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    // To release any previous recorders
    private void ditchMediaRecorder() {
        // Release any previous recorders
        if(recorder != null) {
            recorder.release();
        }
    }

    // To release any previous media players
    private void ditchMediaPlayer() {
        if(mediaPlayer != null)
        {
            try {
                mediaPlayer.release();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/

// /* This Demo is for playing back to you whatever is being listened right away, ECHO example
        import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioRecord;
        import android.media.AudioTrack;
        import android.media.MediaRecorder;
        import android.os.Bundle;
        import android.app.Activity;
        import android.content.Context;
        import android.view.Menu;
        import android.view.View;
        import android.widget.Button;

public class MainActivity extends Activity {
    AudioManager am = null;
    AudioRecord record =null;
    AudioTrack track =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
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
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);

        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        // Create an audio track to play sound from audioRecord
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);
    }

    private void recordAndPlay() {
        short[] lin = new short[1024];
        int num = 0;
        // Audio Manager is used for Volume and Ringer Controls
        // Get the Audio Manager
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        // Set the Audio Mode
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Start recording using the Audio Recorder
        record.startRecording();
        // Start playing using the Audio Track
        track.play();
        // Infinite loop
        while (true) {
            //
            num = record.read(lin, 0, 1024);
            track.write(lin, 0, num);
        }
    }

    boolean isSpeaker = false;
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
    }
}

// */

/*
myList = new ArrayList<String>();
        file = new File( "/sdcard/"+name+"/");
        file.mkdirs();
        File list[] = file.listFiles();

        for( int i=0; i< list.length; i++)
        {
        myList.add( list[i].getName() );
        }
        ListView listView = (ListView) findViewById(R.id.mylist);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, android.R.id.text1, myList);
        listView.setAdapter(adapter);

*/

/*
boolean m_isRun=true;
    public void loopback() {
            // Prepare the AudioRecord & AudioTrack
            try {
                buffersize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

            if (buffersize <= BUF_SIZE) {
                buffersize = BUF_SIZE;
            }
            Log.i(LOG_TAG,"Initializing Audio Record and Audio Playing objects");

            m_record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, buffersize * 1);

            m_track = new AudioTrack(AudioManager.STREAM_ALARM,
                    SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, buffersize * 1,
                    AudioTrack.MODE_STREAM);

            m_track.setPlaybackRate(SAMPLE_RATE);
        } catch (Throwable t) {
            Log.e("Error", "Initializing Audio Record and Play objects Failed "+t.getLocalizedMessage());
        }

        m_record.startRecording();
        Log.i(LOG_TAG,"Audio Recording started");
        m_track.play();
        Log.i(LOG_TAG,"Audio Playing started");

        while (m_isRun) {
            m_record.read(buffer, 0, BUF_SIZE);
            m_track.write(buffer, 0, buffer.length);
        }

        Log.i(LOG_TAG, "loopback exit");
    }

    private void do_loopback() {
        m_thread = new Thread(new Runnable() {
            public void run() {
                loopback();
            }
        });
 */




