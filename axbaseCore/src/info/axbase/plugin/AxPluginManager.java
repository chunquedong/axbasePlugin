/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import info.axbase.util.FileUtil;
import info.axbase.util.SLogger;
import info.axbase.util.Reflection;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class AxPluginManager {
	public static final SLogger log = SLogger.get("axbase.plugin");
	
	final static String intentPackageName = "plugin_package";
	final static String intentClassName = "plugin_class";

	private Context context;
	private AxInstrumentation instrumentation;
	private String storagePath;
	private Map<String, PluginInfo> pluginInfoMap = new HashMap<String, PluginInfo>();

	private static AxPluginManager instance = new AxPluginManager();

	public static AxPluginManager getInstance() {
		return instance;
	}

	private AxAppClassLoader appClassLoader;

	Map<String, PluginInfo> getPluginInfoMap() {
		return pluginInfoMap;
	}
	
	public ClassLoader getClassLoader() {
		return appClassLoader;
	}

	public void init(Context context) {
		try {
			this.context = context.getApplicationContext();

			instrumentation = new AxInstrumentation();
			instrumentation.pluginManager = this;
			injectorInstrumentation();

			storagePath = context.getDir("plugins", Context.MODE_PRIVATE)
					.getAbsolutePath();
			FileUtil.ensureDir(storagePath);
			
			injectorClassLoader();

			loadAllFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void injectorInstrumentation() {
		Context contextImpl = ((ContextWrapper) context).getBaseContext();
		Object activityThread = Reflection.getField(contextImpl, "mMainThread");
		Object old = Reflection.getField(activityThread, "mInstrumentation");
		if (old instanceof AxInstrumentation) {
		} else {
			Reflection.setField(activityThread, "mInstrumentation",
					instrumentation);
			instrumentation.old = (Instrumentation) old;
		}
	}

	void injectorClassLoader() {
		String pkgName = context.getPackageName();

		Context contextImpl = ((ContextWrapper) context).getBaseContext();
		Object activityThread = Reflection.getField(contextImpl, "mMainThread");
		Map mPackages = (Map) Reflection.getField(activityThread, "mPackages");
		WeakReference weakReference = (WeakReference) mPackages.get(pkgName);
		if (weakReference == null) {
			log.e("loadedApk is null");
		} else {
			Object loadedApk = weakReference.get();
			if (loadedApk == null) {
				log.e("loadedApk is null");
				return;
			}
			if (appClassLoader == null) {
				ClassLoader old = (ClassLoader) Reflection.getField(loadedApk,
						"mClassLoader");
				appClassLoader = new AxAppClassLoader(old, this);
			}
			Reflection.setField(loadedApk, "mClassLoader", appClassLoader);
		}
	}

	public PluginInfo load(String apkfile, String packageName) {

		PluginInfo pluginInfo = new PluginInfo();

		try {
			boolean ok = pluginInfo.load(context, storagePath, apkfile, packageName);
			
			log.d("load plugin: " + apkfile + ", name:" + packageName + ", result:" + ok);
			
			if (!ok)
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized(this) {
			pluginInfoMap.put(pluginInfo.getPackageName(), pluginInfo);
		}
		return pluginInfo;
	}

	public Intent makeLaunchIntent(String pkgName) {
		Intent launchIntent = null;
		launchIntent = new Intent(context, ActivityStub.class);
		launchIntent.putExtra(intentPackageName, pkgName);

		return launchIntent;
	}

	private boolean isRegisteredActivity(ComponentName componentName) {
		try {
			ActivityInfo activityInfo = context.getPackageManager()
					.getActivityInfo(componentName,
							PackageManager.GET_META_DATA);
			return activityInfo != null;
		} catch (NameNotFoundException e) {
		}
		return false;
	}

	public void changeIntent(Context who, Intent intent) {
		if (intent.getStringExtra(AxPluginManager.intentPackageName) != null) {
			return;
		}
		if (intent.getStringExtra(AxPluginManager.intentClassName) != null) {
			return;
		}

		ComponentName componentName = intent.getComponent();
		if (componentName != null) {
			String pkgName = componentName.getPackageName();
			String className = componentName.getClassName();

			if (pkgName != null && isRegisteredActivity(componentName) == false) {
				intent.setComponent(new ComponentName(context,
						ActivityStub.class));
				intent.putExtra(AxPluginManager.intentPackageName, pkgName);
				intent.putExtra(AxPluginManager.intentClassName, className);
			}
		}
	}

	public void launch(Context activity, String apkfile, String packageName) {
		try {
			PluginInfo pluginInfo = load(apkfile, packageName);
			Intent i = makeLaunchIntent(pluginInfo.getPackageName());
			if (i == null) {
				log.e("get launch Intent fail: "
								+ pluginInfo.getPackageName());
				return;
			}

			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized PluginInfo getPlugin(String packageName) {
		return pluginInfoMap.get(packageName);
	}
	
	public synchronized void removePlugin(String packageName) {
		PluginInfo plugin = pluginInfoMap.remove(packageName);
		if (plugin != null) {
			plugin.destroy();
		}
		File file = new File(PluginInfo.getPluginDir(storagePath, packageName));
		if (file.exists()) {
			boolean ok = FileUtil.delete(file);
			if (!ok) {
				log.e("remove file fail: " + file);
				file.deleteOnExit();
			}
		}
	}
	
	public void loadAllFiles() {
		File dir = new File(storagePath);
		File[] files = dir.listFiles();
		if (files == null) return;
		
		for (File file : files) {
			if (file.isDirectory()) {
				File apkFile = new File(file, "p.apk");
				if (apkFile.exists()) {
					load(apkFile.getAbsolutePath(), file.getName());
				}
			}
		}
	}
}
