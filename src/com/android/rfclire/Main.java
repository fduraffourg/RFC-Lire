package com.android.rfclire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Main extends Activity {
	private Button button_search;
	private Context mContext;
	private Integer lastSeenRfcNum;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);
        
        // Plug action on search button
        button_search = (Button) findViewById(R.id.button_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startRfcView(getRfcNum());
            }
        });
        
        //plug action on quit button
        Button button_quit = (Button) findViewById(R.id.button_quit);
        button_quit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        
        // Create text and plug action to last seen button
        Button button_last = (Button) findViewById(R.id.button_last_seen);
        SharedPreferences settings = getPreferences(0);
        lastSeenRfcNum = settings.getInt("lastSeen", 0);
        if (lastSeenRfcNum == 0 ) {
        	button_last.setVisibility(0);
        } else {
        	button_last.setText(getResources().getText(R.string.last_seen) + ": " + lastSeenRfcNum.toString());

        	button_last.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) {
        			startRfcView(lastSeenRfcNum);
        		}
        	});
        }
    }
    
    private Integer getRfcNum() {
    	Integer rfcNum;
    	
        // Get the RFC number
    	EditText eRfcNum = (EditText) findViewById(R.id.edit_rfc_num);
    	try {
    	    rfcNum = Integer.parseInt(eRfcNum.getText().toString());
    	} catch(NumberFormatException nfe) {
    	   System.out.println("Could not parse " + nfe);
    	   return null;
    	} 
    	return rfcNum;
    }
    
    private void startRfcView(Integer rfcNum) {
    	SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("lastSeen", rfcNum);
        editor.commit();

    	// Start the RFCView Activity
    	Intent intent = new Intent(mContext, RFCView.class);
    	intent.putExtra(RFCView.EXTRA_RFC_NUM, rfcNum);
    	startActivity(intent);
    }
    
}