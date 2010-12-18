 /* 
 **  Copyright (C) 2010 Omar Moling
 **	
 **  This program is free software; you can redistribute it and/or modify 
 **  it under the terms of the GNU General Public License as published by 
 **  the Free Software Foundation; either version 3 of the License, or
 **	 (at your option) any later version.
 **	
 **  This program is distributed in the hope that it will be useful, 
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 **	 GNU General Public License for more details.
 **	
 **  You should have received a copy of the GNU General Public License
 **  along with this program. If not, see <http://www.gnu.org/licenses/>.
 **	
 ** */

package com.kiwi3.android.mensa;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class Mensa extends Activity {
    private static final String TAG = "Mensa";
	
    private URL url = null;
    private ImageView imageView;
    
    //MENU
    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_ABOUT = Menu.FIRST + 1;
    private static final int MENU_PREFERENCES = Menu.FIRST + 2;
    
    //ACTIVITY RESULT CODES
    private static final int SHOW_PREFERENCES = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Status: onCreate");
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        
        imageView = (ImageView) this.findViewById(R.id.imageview);
        
        try {
        	//sample image
        	//url = new URL("http://isthemensafull.googlecode.com/files/StreamImage.jpeg");
        	
        	//real url
        	url = new URL("http://aws.unibz.it/mensawebcam/StreamImage.aspx");
        	
        } catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		}
        
        refreshImage();
    }
    
    private void refreshImage() {
    	Log.d(TAG, "refreshImage() called");
    	
    	// call task to refresh the image from the webcam
		new GetMensaImage().execute();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_REFRESH, Menu.NONE, R.string.refresh)
				.setIcon(R.drawable.ic_menu_refresh);

		menu.add(0, MENU_ABOUT, Menu.NONE, R.string.about)
				.setIcon(R.drawable.ic_menu_info_details);
		
		menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.preferences)
				.setIcon(R.drawable.ic_menu_preferences);

    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case (MENU_REFRESH): {
			refreshImage();
			return true;
		}
		case (MENU_ABOUT): {
			startActivity(new Intent(Intent.ACTION_VIEW, 
					Uri.parse(getString(R.string.about_url))));
			return true;
		}
		case (MENU_PREFERENCES): {
			startActivityForResult(new Intent(this, Preferences.class), SHOW_PREFERENCES);
			return true;
		}
		}
    	return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
    	case (SHOW_PREFERENCES): {
    		//
    		break;
    	}
    	}
    }
    
    private class GetMensaImage extends AsyncTask<Void, Void, Bitmap> {

    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		setProgressBarIndeterminateVisibility(true);
    	}
    	
		@Override
		protected Bitmap doInBackground(Void... params) {
			HttpURLConnection httpConnection = null;
			
			try {
				httpConnection = (HttpURLConnection) url
						.openConnection();
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				int responseCode = httpConnection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream in = (InputStream) httpConnection
							.getInputStream();

					Bitmap image = BitmapFactory.decodeStream(in);

					return image;
				} else {
					Log.w(TAG, "http response code: " + responseCode);
				}
			} catch (MalformedURLException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				if (httpConnection != null) {
					httpConnection.disconnect();
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			
			if (result != null) {
				imageView.setImageBitmap(result);
				
				// determine if image is 1x1: means webcam is not available
				int width = result.getWidth(), height = result.getHeight();
				Log.d(TAG, "image W x H: " + width + " x " + height);
				
				if (width == 1 && height == 1) {
					Log.i(TAG, "Image is 1x1, webcam not available.");
					Toast.makeText(getBaseContext(), "The webcam appears to be not available. Please check the instructions.", Toast.LENGTH_LONG).show();
				}
			}
			
			setProgressBarIndeterminateVisibility(false);
		}
    	
    }
    
}
