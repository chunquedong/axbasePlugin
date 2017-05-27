/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.appex;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PluginService extends Service {
	public static final String TAG = "PluginService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		PluginApplication.showMessage("onCreate");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		PluginApplication.showMessage("onDestroy");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int r = super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");
		PluginApplication.showMessage("onStartCommand");
		return r;
	}

}
