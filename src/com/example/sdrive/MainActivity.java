package com.example.sdrive;

import java.util.ArrayList;

import com.example.sdrive.SmsListener.MsgToSpeech;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.gesture.Sgesture;
import com.samsung.android.sdk.gesture.SgestureHand;

import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public SmsListener _receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	public void startDrive(View view) {
		Log.v("test", "Drive started");
		Button driveButton = (Button) findViewById(R.id.drive_button);
		driveButton.setText("End Drive!");
		_receiver = new SmsListener();
		IntentFilter msgIntent = new IntentFilter(SMS_RECEIVED);
		this.registerReceiver(_receiver, msgIntent);
        tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) new MsgToSpeech(""));
		
		_gesture = new Sgesture();
		try {
			_gesture.initialize(this);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SsdkUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(_gesture.isFeatureEnabled(Sgesture.TYPE_HAND_PRIMITIVE)){
            _gestureHand = new SgestureHand(Looper.getMainLooper(), _gesture);
        }
        _gestureHand.start(Sgesture.TYPE_HAND_PRIMITIVE, changeHandListener);

	}
	
    private SgestureHand.ChangeListener changeHandListener = new SgestureHand.ChangeListener() {
        
        @Override
        public void onChanged(SgestureHand.Info info) {
            // TODO Auto-generated method stub
            handleGesture(info);
        }
        
    };
    
    
    public void handleGesture(SgestureHand.Info info) {
    	int angle = 0;
        if(info.getType() == Sgesture.TYPE_HAND_PRIMITIVE){
            angle = info.getAngle();
        }
    	Log.v("test", "in handleGesture");
    	Log.v("test", "Angle: " + Integer.toString(angle));
    	String number = null;
        ContentResolver cr = getContentResolver();
        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cur = cr.query(smsUri, null, "read = 0", null, null); 	
    	
        if (225 <= angle && angle <= 315) {
            if (cur != null && cur.getCount() > 0) {
            	cur.moveToFirst();
            	String smsBody = cur.getString(cur.getColumnIndexOrThrow("body"));
	        	number = cur.getString(cur.getColumnIndexOrThrow("address"));   
	        	Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	            Cursor c = cr.query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},null,null,null);
                try {
                	if (c != null && c.getCount() > 0) {
                		c.moveToNext();
                         String sender = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                     	Log.v("test", "Body: "+ smsBody);
                    	Log.v("test", "Sender: "+sender);
                        tts.speak("Text from " + sender + " . Message is " + smsBody, TextToSpeech.QUEUE_FLUSH, null);
                	}
                	
                }finally{
                	if (c != null) {
                		c.close();
                	}
                }

            } else {
            	tts.speak("No new texts", TextToSpeech.QUEUE_FLUSH, null);
            }
        }
        if (45 <= angle && angle <= 135) {
            if (cur != null && cur.getCount() > 0) {
            	cur.moveToFirst();
	        	number = cur.getString(cur.getColumnIndexOrThrow("address"));   
	
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				if (number != null) {
					callIntent.setData(Uri.parse("tel:" + number));
					startActivity(callIntent);
				}
            }
        }
        if (135 <= angle && angle <= 225) {
            if (cur != null && cur.getCount() > 0) {
            	cur.moveToFirst();
	        	number = cur.getString(cur.getColumnIndexOrThrow("address")); 
	            tts.speak("Say your reply.", TextToSpeech.QUEUE_FLUSH, null);
	            Log.v("test", "number is " + number);
	            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	        	recordMessage(number);
	        	
            }
        }
        
        

    }
    
    public final static String EXTRA_MESSAGE = "com.example.sdrive.MESSAGE";
    public final static String EXTRA_NUMBER = "com.example.sdrive.NUMBER";

    
    
    /** Called when user clicks on taking speech as input */
    public void recordMessage(String number) {
    	Log.v("recordMessage", "number is " + number);
        
        intent.putExtra(EXTRA_NUMBER, number);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//        if (!intent.getStringExtra(EXTRA_NUMBER).equals(number))
//        	System.exit(1);
        num = number;
        startActivityForResult(intent, 1);
    }
    
    public void recordMessage2(String result, String number) {

//    	intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(EXTRA_MESSAGE, result);
//        intent.putExtra(EXTRA_NUMBER, number);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        startActivityForResult(intent, 2);
    }
    public Intent intent;
    public String num;
    public String result;
    public String msg;
    
    /** Method for copying the speech string to a buffer */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.v("test", Boolean.toString(data.getStringExtra(EXTRA_NUMBER) != null));

            if (requestCode == 1 && resultCode == RESULT_OK &&  data != null) {
            	result = null;
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                result = text.get(0);
                Log.v("CASE1", result);
            	//String number = data.getExtras().getString(EXTRA_NUMBER);
                
            	Log.v("CASE1", "number is " + num);

            	while(tts.isSpeaking()) {
                	try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                tts.speak("Did you say, " + result + "?", TextToSpeech.QUEUE_FLUSH, null);
                while(tts.isSpeaking()) {
                	try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                recordMessage2(result, num);

            }
            if (requestCode == 2 && resultCode == RESULT_OK &&  data != null) {
                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String confirmation = text.get(0);
                Log.v("CASE2", "confirmation in case 2 is " + confirmation);
                if (confirmation.toLowerCase().equals("yes")) {
                	Log.v("CASE2", "message in case 2 is " + result);
                    if (result != null) {
                    	String body = result;
                    
                    	String number = num;
                		SmsManager sms = SmsManager.getDefault();
                		sms.sendTextMessage(number, null, body, null, null);
                		while(tts.isSpeaking()) {
                        	try {
        						Thread.sleep(500);
        					} catch (InterruptedException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
                        }
                		tts.speak("Message sent", TextToSpeech.QUEUE_FLUSH, null);
                		while(tts.isSpeaking()) {
                        	try {
        						Thread.sleep(500);
        					} catch (InterruptedException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
                        }
                    	// send message
                    }
                } else {
                	while(tts.isSpeaking()) {
                    	try {
    						Thread.sleep(500);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
                    }
                    tts.speak("Please try again.", TextToSpeech.QUEUE_FLUSH, null);
                    while(tts.isSpeaking()) {
                    	try {
    						Thread.sleep(500);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
                    }
                    recordMessage(num);
                }
                    

            	
            }
        }
    
       
    
	public void test(String msg) {
		tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
	}
    
	private TextToSpeech tts;
	private SgestureHand _gestureHand;
	private static Sgesture _gesture;
    private static final int MODE_HAND_PRIMITIVE = 0;    

	class MsgToSpeech implements TextToSpeech.OnInitListener {
		public String msg;
		public MsgToSpeech(String msg) {
			this.msg = msg;
		}
		
		@Override
		public void onInit(int status) {
			if (status == 0) 
				test(msg);
		}
	}
}
