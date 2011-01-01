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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class Mensa extends Activity {
    private static final String TAG = "Mensa";
    
    private MensaApp app;
	
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
        
        app = (MensaApp) getApplication();
        app.registerActivity(this);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        
        imageView = (ImageView) this.findViewById(R.id.imageview);
        
        refreshImage();
    }
    
    private void refreshImage() {
    	Log.d(TAG, "refreshImage() called");
    	// start service to refresh image
    	startService(new Intent(this, MensaService.class));
    }
    
    protected void displayImage(Bitmap image) {
    	imageView.setImageBitmap(image);

		// determine if image is 1x1: means webcam is not available
		int width = image.getWidth(), height = image.getHeight();
		Log.d(TAG, "image W x H: " + width + " x " + height);
		if (width == 1 && height == 1) {
			Log.i(TAG, "Image is 1x1, webcam not available.");
			Toast.makeText(this, "The webcam appears to be not available. Please check the instructions.", Toast.LENGTH_LONG).show();
		}
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
    		// TODO
    		break;
    	}
    	}
    }
    
}
