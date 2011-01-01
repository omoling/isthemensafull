 /* 
 **  Copyright (C) 2011 Omar Moling
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

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class MensaService extends Service {
	private static final String TAG = "MensaService";
	
	private URL url = null;
	private MensaApp app;
	
	private GetImageTask lastGetImageTask = null;
	
	@Override
	public void onCreate() {
		app = (MensaApp) getApplication();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		// TODO: automatic refresh
		refreshImage();
		
	}
	
	private void refreshImage() {
		Log.d(TAG, "refreshImage() called");
		if (lastGetImageTask == null || lastGetImageTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
			lastGetImageTask = new GetImageTask();
			lastGetImageTask.execute((Void[]) null);
		}
	}
	
	private class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		app.setProgressBarIndeterminateVisibility(true);
    	}
    	
		@Override
		protected Bitmap doInBackground(Void... params) {
			HttpURLConnection httpConnection = null;
			
			try {	
				//sample image
	        	url = new URL("http://isthemensafull.googlecode.com/files/StreamImage.jpeg");
				
	        	//real url
	        	//url = new URL("http://aws.unibz.it/mensawebcam/StreamImage.aspx");
		        
	        	httpConnection = (HttpURLConnection) url.openConnection();
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
				//imageView.setImageBitmap(result);
				app.displayImage(result);
			}
			
			app.setProgressBarIndeterminateVisibility(false);
			
			stopSelf();
		}
    	
    }

}
