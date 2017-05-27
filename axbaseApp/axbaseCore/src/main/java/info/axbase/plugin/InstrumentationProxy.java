/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.plugin;

import info.axbase.util.Reflection;
import android.app.Activity;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

public class InstrumentationProxy extends Instrumentation {
	Instrumentation old;
	
	protected void onStartActivity(Context who, Intent intent) {
	}
	
	public ActivityResult execStartActivity(
	        Context who, IBinder contextThread, IBinder token, String target,
	        Intent intent, int requestCode, Bundle options) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode, options);
		return r;
	}
	
	public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, UserHandle user) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode, options, user);
		return r;
	}
	
	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
			Intent intent, int requestCode, Bundle options) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode, options);
		return r;
	}

	public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
			Fragment target, Intent intent, int requestCode, Bundle options) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode, options);
		return r;
	}

	public ActivityResult execStartActivity(
				Context who, IBinder contextThread, IBinder token, Activity target,
				Intent intent, int requestCode) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode);
		return r;
	}

	public ActivityResult execStartActivity(
			Context who, IBinder contextThread, IBinder token, Fragment target,
			Intent intent, int requestCode) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivity"
				, who, contextThread, token, target, intent, requestCode);
		return r;
	}

	public ActivityResult execStartActivityAsCaller(
			            Context who, IBinder contextThread, IBinder token, Activity target,
			            Intent intent, int requestCode, Bundle options, int user) {
		onStartActivity(who, intent);
		ActivityResult r = (ActivityResult) Reflection.callMethod(old, "execStartActivityAsCaller"
				, who, contextThread, token, target, intent, requestCode, options, user);
		return r;
	}
}
