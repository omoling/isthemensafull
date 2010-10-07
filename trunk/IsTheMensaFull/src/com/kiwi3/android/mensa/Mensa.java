package com.kiwi3.android.mensa;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Mensa extends Activity {
    private static final String TAG = "Mensa";
	
    private URL url = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Status: onCreate");
        
        try {
        	//sample image
        	url = new URL("http://isthemensafull.googlecode.com/files/StreamImage.jpeg");
        	
        	//real url
        	//url = new URL("http://aws.unibz.it/mensawebcam/StreamImage.aspx");
        	
        } catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		}
        
        getImage();
    }
    
    private void getImage() {
    	Log.d(TAG, "getImage called");
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.connect();
			
			int responseCode = httpConnection.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				Log.w(TAG, "http response code OK");
				InputStream in = (InputStream) httpConnection.getInputStream();
				
				ImageView image = (ImageView) this.findViewById(R.id.imageview);
				image.setImageBitmap(BitmapFactory.decodeStream(in));
				Log.w(TAG, "image set");
			} else {
				Log.w(TAG, "http response code: " + responseCode);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
    	 
    }
}