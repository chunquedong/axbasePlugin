/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;

import info.axbase.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class UpdateService extends Service {

	public static final String CompleteAction = "slan.UpdateService.complete";
	public static final String ProgressAction = "slan.UpdateService.progress";

	private Timer timer;
	private static long period = 60 * 60 * 1000;
	private static final long minPeriod = 10 * 60 * 1000;
	
	File downloadStoragePath;
	
	private Map<String, OfflineVersion> appMap = new HashMap<String, OfflineVersion>();
	private LinkedList<String> appList = new LinkedList<String>();
	private Object lock = new Object();
	
	private DownloadTask downloadTask;
	
	public static final int installType_update = 0;
	public static final int installType_install = 1;
	public static final int installType_launch = 2;
	
	String packageName;
	TelephonyManager telephonyManager;
	
	static class OfflineVersion {
		File file;
		String version;
		String appId;
	}
	
	static void setCheckTime(long time) {
		if (time < minPeriod) {
			time = minPeriod;
		}
		period = time;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		PluginClient.log.d("onCreate");
		timer = new Timer();

		Context context = this;
		downloadStoragePath = context.getDir("download", Context.MODE_PRIVATE);
		downloadStoragePath.mkdirs();
		
		load();
		
		//TODO: remove
		if (PluginClient.config.isDebug) {
			period = 30 * 1000;
		}
		
		downloadTask = new DownloadTask(this, installType_update);
		timer.schedule(new UpdateTask(), 0, period);
		packageName = this.getPackageName();
		telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PluginClient.log.d("onDestroy");
		timer.cancel();
	}
	
	static String[] parceApkFileName(String name) {
		if (name.endsWith(".jar")) {
			name = name.replace(".jar", "");
		}
		else if (name.endsWith(".apk")) {
			name = name.replace(".apk", "");
		}
		else {
			return null;
		}

		String[] bn = name.split("@");
		if (bn.length != 2) {
			return null;
		}
		return bn;
	}
	
	private void load() {
		File[] files = downloadStoragePath.listFiles();
		for (File file : files) {
			try {
				String name = file.getName();
				String[] bn = parceApkFileName(name);
				if (bn == null) {
					continue;
				}
				
				String appId = bn[0];
				String version = (bn[1]);
				registApp(appId, version, file);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void registApp(String appId, String version, File file) {
		
		OfflineVersion ov = new OfflineVersion();
		ov.appId = appId;
		ov.version = version;
		ov.file = file;
		
		synchronized (lock) {
			if (!appMap.containsKey(appId)) {
				appList.addFirst(appId);
			} else {
				OfflineVersion old = appMap.get(appId);
				if (old != null && old.file != null) {
					if (!old.file.equals(file)) {
						PluginClient.log.d("delete old file:" + file);
						old.file.delete();
					}
				}
			}
			appMap.put(appId, ov);
		}
	}
	
	OfflineVersion getCache(String appId) {
		synchronized (lock) {
			return appMap.get(appId);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		PluginClient.log.d("onStartCommand: " + intent);

		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				String appId = b.getString("appId");
				if (appId != null) {
					int installType = b.getInt("installType");
					try {
						downloadImmediately(appId, installType);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private boolean copyAsset(String id, String dstDir) {
		try {
			String[] list = this.getAssets().list("");
			for (String f : list) {
				if (f.startsWith(id)) {
					String[] bn = parceApkFileName(f);
					if (bn == null) {
						continue;
					}
					String name = f.replace(".apk", ".jar");
					String dst = dstDir + "/" + name;
					FileUtil.copyAsset(this, f, dst);
					
					PluginClient.log.d("copy assets " + f);
					
					String appId = bn[0];
					String version = (bn[1]);
					registApp(appId, version, new File(dst));
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void downloadImmediately(final String appId, final int installType) {
		synchronized (lock) {
			if (PluginClient.config.copyAsset) {
				if (!appMap.containsKey(appId)) {
					copyAsset(appId, downloadStoragePath.getAbsolutePath());
				}
			}
			
			if (appMap.containsKey(appId)) {
				OfflineVersion ov = appMap.get(appId);
				if (ov != null) {
					VersionInfo version = new VersionInfo();
					version.appProject = ov.appId;
					version.version = ov.version;
					
					PluginClient.log.d(appId + " hit cache");
					onComplete(appId, version, true, null, installType, true);
					
					if (PluginClient.config.updateWhenLaunch 
							&& installType == installType_launch) {
					} else {
						return;
					}
				}
			} else {
				//put an empty for avoid repeat request
				appMap.put(appId, null);
			}
		}
		
		new Thread() {
			public void run() {
				try {
					new DownloadTask(UpdateService.this, installType).request(appId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private static boolean isWifi(Context mContext) {  
	    ConnectivityManager connectivityManager = (ConnectivityManager) mContext  
	            .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
	    if (activeNetInfo != null  
	            && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
	        return true;  
	    }  
	    return false;  
	}

	class UpdateTask extends TimerTask {
		@Override
		public void run() {
			try {
				if (PluginClient.config.updateOnlyWifi
						&& !isWifi(UpdateService.this)) {
					PluginClient.log.d("not wifi for download");
					return;
				}
				
				String appId = null;
				synchronized (lock) {
					if (appList.size() > 0) {
						appId = appList.removeFirst();
						appList.addLast(appId);
					}
				}
				
				if (appId != null) {
					downloadTask.request(appId);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getFileName(VersionInfo versionInfo) {
		return versionInfo.appProject +"@"+ versionInfo.version+".jar";
	}
	
	void onComplete(String appId, VersionInfo versionInfo
			, boolean success, String error, int installType, boolean cache) {
		if (!success) {
			PluginClient.log.w(versionInfo.appProject +"," + error);
		}
		
		Intent i = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("appId", appId);
		bundle.putSerializable("versionInfo", versionInfo);
		bundle.putBoolean("success", success);
		bundle.putString("error", error);
		bundle.putInt("installType", installType);
		bundle.putBoolean("cache", cache);
		i.putExtras(bundle);
		i.setAction(CompleteAction);
		sendBroadcast(i);
	}
	
	void onProgress(String appId, float percent, int max, int installType) {
		Intent i = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("appId", appId);
		bundle.putFloat("percent", percent);
		bundle.putInt("max", max);
		bundle.putInt("installType", installType);
		
		i.putExtras(bundle);
		i.setAction(ProgressAction);
		sendBroadcast(i);
	}
}
