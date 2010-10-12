 /*  *********************************************************************  **
 **  Copyright (C) 2010 Omar Moling			 								**
 **											                                **
 **  This program is free software; you can redistribute it and/or modify   ** 
 **  it under the terms of the GNU General Public License as published by   ** 
 **  the Free Software Foundation; either version 3 of the License, or 		**
 **	 (at your option) any later version.                                    **
 **																			**
 **  This program is distributed in the hope that it will be useful,		** 
 **  but WITHOUT ANY WARRANTY; without even the implied warranty of 		**
 **  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 			**
 **	 GNU General Public License for more details.							**
 **																			**
 **  You should have received a copy of the GNU General Public License 		**
 **  along with this program. If not, see <http://www.gnu.org/licenses/>.	**
 **																			**
 **  *********************************************************************  */

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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class Mensa extends Activity {
    private static final String TAG = "Mensa";
	
    private URL url = null;
    
    //MENU
    private static final int MENU_REFRESH = Menu.FIRST;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Status: onCreate");
        
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
		try {
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.connect();
			
			int responseCode = httpConnection.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				Log.d(TAG, "http response code OK");
				InputStream in = (InputStream) httpConnection.getInputStream();
				
				ImageView image = (ImageView) this.findViewById(R.id.imageview);
				image.setImageBitmap(BitmapFactory.decodeStream(in));
				Log.w(TAG, "image set");
				
				httpConnection.disconnect();
			} else {
				Log.w(TAG, "http response code: " + responseCode);
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
    	 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(0, MENU_REFRESH, Menu.NONE, "Refresh");
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case (MENU_REFRESH):
			refreshImage();
			return true;
		}
    	return true;
    }
    
}