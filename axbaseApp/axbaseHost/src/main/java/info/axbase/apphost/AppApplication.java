/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.apphost;

import info.axbase.app.Config;
import info.axbase.app.PluginClient;
import info.axbase.util.Logcat;
import info.axbase.util.SLogger;
import android.app.Application;
import android.util.Log;

public class AppApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("host", "Application onCreate");

		if (BuildConfig.DEBUG) {
			Logcat.getInstance(this).start();
			SLogger.setDefaultLevel(SLogger.levelDebug);
			Config config = new Config();
			// config.hostUrl = "http://192.168.1.100:8080";
			// config.copyAsset = false;
			config.isDebug = true;
			PluginClient.setConfig(config);
		}

		PluginClient.init(this);
	}
}
