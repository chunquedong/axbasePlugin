/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import info.axbase.util.FileUtil;
import info.axbase.util.Reflection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

public class PluginInfo {

	private PackageInfo packageInfo;
	private String storagePath;

	private PluginClassLoader classLoader;

	AssetManager assetManager;
	Resources resources;
	private Application application;

	private String packageName;

	public String getPackageName() {
		return packageName;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	private String makeDir(String dirName) {
		String path = storagePath + "/" + dirName;
		FileUtil.ensureDir(path);
		return path;
	}

	public String getMainActivityName() {
		if (packageInfo.activities != null && packageInfo.activities.length > 0) {
			return packageInfo.activities[0].name;
		}
		return "";
	}

	public ActivityInfo findAcitivityInfo(String name) {
		for (ActivityInfo ai : packageInfo.activities) {
			if (name.equals(ai.name)) {
				return ai;
			}
		}
		return null;
	}

	public void destroy() {
		try {
			this.application.onTerminate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initApplication(Context context) {
		String appClassName;
		if (packageInfo.applicationInfo != null
				&& packageInfo.applicationInfo.className != null) {
			appClassName = packageInfo.applicationInfo.className;
		} else {
			appClassName = Application.class.getName();
		}
		try {
			application = (Application) getClassLoader()
					.loadClass(appClassName).newInstance();

			PluginContext contextHook = new PluginContext(context);
			contextHook.plugin = this;
			Reflection.setField(application, "mBase", contextHook);

			application.onCreate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getPluginDir(String storagePathBase, String packageName) {
		return storagePathBase + "/" + packageName;
	}

	public boolean load(Context context, String storagePathBase,
			String apkfilePath, String packageName) {

		File aokfile = new File(apkfilePath);
		if (!aokfile.exists()) {
			AxPluginManager.log.e("file not found: " + apkfilePath);
			return false;
		}

		PackageManager pm = context.getPackageManager();
		packageInfo = pm.getPackageArchiveInfo(apkfilePath,
				PackageManager.GET_ACTIVITIES | PackageManager.GET_RECEIVERS
						| PackageManager.GET_PROVIDERS
						| PackageManager.GET_META_DATA
						| PackageManager.GET_SHARED_LIBRARY_FILES
						| PackageManager.GET_SERVICES);
		if (packageInfo == null) {
			return false;
		}

		if (packageName == null) {
			this.packageName = packageInfo.packageName;
		} else {
			this.packageName = packageName;
		}

		storagePath = getPluginDir(storagePathBase, this.packageName);
		String optimizedDirectory;
		String nativeLibraryDir;
		String dexfile = storagePath + "/p.apk";

		File file = new File(storagePath);
		if (!file.exists()) {
			file.mkdir();
			optimizedDirectory = makeDir("olib");
			nativeLibraryDir = makeDir("lib");

			try {
				FileUtil.copyFile(apkfilePath, dexfile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			copyNativeLibs(context, dexfile, nativeLibraryDir);
		} else {
			optimizedDirectory = storagePath + "/" + ("olib");
			nativeLibraryDir = storagePath + "/" + ("lib");
		}

		classLoader = PluginClassLoader.make(dexfile, optimizedDirectory,
				nativeLibraryDir, context.getClassLoader());
		classLoader.plugin = this;
		loadAsset(context, dexfile);

		initApplication(context);
		return true;
	}

	private void loadAsset(Context context, String dexPath) {
		try {
			AssetManager am = (AssetManager) AssetManager.class.newInstance();
			am.getClass().getMethod("addAssetPath", String.class)
					.invoke(am, dexPath);
			assetManager = (am);
			Resources ctxres = context.getResources();
			PluginContext.PluginResources res = new PluginContext.PluginResources(
					am, ctxres.getDisplayMetrics(), ctxres.getConfiguration());
			res.old = ctxres;
			resources = res;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这段代码来自https://github.com/DroidPluginTeam/DroidPlugin
	 * @param context
	 * @param apkfile
	 * @param nativeLibraryDir
	 */
	private static void copyNativeLibs(Context context, String apkfile,
			String nativeLibraryDir) {
		ZipFile zipFile = null;
		try {
			try {
				zipFile = new ZipFile(apkfile);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			Map<String, ZipEntry> libZipEntries = new HashMap<String, ZipEntry>();
			Map<String, Set<String>> soList = new HashMap<String, Set<String>>(
					1);
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith("/")) {
					name = name.substring(1);
				}
				if (name.contains("../")) {
					AxPluginManager.log.w("error so path");
					continue;
				}
				if (name.startsWith("lib/") && !entry.isDirectory()) {
					libZipEntries.put(name, entry);
					String soName = new File(name).getName();
					Set<String> fs = soList.get(soName);
					if (fs == null) {
						fs = new TreeSet<String>();
						soList.put(soName, fs);
					}
					fs.add(name);
				}
			}

			for (String soName : soList.keySet()) {
				Set<String> soPaths = soList.get(soName);
				String soPath = findSoPath(soPaths);
				if (soPath != null) {
					File file = new File(nativeLibraryDir, soName);
					if (file.exists()) {
						file.delete();
					}
					InputStream in = null;
					FileOutputStream ou = null;
					try {
						in = zipFile.getInputStream(libZipEntries.get(soPath));
						ou = new FileOutputStream(file);
						byte[] buf = new byte[8192];
						int read = 0;
						while ((read = in.read(buf)) != -1) {
							ou.write(buf, 0, read);
						}
						ou.flush();
						ou.getFD().sync();
						AxPluginManager.log.i("copy so(" + soName + ") for "
								+ soPath + " to " + file.getPath() + " ok!");
					} catch (Exception e) {
						if (file.exists()) {
							file.delete();
						}
						e.printStackTrace();
						return;
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (Exception e) {
							}
						}
						if (ou != null) {
							try {
								ou.close();
							} catch (Exception e) {
							}
						}
					}
				}
			}
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private static String findSoPath(Set<String> soPaths) {
		if (soPaths != null && soPaths.size() > 0) {
			for (String soPath : soPaths) {
				if (!TextUtils.isEmpty(Build.CPU_ABI)
						&& soPath.contains(Build.CPU_ABI)) {
					return soPath;
				}
			}

			for (String soPath : soPaths) {
				if (!TextUtils.isEmpty(Build.CPU_ABI2)
						&& soPath.contains(Build.CPU_ABI2)) {
					return soPath;
				}
			}
		}
		return null;
	}
}
