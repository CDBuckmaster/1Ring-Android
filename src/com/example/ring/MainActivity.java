package com.example.ring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

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
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

public class MainActivity extends Activity {
	
	private final int REQUEST_ENABLE_BT = 8000;
	//private final UUID APPLICATION_UUID = UUID.fromString("b9ed3f50-de50-11e3-8b68-0800200c9a66");
	private final UUID APPLICATION_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    
    private static int numFiles = -1;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    
    private DeleteButton mDeleteButton = null;
    
    private EditText editText;
    
    private Vector<String> stringArray = new Vector<String>();
    private ArrayAdapter<String> mArrayAdapter;
    private ListView listView;
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket = null;
    private IntentFilter filter;
    private BroadcastReceiver mReceiver;

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
        ll.setOrientation(LinearLayout.VERTICAL);
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
        
        listView = new ListView(this);
        mArrayAdapter = new ArrayAdapter<String>(this,
        		android.R.layout.simple_list_item_1, stringArray);
        listView.setAdapter(mArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				String item = (String) listView.getItemAtPosition(pos);
				unregisterReceiver(mReceiver);
		    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(item.substring(item.indexOf('\n')+ 1));
				Toast.makeText(getApplicationContext(),"You selected : " + item.substring(item.indexOf('\n')+ 1),Toast.LENGTH_SHORT).show();
				new connectThread(device).start();
			}
        	
        });
        ll.addView(listView, 
        		new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        
        setContentView(ll);
        
        startBluetooth();
    }
    

	private void startBluetooth(){
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
        
        Log.e("Debug", String.valueOf(mBluetoothAdapter.startDiscovery()));
        
        // Create a BroadcastReceiver for ACTION_FOUND
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        };
        // Register the BroadcastReceiver
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        
        
	     
    }
    private class connectThread extends Thread{
    	private BluetoothSocket bs;
    	public connectThread(BluetoothDevice bd){
    		//Log.e("UUID",bd.getUuids().toString());
    		try{
    			bs = bd.createRfcommSocketToServiceRecord(APPLICATION_UUID);
    		} catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    	
    	public void run(){
    		mBluetoothAdapter.cancelDiscovery();
    		try{
    			bs.connect();
    			//manage in a separate thread
        		new communicationThread(bs).start();
    		} catch(IOException e){
    			e.printStackTrace();
    			try{
    				bs.close();
    			} catch(IOException e1){
    				e1.printStackTrace();
    			}
    		}
    		
    		
    		return;
    	}
    	
    	public void cancel(){
    		try{
    			bs.close();
    		} catch(IOException e){
    			
    		}
    	}
    }
    
    
    private class communicationThread extends Thread{
    	private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket socket;
    	
    	public communicationThread(BluetoothSocket bs){
    		socket = bs;
    		InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
    	}
    	
    	public void run(){
    		byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes;
            Log.e("DEBUG","It liiiiiives");
            write(timeMessage());
            
            while (true) {
                try {
                    // Read from the InputStream
                	String s = "";
                	Boolean start = false;
                    while((bytes = mmInStream.read(buffer)) != -1){
                    	for(int i = 0; i < bytes; i++){
                    		if(buffer[i] != 0){
                    			//Log.e("BYTE", String.valueOf((char)buffer[i]));
                    			if((char)buffer[i] == '*'){
                    				start = true;
                    			}
                    			else if((char)buffer[i] == '#' && start){
                    				handleMessage(s);
                    				s = "";
                    				start = false;
                    				break;
                    			}
                    			else if(start){
                    				s += (char)buffer[i];
                    			}
                    			
                    		}
                    	}
                    	Arrays.fill(buffer, (byte) 0);
                    }
                    
                	
                	
                    
                    
                } catch (IOException e) {
                    break;
                }
            }
            
            cancel();
    	}
    	
    	public void write(String message) {
            try {
                mmOutStream.write(message.getBytes());
            } catch (IOException e) { }
        }
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }
    
    private void handleMessage(String message){
    	Log.e("MESSAGE", message);
    	String parts[] = message.split(";");
    	if(parts[0].equals("playstart")){
    		startPlaying(Integer.parseInt(parts[1]));
    	}
    	else if(parts[0].equals("playend")){
    		stopPlaying();
    	}
    	else if(parts[0].equals("recordstart")){
    		startRecording(Integer.parseInt(parts[1]));
    	}
    	else if(parts[0].equals("recordend")){
    		stopRecording();
    	}
    	else if(parts[0].equals("delete")){
    		deleteRecording(Integer.parseInt(parts[1]));
    	}
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
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	if(filter != null){
    		unregisterReceiver(mReceiver);   		
    	}
    }
    
    private String timeMessage(){
    	Calendar c = Calendar.getInstance();
    	return "*time;" + ((c.get(Calendar.HOUR) > 9) ? c.get(Calendar.HOUR) : ("0" + c.get(Calendar.HOUR)))  + 
    			";" + ((c.get(Calendar.MINUTE) > 9) ? c.get(Calendar.MINUTE) : ("0" + c.get(Calendar.MINUTE))) +
    			";" + ((c.get(Calendar.SECOND) > 9) ? c.get(Calendar.SECOND) : ("0" + c.get(Calendar.SECOND))) +
    			";" + ((c.get(Calendar.DAY_OF_MONTH) > 9) ? c.get(Calendar.DAY_OF_MONTH) : ("0" + c.get(Calendar.DAY_OF_MONTH))) +
    			";" + ((c.get(Calendar.MONTH) > 9) ? c.get(Calendar.MONTH) : ("0" + c.get(Calendar.MONTH))) +
    			";" + c.get(Calendar.YEAR) + "#";
    }

}
