package com.example.sdrive;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {
	private TextToSpeech tts;
	public SmsListener() {
		
	}
	
	public void test(String msg) {
		tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("test", "Msg Received");
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String sender;
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
  
                String no = msgs[i].getOriginatingAddress();

                //Resolving the contact name from the contacts.
                Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(no));
                ContentResolver resolver = context.getContentResolver();
                Cursor c = resolver.query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},null,null,null);
                try {
                	if (c != null && c.getCount() > 0) {
                		c.moveToNext();
                        sender = c.getString(c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        Log.v("test", sender);
                        boolean wait = true;
                        tts = new TextToSpeech(context, (TextToSpeech.OnInitListener) new MsgToSpeech("Text from " + sender));
                        
//                        Intent checkIntent = new Intent();
//                        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
//                        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
                        
                       
                        tts.speak("Text Message from " + sender, TextToSpeech.QUEUE_FLUSH, null);
                	}
                	
                }finally{
                	if (c != null) {
                		c.close();
                	}
                }
			}
		}
	}
	
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
