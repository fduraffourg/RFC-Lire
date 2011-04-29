package com.android.rfclire;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RFCView extends Activity {
    public static final String EXTRA_RFC_NUM =
        "com.android.rfclire.RFCView.EXTRA_RFC_NUM";

	private Context mContext;
	private TextView rfcTv;
	private ScrollView scrollView;
	
	private String rfcText;
	private int rfcNum;
	
	private static final int HTTP_STATUS_OK = 200;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        
        rfcNum = getIntent().getIntExtra(EXTRA_RFC_NUM, 0);
        
        setContentView(R.layout.rfcview);
        rfcTv = (TextView) findViewById(R.id.rfc_content);
        
        scrollView = (ScrollView) findViewById(R.id.rfcview_scrollview);
        
        
        if (getRfcContent()) {
        	setRfcContent();
        }
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
    	// Scroll the view to the last position
    	scrollView.post(new Runnable() {
    		//@Override
    		public void run() {
    			Integer scroll;
    			scroll = loadScrollPosition();
    			scrollView.scrollTo(0,scroll);
    		}
    	});
    }

    @Override
    public void onStop() {
    	super.onStop();
    	saveScrollPosition();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rfcview_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.rfcview_menu_save:
        	if (saveRfcContent()) {
        	Toast.makeText(getApplicationContext(), "RFC saved",
    				Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(getApplicationContext(), "Error while saving the RFC",
        				Toast.LENGTH_SHORT).show();
        	}
            return true;
        case R.id.rfcview_menu_quit:
        	finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void setRfcContent() {
    	rfcTv.setText(rfcText);
    }
    
    private boolean getRfcContent() {
    	if (openRfcContent()) {
    		Toast.makeText(getApplicationContext(), "Read from file",
    				Toast.LENGTH_SHORT).show();
    		return true;
    	}
    	
    	try {
    		downloadRfcContent();
    	} catch(Exception e) {
    		return false;
    	}
    	return true;
    }
    
    public boolean saveRfcContent() {
    	String state = Environment.getExternalStorageState();
    	if ( ! Environment.MEDIA_MOUNTED.equals(state)) {
    	    Toast.makeText(getApplicationContext(), "External media not available",
    				Toast.LENGTH_SHORT).show();
    	    return false;
    	}
    	String filename = String.format("rfc%d.txt", rfcNum);
    	File file = new File(mContext.getExternalFilesDir(null), filename);
    	try {
    		OutputStream os = new FileOutputStream(file);
    		os.write(rfcText.getBytes());
    		os.close();
    	} catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Error when writting: " + e.toString(),
    				Toast.LENGTH_SHORT).show();
    	    return false;
    	}
    	return true;
    }
    
    public boolean openRfcContent() {
    	byte[] sBuffer = new byte[512];
    	String state = Environment.getExternalStorageState();
    	if ( ! ( Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state))) {
    	    Toast.makeText(getApplicationContext(), "External media not available",
    				Toast.LENGTH_SHORT).show();
    	    return false;
    	}
    	String filename = String.format("rfc%d.txt", rfcNum);
    	File file = new File(mContext.getExternalFilesDir(null), filename);
    	try {
    		InputStream is = new FileInputStream(file);
    		// Read response into a buffered stream
    		ByteArrayOutputStream content = new ByteArrayOutputStream();
    		int readBytes = 0;
    		while ((readBytes = is.read(sBuffer)) != -1) {
    			content.write(sBuffer, 0, readBytes);
    		}
    		
    		// Return result from buffered stream
    		rfcText = new String(content.toByteArray());
    		
    		is.close();
    	} catch (Exception e) {
    		Toast.makeText(getApplicationContext(), "Error when reading: " + e.toString(),
    				Toast.LENGTH_SHORT).show();
    	    return false;
    	}
    	return true;
    }
    
    public void downloadRfcContent() throws Exception {
    	String url;
    	byte[] sBuffer = new byte[512];

    	url = String.format("http://www.ietf.org/rfc/rfc%d.txt", rfcNum);

    	// Create client and set our specific user-agent string
    	HttpClient client = new DefaultHttpClient();
    	HttpGet request = new HttpGet(url);
    	//request.setHeader("User-Agent", sUserAgent);

    	try {
    		HttpResponse response = client.execute(request);

    		// Check if server response is valid
    		StatusLine status = response.getStatusLine();
    		if (status.getStatusCode() != HTTP_STATUS_OK) {
    			throw new Exception("Invalid response from server: " +
    					status.toString());
    		}

    		// Pull content stream from response
    		HttpEntity entity = response.getEntity();
    		InputStream inputStream = entity.getContent();

    		ByteArrayOutputStream content = new ByteArrayOutputStream();

    		// Read response into a buffered stream
    		int readBytes = 0;
    		while ((readBytes = inputStream.read(sBuffer)) != -1) {
    			content.write(sBuffer, 0, readBytes);
    		}

    		// Return result from buffered stream
    		rfcText = new String(content.toByteArray());
    	} catch (IOException e) {
    		throw new Exception("Problem communicating with API", e);
    	}
    }
    private int byteArrayToInt(byte[] b)
    {
        return (b[3] & 0xFF) + ((b[2] & 0xFF) << 8) + ((b[1] & 0xFF) << 16) + ((b[0] & 0xFF) << 24);
    }

    private void saveScrollPosition() {
    	// Get the scroll position
    	Integer scrollX, scrollY;
    	//scrollX = scrollView.getScrollX();
    	scrollY = scrollView.getScrollY();
    	
    	// Save the scroll position
    	String filename = String.format("rfc%d.txt", rfcNum);
    	File file = new File(mContext.getCacheDir(), filename);
    	try {
    		OutputStream os = new FileOutputStream(file);
    		//os.write(ByteBuffer.allocate(Integer.SIZE/8).putInt(scrollX).array());
    		os.write(ByteBuffer.allocate(Integer.SIZE/8).putInt(scrollY).array());
    		os.flush();
    		os.close();
    	} catch (Exception e) {  	}    	
    }
    
    private Integer loadScrollPosition() {
    	// Get the scroll position
    	Integer scrollY = 0;
    	byte[] buffer = new byte[4];
    	
    	// Save the scroll position
    	String filename = String.format("rfc%d.txt", rfcNum);
    	File file = new File(mContext.getCacheDir(), filename);
    	try {
    		InputStream is = new FileInputStream(file);
    		is.read(buffer, 0, 4);
    		scrollY = byteArrayToInt(buffer);
    		is.close();
    	} catch (Exception e) {  	}
    	return scrollY;
    }
}
