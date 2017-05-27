/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

public class Logcat {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm:ss_yyyy-MM-dd", Locale.US);

	private static Logcat instance = null;
	private String path;
	private LogDumper mLogDumper = null;

	static String generateFileName() {
		return dateFormat.format(new Date());
	}

	public static Logcat getInstance(Context context) {
		if (instance == null) {
			instance = new Logcat(context);
		}
		return instance;
	}

	private Logcat(Context context) {
		String dir = context.getPackageName();
		path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + dir;
	}

	public void start() {
		if (mLogDumper == null) {
			mLogDumper = new LogDumper();
		}
		mLogDumper.start();
	}

	public void stop() {
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		private String cmds = null;
		private FileOutputStream out = null;

		public LogDumper() {
			try {
				File file = new File(path);
				if (!file.exists()) {
					file.mkdirs();
				}

				out = new FileOutputStream(new File(path, generateFileName()
						+ ".txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			cmds = "logcat";
		}

		public void stopLogs() {
			mRunning = false;
		}

		private void close() {
			if (logcatProc != null) {
				logcatProc.destroy();
				logcatProc = null;
			}
			if (mReader != null) {
				try {
					mReader.close();
					mReader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = null;
			}
		}

		@Override
		public void run() {
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(
						logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null) {
						out.write((line + "\n").getBytes());
						out.flush();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}

	}

}
