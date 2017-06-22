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
	public int forceRestart = 1;
	
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
	
	/**
	 * 类加载器类型。当宿主和插件包含相同类时：0表示宿主优先，1表示插件优先。推荐不要在插件和宿主中打包相同的类。类加载器会有两个常见问题：
	 * 1.插件优先时，可能会导致宿主和插件的类被隔离到不同的空间，在相互调用时可能会产生ClassCastException异常。
	 * 2.宿主优先时，在Android4.4以下系统，如果插件和宿主有相同的类，在某些情况下可能会出现Class ref in pre-verified异常。解决方法见https://github.com/chunquedong/axbasePlugin/wiki
	 */
	public int classLoaderType = 0;
}
