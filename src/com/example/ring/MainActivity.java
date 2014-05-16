package com.example.ring;

import java.io.File;
import java.io.IOException;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

public class MainActivity extends Activity {

	private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    
    private static int numFiles = -1;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    
    private DeleteButton mDeleteButton = null;
    
    private EditText editText;

    private void onRecord(boolean start, int id) {
        if (start) {
            startRecording(id);
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start, int id) {
        if (start) {
            startPlaying(id);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(int id) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName + "/" + id + ".3gp");
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording(int id) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName + "/" + id + ".3gp");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
    
    private void deleteRecording(int id){
    	File file = new File(mFileName + "/" + id + ".3gp");
    	file.delete();
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
            	
            	try{
            		int id = Integer.parseInt(editText.getText().toString());
            		
	                onRecord(mStartRecording, id);
	                if (mStartRecording) {
	                    setText("Stop recording");
	                } else {
	                    setText("Start recording");
	                }
	                mStartRecording = !mStartRecording;
	            
            	}
            	
            	catch(Exception e){
            		//meh
            	}
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }
    

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
            	int id;
            	try{
            		id = Integer.parseInt(editText.getText().toString());
            				
	                onPlay(mStartPlaying, id);
	                if (mStartPlaying) {
	                    setText("Stop playing");
	                } else {
	                    setText("Start playing");
	                }
	                mStartPlaying = !mStartPlaying;
            	} catch(Exception e){
            		//eh
            	}
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }
    
    class DeleteButton extends Button{
    	OnClickListener clicker = new OnClickListener() {
    		public void onClick(View v) {
    			try{
            		int id = Integer.parseInt(editText.getText().toString());
            		deleteRecording(id);
            		
    			} catch(Exception e){
    				//bleh
    			}
    		}
    	};
    	
    	public DeleteButton(Context ctx) {
            super(ctx);
            setText("Delete");
            setOnClickListener(clicker);
        }
    }

    public MainActivity() {
    	File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + 
    			"/recordings");
    	dir.mkdirs();
    	
    	numFiles = dir.list().length;
        mFileName = dir.getAbsolutePath();
        //mFileName += "/audiorecordtest.3gp";
        
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LinearLayout ll = new LinearLayout(this);
        editText = new EditText(this);
        ll.addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mDeleteButton = new DeleteButton(this);
        ll.addView(mDeleteButton,
        	new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        setContentView(ll);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
