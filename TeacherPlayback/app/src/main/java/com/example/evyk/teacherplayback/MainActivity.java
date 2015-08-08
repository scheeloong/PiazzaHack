package com.example.evyk.teacherplayback;

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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    AudioManager am = null;
    AudioRecord record =null;
    AudioTrack track =null;
    private int BUFFER_SIZE_TRIAL = 9999999;

    private int writeNow, writeNowHead, bla;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        writeNow = 0;
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
    }

    private int count = 0;

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

        // Start playing using the Audio Track
        Button playBtn=(Button) findViewById(R.id.playButton);

        class StopTask extends TimerTask {
            public void run() {
                Log.d("LOGLOGLOG", "timer stopped");
                count=0;
                track.setVolume(0);
                track.setPlaybackRate(8000);
            }
        }

        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                writeNow = 1;
            }
        });

        Button stopBtn=(Button) findViewById(R.id.stopButton);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //track.reloadStaticData();
                //track.setPlaybackRate(10000);
                // Play from few seconds before
                writeNow = 0;
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
                if (writeNow == 1) {
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

    /*
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
*/
}
