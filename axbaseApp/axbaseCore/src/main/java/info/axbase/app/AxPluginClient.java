/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;

import info.axbase.plugin.AxPluginManager;
import info.axbase.plugin.PluginInfo;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class AxPluginClient extends PluginClient {
	private AxPluginManager plugMgr;
	private Map<String, String> plugMap = new HashMap<String, String>();

	@Override
	protected void onInit(Context context) {
		plugMgr = AxPluginManager.getInstance();
		plugMgr.init(context);
	}

	@Override
	protected void uninstall(String id) {
		String pkgName = plugMap.get(id);
		plugMgr.removePlugin(pkgName);
	}

	@Override
	protected void install(String file, String id) {
		try {
			PluginInfo pluginInfo = plugMgr.load(file, id);
			plugMap.put(id, pluginInfo.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean isInstalled(String id) {
		String pkgName = plugMap.get(id);
		return plugMgr.getPlugin(pkgName) != null;
	}

	@Override
	public boolean startMainActivity(Context context, String id) {
		String pkgName = plugMap.get(id);
		Intent i = plugMgr.makeLaunchIntent(pkgName);
		context.startActivity(i);
		return true;
	}

	@Override
	public void startActivity(Context context, Intent intent) {
		plugMgr.changeIntent(context, intent);
		context.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Activity activity, Intent intent,
			int requestCode) {
		plugMgr.changeIntent(context, intent);
		activity.startActivityForResult(intent, requestCode);
	}
}