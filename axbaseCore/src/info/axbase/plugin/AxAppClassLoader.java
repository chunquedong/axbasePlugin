/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import java.util.Map;

class AxAppClassLoader extends ClassLoader {
	private AxPluginManager pluginManager;

	AxAppClassLoader(ClassLoader parentLoader, AxPluginManager pluginManager) {
		super(parentLoader);
		this.pluginManager = pluginManager;
	}

	private Class<?> safeLoader(String className, ClassLoader loader) {
		try {
			return loader.loadClass(className);
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	private Class<?> tryLoadClassInPlugin(String className) {
		synchronized (pluginManager) {
			Class<?> clazz = null;
			// find in all plugin
			if (className != null) {
				for (Map.Entry<String, PluginInfo> entry : pluginManager
						.getPluginInfoMap().entrySet()) {
					clazz = safeLoader(className, entry.getValue()
							.getClassLoader());
					if (clazz != null)
						return clazz;
				}
			}
			return null;
		}
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		if (PluginClassLoader.classLoaderType == 0) {
			try {
				return super.loadClass(className);
			} catch (ClassNotFoundException e) {
				Class<?> clazz = tryLoadClassInPlugin(className);
				if (clazz != null) {
					return clazz;
				} else {
					throw e;
				}
			}
		} else {
			Class<?> clazz = tryLoadClassInPlugin(className);
			if (clazz != null) {
				return clazz;
			}
			try {
				return super.loadClass(className);
			} catch (ClassNotFoundException e) {
				synchronized (pluginManager) {
					// find in all plugin
					for (Map.Entry<String, PluginInfo> entry : pluginManager
							.getPluginInfoMap().entrySet()) {
						AxPluginManager.log.w("ClassLoaderDump:"
								+ entry.getValue().getClassLoader());
					}
				}
				throw e;
			}
		}
	}
}
