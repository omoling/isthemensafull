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
import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

public class MensaApp extends Application {
	private static final String TAG = "MensaApp";

	private Mensa mensaActivity;
	
	protected void registerActivity(Activity activity) {
		//to be extended in case of multiple activities
		Log.d(TAG, "registering Mensa activity");
		mensaActivity = (Mensa) activity;
	}
	
	protected void displayImage(Bitmap image) {
		try {
			Log.d(TAG, "displayImage() called");
			mensaActivity.displayImage(image);
		} catch (NullPointerException e) {
			// TODO
			Log.e(TAG, e.getMessage());
		}
	}
	
	protected void setProgressBarIndeterminateVisibility(boolean visible) {
		try {
			//to be extended in case of multiple activities
			mensaActivity.setProgressBarIndeterminateVisibility(visible);
		} catch (NullPointerException e) {
			// TODO
			Log.e(TAG, e.getMessage());
		}
	}
	
}
