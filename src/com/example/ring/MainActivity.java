package com.example.ring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	private final int ADD_REMINDER = 1;
	private final int DELETE_REMINDER = 2;
	private final int ERROR = 3;

	private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    
    private static int numFiles = -1;
    
    private boolean connected = false;
    private HashMap<Long, Reminder> reminders = new HashMap<Long, Reminder>();
    
    private Handler incomingHandler;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    
    private MediaPlayer   mPlayer = null;
    
    private SubmitButton mSubmitButton = null;
    private DeleteButton mDeleteButton = null;
    
    private EditText editText;
    private EditText redText;
    private	EditText greenText;
    private EditText blueText;
    
    private EditText startText;
    private EditText endText;
    
    private Vector<String> stringArray = new Vector<String>();
    private ArrayAdapter<String> mArrayAdapter;
    private ListView listView;
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket = null;
    private IntentFilter filter;
    private BroadcastReceiver mReceiver;
    
    private OutThread outThread;
    private InThread inThread;

    private void onRecord(boolean start, long id) {
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

    private void startPlaying(long id) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName + "/" + id + ".3gp");
            mPlayer.prepare();
            mPlayer.setLooping(false);
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording(long id) {
    	Log.e("debug", id + "");
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
    
    private void deleteRecording(long id){
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
    
    class SubmitButton extends Button{
    	OnClickListener clicker = new OnClickListener(){
    		public void onClick(View v){
    			if(connected){
	    			try{
	    				if(redText.getText().length() == 0 || greenText.getText().length() == 0 ||
	    						blueText.getText().length() == 0 || editText.getText().length() == 0){
	    					throw new Exception();
	    				}
	    				int r = Integer.parseInt(redText.getText().toString());
	    				int g = Integer.parseInt(greenText.getText().toString());
	    				int b = Integer.parseInt(blueText.getText().toString());
	    				long c = rgbToLong(r, g, b);
	    				String id = editText.getText().toString();
	    				long start = Long.parseLong(startText.getText().toString());
	    				String e = endText.getText().toString();
	    				long end;
	    				if(e.length() == 0){
	    					end = -1;
	    				}
	    				else{
	    					end = Long.parseLong(e);
	    				}
	    				reminders.put(Long.parseLong(id), new Reminder(Long.parseLong(id), c, start, end));
	    				mArrayAdapter.add("ID:" + id + " Colour:" + c);
	    				mArrayAdapter.notifyDataSetChanged();
	    				Message msg = outThread.outgoingHandler.obtainMessage(ADD_REMINDER, "*newreminder;" + id + ";" + c + ";" + start + ";" + end + "#");
	    				msg.sendToTarget();
	    				
	    			} catch(Exception e){
	    				Toast.makeText(getApplicationContext(),"Message not sent",Toast.LENGTH_SHORT).show();
	    				e.printStackTrace();
	    				}
	    		}
    		}
    	};
    	public SubmitButton(Context ctx) {
            super(ctx);
            setText("Submit");
            setOnClickListener(clicker);
        }
    }

    
    class DeleteButton extends Button{
    	OnClickListener clicker = new OnClickListener() {
    		public void onClick(View v) {
    			try{
            		long id = Long.parseLong(editText.getText().toString());
            		deleteRecording(id);
            		reminders.remove(id);
            		Message msg = outThread.outgoingHandler.obtainMessage(ADD_REMINDER, "*deletereminder;" + id + "#");
    				msg.sendToTarget();
    				for(int i = 0; i < mArrayAdapter.getCount(); i ++){
    					String s = mArrayAdapter.getItem(i);
    					if(s.split(" ")[0].split(":")[1].equals(String.valueOf(id))){
    						mArrayAdapter.remove(s);
    					}
    				}
    				mArrayAdapter.notifyDataSetChanged();
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
        
        fileToReminders();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout l0 = new LinearLayout(this);
        ll.addView(l0, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        TextView idText = new TextView(this);
        idText.setText("ID:");
        l0.addView(idText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        editText = new EditText(this);
        l0.addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        LinearLayout l2 = new LinearLayout(this);
        ll.addView(l2, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        TextView rgbText = new TextView(this);
        rgbText.setText("RGB:");
        l2.addView(rgbText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        redText = new EditText(this);
        l2.addView(redText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        greenText = new EditText(this);
        l2.addView(greenText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        blueText = new EditText(this);
        l2.addView(blueText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        LinearLayout l3 = new LinearLayout(this);
        ll.addView(l3, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        TextView timeText = new TextView(this);
        timeText.setText("Start and end times:");
        l3.addView(timeText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        startText = new EditText(this);
        l3.addView(startText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        endText = new EditText(this);
        l3.addView(endText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        
        mSubmitButton = new SubmitButton(this);
        ll.addView(mSubmitButton,
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
				if(!connected){
					try{
					unregisterReceiver(mReceiver);
					}catch (Exception e){}
			    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(item.substring(item.indexOf('\n')+ 1));
					Toast.makeText(getApplicationContext(),"You selected : " + item.substring(item.indexOf('\n')+ 1),Toast.LENGTH_SHORT).show();
					new connectThread(device).start();
					connected = true;
		            mArrayAdapter.clear();
		            for(long i: reminders.keySet()){
		            	mArrayAdapter.add("ID:" + reminders.get(i).id + " Colour:" + reminders.get(i).colour);
		            	mArrayAdapter.notifyDataSetChanged();
		            }
				}
				else{
					long iId = Long.parseLong(item.split(" ")[0].split(":")[1]);
					Log.e("DEBUG", String.valueOf(iId));
					reminders.remove(iId);
					deleteRecording(iId);
					Message msg = outThread.outgoingHandler.obtainMessage(ADD_REMINDER, "*deletereminder;" + iId + "#");
    				msg.sendToTarget();
    				mArrayAdapter.remove(item);
    				mArrayAdapter.notifyDataSetChanged();
					
				}
			}
        	
        });
        ll.addView(listView, 
        		new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        incomingHandler = new Handler(){
        	@Override
        	public void handleMessage(Message msg){
        		switch(msg.what){
        		
        			case ADD_REMINDER:
        				mArrayAdapter.add((String) msg.obj);
        				mArrayAdapter.notifyDataSetChanged();
        				break;
        			case DELETE_REMINDER:
        				for(int i = 0; i < mArrayAdapter.getCount(); i ++){
        					String s = mArrayAdapter.getItem(i);
        					if(s.split(" ")[0].split(":")[1].equals((String) msg.obj)){
        						mArrayAdapter.remove(s);
        					}
        				}
        				mArrayAdapter.notifyDataSetChanged();
        				break;
        			case ERROR:
        				break;
        		}
        		super.handleMessage(msg);
        	}
        };
        
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
        		outThread = new OutThread(bs);
        		outThread.start();
        		inThread = new InThread(bs);
        		inThread.start();
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
    
    private class OutThread extends Thread{
    	private final OutputStream mmOutStream;
    	private final BluetoothSocket socket;
        public Handler outgoingHandler;
        
        public OutThread(BluetoothSocket bs){
        	socket = bs;
        	OutputStream tmpOut = null;
        	try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
        	
        	mmOutStream = tmpOut;
        }
        
        public void run(){
        	write(timeMessage());
        	for(Long r : reminders.keySet()){
        		Log.e("dehug", "hello");
        		Reminder rem = reminders.get(r);
        		write("*newreminder;" + rem.id + ";" + rem.colour + ";" + 
        		rem.startTimeLeft + ";" + rem.endTimeLeft + "#");
        	}
        	Looper.prepare();
        	outgoingHandler = new Handler(){
            	@Override
            	public void handleMessage(Message msg){
            		Log.e("DEBUG", (String) msg.obj);
            		write((String) msg.obj);
            		super.handleMessage(msg);
            	}
            };
        	Looper.loop();
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
    
    
    private class InThread extends Thread{
    	private final InputStream mmInStream;
        private final BluetoothSocket socket;
    	
    	public InThread(BluetoothSocket bs){
    		socket = bs;
    		InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            
            
    	}
    	
    	public void run(){ 
    		byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes;
            
            
            while(true){
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
    		Log.e("DEBUG", parts[1]);
    		startRecording(Integer.parseInt(parts[1]));
    	}
    	else if(parts[0].equals("recordend")){
    		stopRecording();
    	}
    	else if(parts[0].equals("delete")){
    		deleteRecording(Integer.parseInt(parts[1]));
    	}
    	else if(parts[0].equals("addreminder")){
    		reminders.put(Long.parseLong(parts[1]), new Reminder(Long.parseLong(parts[1]),
    				Long.parseLong(parts[2]),
    				Long.parseLong(parts[3]),
    				Long.parseLong(parts[4])));
    		Message msg = incomingHandler.obtainMessage(
    				ADD_REMINDER, "ID:" + parts[1] + " Colour:" + parts[2]);
    		msg.sendToTarget();
    	}
    	else if(parts[0].equals("deletereminder")){
    		reminders.remove(Long.parseLong(parts[1]));
    		Message msg = incomingHandler.obtainMessage(
    				DELETE_REMINDER, parts[1]);
    		msg.sendToTarget();
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
    
    private long rgbToLong(int r, int g, int b){
    	return ((long)r << 16) | ((long)g <<  8) | b;
    }
    
    private void fileToReminders(){
    	File sdcard = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + 
    			"/recordings");

    	//Get the text file
    	File file = new File(sdcard,"reminders.txt");
    	try{
	    	FileWriter writer = new FileWriter(file, true);
	    	writer.append("");
	        writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	//Read text from file
    	StringBuilder text = new StringBuilder();

    	try {
    	    BufferedReader br = new BufferedReader(new FileReader(file));
    	    String line;

    	    while ((line = br.readLine()) != null) {
    	        text.append(line);
    	    }
    	}
    	catch (IOException e) {
    	    //You'll need to add proper error handling here
    	}
    	
    	if(text.length() != 0){
    		try{
		    	String rs[] = text.toString().split("\n");
		    	for(String s: rs){
		    		String parts[] = s.split(" ");
		    		long id = Long.parseLong(parts[0].split(":")[1]);
		    		long colour = rgbToLong(Integer.parseInt(parts[1].split(":")[1]),
		    				Integer.parseInt(parts[2].split(":")[1]),
		    				Integer.parseInt(parts[3].split(":")[1]));
		    		long start = Long.parseLong(parts[4].split(":")[1]);
		    		long end = Long.parseLong(parts[5].split(":")[1]);
		    		reminders.put(id, new Reminder(id, colour, start, end));
		    	}
    		} catch(Exception e){
    			
    		}
    	}
    	
    	Log.e("reminders", text.toString());
    }

}

class Reminder{
	public long id;
	public long colour;
	public long startTimeLeft;
	public long endTimeLeft;
	public Reminder(long id, long colour, long startTimeLeft, long endTimeLeft){
		this.id = id;
		this.colour = colour;
		this.startTimeLeft =  startTimeLeft;
		this.endTimeLeft = endTimeLeft;
	}
}
