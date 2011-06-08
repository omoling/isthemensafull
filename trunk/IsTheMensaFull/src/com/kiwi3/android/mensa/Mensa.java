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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class Mensa extends Activity {
    private static final String TAG = "Mensa";
    
    private MensaApp app;
    private SharedPreferences prefs;
    private GetImageTask lastGetImageTask = null;
	
    private ImageView imageView;
    
    private MensaAlarmReceiver receiver;
    private AlarmManager alarms;
    private PendingIntent alarmIntent;
    
    //MENU
    private static final int MENU_REFRESH_START 	= Menu.FIRST;
    private static final int MENU_REFRESH_STOP 		= Menu.FIRST + 1;
    private static final int MENU_REFRESH_ONCE		= Menu.FIRST + 2;
    private static final int MENU_ABOUT 			= Menu.FIRST + 3;
    private static final int MENU_PREFERENCES 		= Menu.FIRST + 4;
    
    //DIALOGS
    private static final int DIALOG_INFO = 1;
    
    //ACTIVITY RESULT CODES
    private static final int SHOW_PREFERENCES = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        
        app = (MensaApp) getApplication();
        
        alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        String ALARM_ACTION;
		ALARM_ACTION = MensaAlarmReceiver.ACTION_REFRESH_ALARM;
		
		Intent intentToFire = new Intent(ALARM_ACTION);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
		
		receiver = new MensaAlarmReceiver();
		
        prefs = PreferenceManager.getDefaultSharedPreferences(app);
        setPausingAutoRefresh(false);
        
        imageView = (ImageView) this.findViewById(R.id.imageview);
        
        refreshImage();
        
        checkInfoDialog();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	Log.d(TAG, "onConfigurationChanged()");
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume()");
    	registerReceiver(receiver, new IntentFilter(MensaAlarmReceiver.ACTION_REFRESH_ALARM));
    	//check past auto-refresh state
    	Log.d(TAG, "autorefresh was: " + prefs.getBoolean(getString(R.string.pref_pausing_auto_refresh_key), false));
    	if (wasAutoRefresh()) {
    		setAutoRefresh(true);
    		refreshImage();
    	}
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	Log.d(TAG, "onPause()");
    	//check GetImageTask
    	stopLastGetImageTask();
    	//unregister receiver
    	try {
    		unregisterReceiver(receiver);
    	} catch (Exception t) {}
    	//store current auto-refresh state to resume later
    	setPausingAutoRefresh(isAutoRefresh());
    	//set current auto-refresh to false
    	setAutoRefresh(false);
    	super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy()");
    	setAutoRefresh(false);
    	setPausingAutoRefresh(false);
    	super.onDestroy();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_REFRESH_STOP, Menu.NONE, R.string.refresh_stop)
				.setIcon(R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_REFRESH_START, Menu.NONE, R.string.refresh_start)
				.setIcon(R.drawable.ic_menu_play_clip);
		menu.add(0, MENU_REFRESH_ONCE, Menu.NONE, R.string.refresh_once)
				.setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, MENU_ABOUT, Menu.NONE, R.string.about)
				.setIcon(R.drawable.ic_menu_info_details);
		menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.preferences)
				.setIcon(R.drawable.ic_menu_preferences);

    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	MenuItem refreshStart = menu.findItem(MENU_REFRESH_START);
    	MenuItem refreshStop = menu.findItem(MENU_REFRESH_STOP);
    	MenuItem refreshOnce = menu.findItem(MENU_REFRESH_ONCE);
    	if (isAutoRefresh()) {
    		refreshStart.setVisible(false);
    		refreshStop.setVisible(true);
    		refreshOnce.setEnabled(false);
    	} else {
    		refreshStart.setVisible(true);
    		refreshStop.setVisible(false);
    		refreshOnce.setEnabled(true);
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case (MENU_REFRESH_START): {
			setAutoRefresh(true);
			refreshImage();
			return true;
		}
		case (MENU_REFRESH_STOP): {
			stopLastGetImageTask();
			setAutoRefresh(false);
			try {
				//unregisterReceiver(receiver);
				// note: after stopping, an additional refresh happens: register and unregister receiver to stop this.
			} catch (Exception t) {}
			return true;
		}
		case (MENU_REFRESH_ONCE): {
			stopLastGetImageTask();
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
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder dialogB = new AlertDialog.Builder(this);
    	
    	switch (id) {
    	case (DIALOG_INFO): {
    		dialogB.setTitle(R.string.info_dialog_title);
    		dialogB.setMessage(R.string.info_dialog_content);
    		dialogB.setPositiveButton(R.string.info_dialog_positive, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//positive: hide next time
					setHideInfoDialog(true);
				}
			});
    		dialogB.setNegativeButton(R.string.info_dialog_negative, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					setHideInfoDialog(false);
				}
			});
    		break;
    	}
    	}

    	return dialogB.create();
    }
    
    private void checkInfoDialog() {
    	boolean hideInfoDialog = prefs.getBoolean(getString(R.string.pref_hide_info_dialog), false);
    	if (!hideInfoDialog) {
    		showDialog(DIALOG_INFO);
    	}
    }
    
    private void refreshImage() {
    	Log.d(TAG, "refreshImage() called");
    	
    	if (isAutoRefresh()) {
    		//get refresh interval
        	int refreshFrequency = getRefreshFrequency();
        	int alartType = AlarmManager.ELAPSED_REALTIME;
			long timeToRefresh = SystemClock.elapsedRealtime() + refreshFrequency * 1000;
			alarms.setRepeating(alartType, timeToRefresh, refreshFrequency * 1000, alarmIntent);
    	} else {
    		alarms.cancel(alarmIntent);
    	}
    	
    	if (lastGetImageTask == null || lastGetImageTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
			lastGetImageTask = new GetImageTask();
			lastGetImageTask.execute((Void[]) null);
		} else {
			Log.d(TAG, "Refreshing image skipped since still executing a previous call.");
		}
    }
    
    protected void displayImage(Bitmap image) {
    	imageView.setImageBitmap(image);

		// determine if image is 1x1: means webcam is not available
		int width = image.getWidth(), height = image.getHeight();
		//Log.d(TAG, "image W x H: " + width + " x " + height);
		if (width == 1 || height == 1) {
			Log.i(TAG, "Webcam not available.");
			Toast.makeText(this, R.string.webcam_not_available_error_msg, Toast.LENGTH_SHORT).show();
		}
    }
    
    private void setHideInfoDialog(boolean hide) {
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(getString(R.string.pref_hide_info_dialog), hide);
    	editor.commit();
    	Log.d(TAG, "hide dialog set to: " + prefs.getBoolean(getString(R.string.pref_hide_info_dialog), false));
    }
    
    private boolean isAutoRefresh() {
    	return prefs.getBoolean(app.getString(R.string.pref_auto_refresh_key), false);
    }
    
    private void setAutoRefresh(boolean autoRefresh) {
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(getString(R.string.pref_auto_refresh_key), autoRefresh);
    	editor.commit();
    	Log.d(TAG, "autoRefresh: " + prefs.getBoolean(getString(R.string.pref_auto_refresh_key), false));
    }
    
    private boolean wasAutoRefresh() {
    	return prefs.getBoolean(getString(R.string.pref_pausing_auto_refresh_key), false);
    }
    
    private void setPausingAutoRefresh(boolean pausingAutoRefresh) {
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(getString(R.string.pref_pausing_auto_refresh_key), pausingAutoRefresh);
    	editor.commit();
    	Log.d(TAG, "pausingAutoRefresh: " + prefs.getBoolean(getString(R.string.pref_pausing_auto_refresh_key), false));
    }
    
    private int getRefreshFrequency() {
    	int refreshFrequency;
		try {
			refreshFrequency = Integer.parseInt(prefs.getString(app.getString(
					R.string.pref_refresh_freq_key), app.getString(
							R.string.pref_refresh_freq_default_value)));
			if (refreshFrequency < 0) {
				refreshFrequency = 5;
			}
		} catch (NumberFormatException e) {
			refreshFrequency = 5;
			Log.w(TAG, "Could not convert frequency to integer! Setting it to 5.");
		}
		return refreshFrequency;
    }
    
    private void stopLastGetImageTask() {
    	if (lastGetImageTask != null) {
    		Log.d(TAG, "Stopping lastGetImageTask");
    		lastGetImageTask.cancel(true);
    		lastGetImageTask = null;
    	}
    	setProgressBarIndeterminateVisibility(false);
    }
    
    private class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		setProgressBarIndeterminateVisibility(true);
    	}
    	
		@Override
		protected Bitmap doInBackground(Void... params) {
			HttpURLConnection httpConnection = null;
			
			try {	
				//sample image
	        	//url = new URL("http://isthemensafull.googlecode.com/files/StreamImage.jpeg");
				
	        	//real url
	        	URL url = new URL("http://aws.unibz.it/mensawebcam/StreamImage.aspx");
		        
	        	httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				int responseCode = httpConnection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream in = httpConnection
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
			}
			setProgressBarIndeterminateVisibility(false);
		}
    	
    }
    
    public class MensaAlarmReceiver extends BroadcastReceiver {
    	public static final String ACTION_REFRESH_ALARM = "com.kiwi3.android.mensa.ACTION_REFRESH_ALARM";

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.d(TAG, "MensaAlarmReceiver: onReceive()");
    		refreshImage();
    	}

    }
    
}
