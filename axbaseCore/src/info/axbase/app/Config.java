/*
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.app;
/**
 * 配置选项
 *
 */
public class Config {
	public String hostUrl = null;
	
	/**
	 * 启动时检查更新
	 */
	public boolean updateWhenLaunch = false;
	
	/**
	 * 在更新应用后强制重启
	 */
	public boolean forceRestart = false;
	
	/**
	 * 只在wifi下更新应用
	 */
	public boolean updateOnlyWifi = true;
	
	/**
	 * 是否为调试模式，调试下将打印大量日志
	 */
	public boolean isDebug = false;
	
	/**
	 * 是否使用assets目录预置的apk插件
	 */
	public boolean copyAsset = true;
	
	/**
	 * 检查更新时间间隔
	 */
	public long checkUpdateTime = 15 * 60 * 1000;
}
