/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.util;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class SLogger {
	private final String tag;
	
	public static final int levelDebug = 0;
	public static final int levelInfo = 1;
	public static final int levelWarn = 2;
	public static final int levelError = 3;
	public static final int levelSilent = 4;
	
	private static int defaultLevel = levelInfo;
	
	private int level = defaultLevel;
	
	private static Map<String, SLogger> map = new HashMap<String, SLogger>();
	
	public static SLogger get(String name) {
		SLogger logger = map.get(name);
		if (logger == null) {
			logger = new SLogger(name);
			map.put(name, logger);
		}
		return logger;
	}
	
	private SLogger(String tag) {
		this.tag = tag;
	}
	
	public static void setDefaultLevel(int l) {
		defaultLevel = l;
	}
	
	public boolean isDebug() {
		return level == levelDebug;
	}
	
	public void setLevel(int l) {
		level = l;
	}
	
	boolean loggable(int l) {
		return l >= level;
	}
	
	public void e(String msg) {
		if (!loggable(levelDebug)) return;
		Log.e(tag, msg);
	}

	public void d(String msg) {
		if (!loggable(levelDebug)) return;
		Log.d(tag, msg);
	}
	
	public void i(String msg) {
		if (!loggable(levelDebug)) return;
		Log.d(tag, msg);
	}
	
	public void w(String msg) {
		if (!loggable(levelDebug)) return;
		Log.d(tag, msg);
	}
}
