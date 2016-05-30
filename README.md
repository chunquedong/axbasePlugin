#Overview
axbasePlugin是轻量级Android插件化开发框架。能实现在不重新安装的情况下动态更新应用。

#Features
- 完整解决方案，不止核心引擎
- 非隔离式，宿主和插件间可自由通信
- 非侵入式设计，插件和普通App代码编写方式无区别
- 插件可作为独立App安装运行，便于测试和独立分发
- 支持Service等四大组件，和.so文件
- 支持assets目录中预制插件和网络下载方式
- 不需要手动分配资源ID或者使用定制aapt打包
- 最小化Hook系统私有API数量，运行更稳定
- 插件库大小不到60k，极致体积

#Source Code
- 源码中axbaseCore为插件框架的核心代码。
- axbaseHostApp和axbasePlugin分别为宿主Demo和插件Demo。
- 其中axbaseHostApp为AndroidStudio项目，其他为EclipseADT项目。

#Links
- [项目主页](http://www.axbase.info)
- [使用说明](http://www.axbase.info/Doc/start)
- [原理介绍](http://my.oschina.net/chunquedong/blog/676946)
- [常见问题](https://github.com/chunquedong/axbasePlugin/wiki)
