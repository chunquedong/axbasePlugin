# Overview
AxbasePlugin是轻量级开源Android插件化开发框架。可实现在不重新安装的情况下动态更新应用。

# Features
- 完整解决方案，不止核心引擎
- 非隔离式，宿主和插件间可自由通信
- 非侵入式设计，插件和普通App代码编写方式无区别
- 插件可作为独立App安装运行，便于测试和独立分发
- 支持Service等四大组件，和.so文件
- 支持assets目录中预置插件和网络下载方式
- 不需要手动分配资源ID或者使用定制aapt打包
- 最小化Hook系统私有API数量，运行更稳定
- 插件库大小不到60k，极致体积

# Source Code
- 源码中axbaseCore为插件框架的核心代码。
- axbaseHost为宿主Demo，axbasePlugin为插件Demo。
- axbaseProtocl是宿主和插件通信的示例，可选。

# Usage
在宿主AndroidMainifest.xml注册插件使用的组件:
```
<service android:name="info.axbase.app.UpdateService"/>
<activity android:name="info.axbase.plugin.ActivityStub"/>
```
在Application.onCreate（必须是Application的）中初始化。
```
PluginClient.init(this);
```
启动插件：
```
PluginClient.getInstance()
.launch("0729c758-3216-3c80-3113-0242ac110150(你新建项目获得的AppID)", MainActivity.this, false);
```
插件包0729c758-3216-3c80-3113-0242ac110150@0.apk(后面的@0指版本号)放入assets目录中。后续上传版本的版本会自动更新。

# More
- [项目主页](http://www.axbase.info)
- [使用说明](http://www.axbase.info/Doc/start)
- [原理介绍](http://my.oschina.net/chunquedong/blog/676946)
- [常见问题](https://github.com/chunquedong/axbasePlugin/wiki)

其他问题请提在[Issues页](https://github.com/chunquedong/axbasePlugin/issues)
