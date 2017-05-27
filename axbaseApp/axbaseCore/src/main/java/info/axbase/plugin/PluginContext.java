/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;

public class PluginContext extends ContextWrapper {
	PluginInfo plugin;

	public static class PluginResources extends Resources {
		Resources old;

		public PluginResources(AssetManager assets, DisplayMetrics metrics,
				Configuration config) {
			super(assets, metrics, config);
		}

		public void getValue(int id, TypedValue outValue, boolean resolveRefs)
				throws NotFoundException {
			try {
				super.getValue(id, outValue, resolveRefs);
			} catch (NotFoundException e) {
				e.printStackTrace();
				old.getValue(id, outValue, resolveRefs);
			}
		}

		public Drawable getDrawable(int id) throws NotFoundException {
			try {
				return super.getDrawable(id);
			} catch (NotFoundException e) {
				return old.getDrawable(id);
			}
		}

		@SuppressLint("NewApi")
		public Drawable getDrawable(int id, Theme theme)
				throws NotFoundException {
			try {
				return super.getDrawable(id, theme);
			} catch (NotFoundException e) {
				e.printStackTrace();
				return old.getDrawable(id, theme);
			}
		}
	}

	public PluginContext(Context base) {
		super(base);
	}

	@Override
	public AssetManager getAssets() {
		return plugin.assetManager;
	}

	@Override
	public Resources getResources() {
		return plugin.resources;
	}

//	@Override
//	public ApplicationInfo getApplicationInfo() {
//		return plugin.packageInfo.applicationInfo;
//	}
//
//	@Override
//	public String getPackageName() {
//		return plugin.getPackageName();
//	}
	
	@Override
    public ClassLoader getClassLoader() {
        return plugin.getClassLoader();
    }
	
	private LayoutInflater mInflater;
	
	@Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mInflater;
        }
        return super.getSystemService(name);
    }
	
}
