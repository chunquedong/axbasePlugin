/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;

import info.axbase.app.UpdateService.OfflineVersion;
import info.axbase.util.HttpClient;
import info.axbase.util.HttpClient.HttpException;
import info.axbase.util.HttpClient.HttpHandler;
import info.axbase.util.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;

class DownloadTask {
	private UpdateService service;

	private HttpClient httpClient = new HttpClient();
	private File downloadStoragePath;
	private int installType;

	private static String urlHost = "http://www.axbase.info";

	String getHost() {
		return urlHost;
	}

	static void setHost(String url) {
		urlHost = url;
	}

	String getImei() {
		String imei = null;
		try {
			imei = service.telephonyManager.getDeviceId();
			return imei;
		} catch (Exception ex) {
		}
		
		if (imei != null) {
			return imei;
		}
		return "unknow";
	}

	String getPackageName() {
		return service.packageName;
	}

	public DownloadTask(UpdateService service, int installType) {
		this.service = service;
		this.downloadStoragePath = service.downloadStoragePath;
		this.installType = installType;
	}

	public void request(String appId) throws HttpException, JSONException {
		String urlStr = getHost() + "/AppProjects/checkUpdate/" + appId
				+ "?did=" + getImei() + "&pkg=" + getPackageName();
		String text = httpClient.get(urlStr);
		VersionInfo versionInfo = VersionInfo.parse(text);
		if (versionInfo == null)
			return;

		PluginClient.log.d(versionInfo.toString());

		OfflineVersion ov = service.getCache(appId);
		if (ov != null) {
			if (versionInfo.version.equals(ov.version)
					&& versionInfo.fileSize == ov.file.length()) {
				PluginClient.log.d(appId + " version is ok");
				onComplete(versionInfo, true, null, true);
				return;
			}
		}

		PluginClient.log.d("doDownload " + appId);
		doDownload(versionInfo);
	}

	public class DownloadHttpHandler implements HttpHandler {
		VersionInfo versionInfo;

		@Override
		public void writeReq(OutputStream out) throws IOException {
		}

		public void pipe(InputStream in, OutputStream out, int length)
				throws IOException {
			try {
				byte[] bytes = new byte[StreamUtil.bufferSize];
				int len = 0;
				int count = 0;
				int bufCount = 0;
				if (length <= 0) {
					length = (int) versionInfo.fileSize;
				}

				while ((len = in.read(bytes)) != -1) {
					out.write(bytes, 0, len);

					count += len;

					++bufCount;
					if (bufCount > 10) {
						float percent = count / (float) length;
						service.onProgress(versionInfo.appProject, percent,
								length, installType);
						bufCount = 0;

						if (PluginClient.config.isDebug) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void readRes(InputStream in, int length) throws IOException {
			File file = new File(downloadStoragePath, "temp_"
					+ versionInfo.appProject);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream out = new FileOutputStream(file);
			pipe(in, out, length);
			out.close();

			long fileSize = file.length();
			if (versionInfo.fileSize == fileSize) {

				FileInputStream fin = new FileInputStream(file);
				String digest = "" + StreamUtil.crc32(fin);

				if (versionInfo.digest.equals(digest)) {
					File newPath = new File(downloadStoragePath,
							UpdateService.getFileName(versionInfo));
					file.renameTo(newPath);
					service.registApp(versionInfo.appProject,
							versionInfo.version, newPath);
					onComplete(versionInfo, true, null, false);
				} else {
					onComplete(versionInfo, false, "diest error", false);
				}
			} else {
				onComplete(versionInfo, false, "file size error", false);
			}
		}

		@Override
		public void onError(int code, Object msg) {
			onComplete(versionInfo, false, "net error", false);
		}

	}

	private void doDownload(VersionInfo versionInfo) {
		DownloadHttpHandler handler = new DownloadHttpHandler();
		handler.versionInfo = versionInfo;
		String url = getHost() + "/AppProjects/download/"
				+ versionInfo.appProject + "?version=" + versionInfo.version
				+ "&did=" + getImei() + "&pkg=" + getPackageName();
		httpClient.doRequest(url, "GET", handler);
	}

	private void onComplete(VersionInfo versionInfo, boolean success,
			String error, boolean cache) {
		if (!success) {
			PluginClient.log.w(versionInfo.appProject + "," + error);
		}

		service.onComplete(versionInfo.appProject, versionInfo, success, error,
				installType, cache);
	}
}
