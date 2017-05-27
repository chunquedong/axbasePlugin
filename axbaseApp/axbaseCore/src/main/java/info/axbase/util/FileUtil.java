/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class FileUtil {
	
	public static void copyAsset(Context context, String name, String dst) throws IOException {
		BufferedInputStream in = null;
		BufferedOutputStream ou = null;
		try {
			in = new BufferedInputStream(context.getAssets().open(name));
			ou = new BufferedOutputStream(new FileOutputStream(dst));
			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				ou.write(buffer, 0, read);
			}
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

	public static void copyFile(String src, String dst) throws IOException {
		BufferedInputStream in = null;
		BufferedOutputStream ou = null;
		try {
			in = new BufferedInputStream(new FileInputStream(src));
			ou = new BufferedOutputStream(new FileOutputStream(dst));
			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				ou.write(buffer, 0, read);
			}
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

	public static void ensureDir(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		} else if (!file.isDirectory()) {
			throw new RuntimeException("not dir:" + dir);
		}
	}

	public static boolean delete(File file) {
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				return file.delete();
			}
			for (File f : childFile) {
				boolean ok = delete(f);
				if (!ok) {
					Log.d("axbase", "remove file fail: " + f);
				}
			}
			return file.delete();
		} else {
			return file.delete();
		}
	}
}
