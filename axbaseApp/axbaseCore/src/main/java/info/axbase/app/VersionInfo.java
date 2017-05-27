/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

class VersionInfo implements Serializable{
	private static final long serialVersionUID = 8445572940087456648L;
	
	public String fileName;
	public String whatsNew;
	public long fileTime;
	public long fileSize;
	public String appProject;
	public String digest;
	public String version;

	
	public static VersionInfo parse(String text) throws JSONException {
		JSONTokener jsonParser = new JSONTokener(text);    
		JSONObject msg = (JSONObject) jsonParser.nextValue();
		if (msg == null) {
			return null;
		}
		if (msg.optInt("error") == 0) {
			JSONObject data = msg.optJSONObject("data");
			if (data == null) {
				return null;
			}
			
			VersionInfo versionInfo = new VersionInfo();
			versionInfo.fileName = data.optString("fileName");
			versionInfo.whatsNew = data.optString("whatsNew");
			versionInfo.fileTime = data.optLong("fileTime");
			versionInfo.fileSize = data.optLong("fileSize");
			versionInfo.appProject = data.optString("appProject");
			versionInfo.digest = data.optString("digest");
			versionInfo.version = data.optString("version");
			return versionInfo;
		} else {
			Log.e("ParseError", msg.optString("msg"));
			return null;
		}
	}


	@Override
	public String toString() {
		return "VersionInfo [fileName=" + fileName + ", whatsNew=" + whatsNew
				+ ", fileTime=" + fileTime + ", fileSize=" + fileSize
				+ ", appProject=" + appProject + ", digest=" + digest + ", version="
				+ version + "]";
	}
	
}
