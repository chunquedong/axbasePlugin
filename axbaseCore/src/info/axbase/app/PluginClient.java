/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;

import info.axbase.plugin.PluginClassLoader;
import info.axbase.util.SLogger;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public abstract class PluginClient {
	protected Context context;
	private MessageReceiver receiver;
	private PluginListener listener;
	Context tempContext;
	
	static Config config = new Config();
	
	public static final SLogger log = SLogger.get("axbase.client");
	
	public void setListener(PluginListener ls) {
		listener = ls;
	}
	
	public static void setConfig(Config c) {
		config = c;
		if (config.isDebug) {
			SLogger.setDefaultLevel(SLogger.levelDebug);
		}
		
		if (config.hostUrl != null) {
			DownloadTask.setHost(config.hostUrl);
		}
		UpdateService.setCheckTime(config.checkUpdateTime);
		
		PluginClassLoader.setClassLoaderType(c.classLoaderType);
	}
	
	private static PluginClient instance;
	public static void init(Context context) {
		init(context, "axplugin");
	}
	public static void init(Context context, String engine) {
		if (instance != null) {
			log.w("already inited");
			return;
		}
		
		log.d("PluginClient init");
		
		if ("axplugin".equals(engine)) {
			instance = new AxPluginClient();
		} else {
			log.i("unknow engine:" + engine);
			instance = new AxPluginClient();
		}
		
		instance.context = context.getApplicationContext();
		instance.listener = new PluginListener(instance.context);
		instance.onInit(context);
		instance.start(context);
	}
	
	public static PluginClient getInstance() {
		return instance;
	}
	
	protected abstract void onInit(Context context);

	protected void start(Context context) {
		Intent i = new Intent(context, UpdateService.class);
		context.startService(i);
		registerBroadcastReceiver();
	}

	protected void stop() {
		Intent i = new Intent(context, UpdateService.class);
		context.stopService(i);
		context.unregisterReceiver(receiver);
	}

	//public abstract List<PlugInfo> installedList();

	protected abstract void install(String file, String id);
	
	protected abstract void uninstall(String id);
	
	protected abstract boolean isInstalled(String id);
	
	public boolean isLoaded(String id) { return isInstalled(id); }

	public void load(String id) {
		log.d("load: " + id);
		
		if (!isInstalled(id)) {
			downloadPlugin(id
					, UpdateService.installType_install);
		} else {
			if (listener != null) {
				listener.onLoaded(id);
			}
		}
	}

	/**
	 * load and run main Activity
	 * @param id
	 * @param context
	 */
	public void launch(String id, Context ctx, boolean finishLaunchActivity) {
		log.d("launch: " + id);
		
		if (listener != null) {
			listener.finishLaunchActivity = finishLaunchActivity;
		}
		
		if (!isInstalled(id)) {
			tempContext = ctx;
			downloadPlugin(id
					, UpdateService.installType_launch);
		} else {
			if (listener != null) {
				listener.onLoaded(id);
			}
			this.startMainActivity(ctx, id);
			
			if (listener != null) {
				tempContext = ctx;
				listener.onLaunch(id);
				tempContext = null;
			}
		}
	}
	
	private void downloadPlugin(String appId, int installType) {
		Intent i = new Intent(context, UpdateService.class);
		Bundle b = new Bundle();
		b.putString("appId", appId);
		b.putInt("installType", installType);
		i.putExtras(b);
		context.startService(i);
	}

	private void onDownload(String appId, VersionInfo versionInfo
			, int installType, boolean cache) {
		File downloadStoragePath = context.getDir("download",
				Context.MODE_PRIVATE);
		File file = new File(downloadStoragePath, UpdateService.getFileName(versionInfo));
		
		if (isInstalled(appId)) {
			if (cache == false) {
				uninstall(appId);
				install(file.getAbsolutePath(), appId);
			}
		} else {
			install(file.getAbsolutePath(), appId);
		}
		
		if (listener != null) {
			if (installType != UpdateService.installType_update) {
				listener.onLoaded(appId);
			} else {
				if (cache == false) {
					listener.onUpdate(appId, versionInfo.whatsNew);
				}
			}
		}
		
		if (installType == UpdateService.installType_launch) {
			this.startMainActivity(tempContext != null ? tempContext : context, appId);
			if (listener != null) {
				listener.onLaunch(appId);
			}
			tempContext = null;
		}
	}

	private void registerBroadcastReceiver() {
		receiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter(UpdateService.CompleteAction);
		context.registerReceiver(receiver, filter);
		IntentFilter filter2 = new IntentFilter(UpdateService.ProgressAction);
		context.registerReceiver(receiver, filter2);
	}

	private class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (UpdateService.CompleteAction.equals(action)) {
				try {
					Bundle b = intent.getExtras();
					String appId = b.getString("appId");
					VersionInfo versionInfo = (VersionInfo) b.getSerializable("versionInfo");
					boolean success = b.getBoolean("success");
					int installType = b.getInt("installType");
					boolean cache = b.getBoolean("cache");
					String error = b.getString("error");
					
					if (success) {
						onDownload(appId, versionInfo, installType, cache);
					} else {
						if (listener != null) {
							listener.onError(appId, error);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (UpdateService.ProgressAction.equals(action)) {
				try {
					Bundle b = intent.getExtras();
					String appId = b.getString("appId");
					float percent = b.getFloat("percent");
					int installType = b.getInt("installType");
					int max = b.getInt("max");
					if (listener != null) {
						listener.onProgress(appId, percent, max, installType);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public abstract boolean startMainActivity(Context context, String id);

	public abstract void startActivity(Context context, Intent intent);

	public abstract void startActivityForResult(Activity activity,
			Intent intent, int requestCode);
}