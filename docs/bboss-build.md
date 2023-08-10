# Elasticsearch Bboss源码构建

# Gradle安装和配置

在自己的电脑上装好gradle运行环境,下载**最新**的gradle版本：

[下载gradle](https://gradle.org/releases) 

![image-20210619095208788](images\gradle.png)

下载完毕后解压，然后配置好gradle环境变量： 

GRADLE_HOME:  指定gradle安装目录

GRADLE_USER_HOME: 指定gradle从maven中央库下载依赖包本地存放目录 

M2_HOME: 一般还需要通过M2_HOME指定maven安装地址，这样gradle 构建的本地包才能被maven项目引用到，gradle 通过M2_HOME环境变量查找maven安装目录，一定要与idea或者eclipse中配置的maven安装目录一致

![img](images/gradle_path.png)
![img](images/gradle_home.png)

一般还需要通过M2_HOME指定maven安装地址，这样gradle 构建的本地包才能被maven项目引用到，gradle 通过M2_HOME环境变量查找maven安装环境，一定要与idea或者eclipse中配置的maven安装目录一致，配置M2_HOME环境变量如下图： 

![img](images/m2_home.jpg)

M2_HOME变量中的maven安装路径要与idea中maven配置保持一致,进入setting，配置maven：

![image-20200510093315247](images/maven-idea.png)

新版本的idea必须调整导入工程的gradle配置，进入setting，设置工程的gradle配置：

![](images/mongodb/settingprojectgradle.png)

# 构建Elasticsearch bboss源码

安装后gradle后，我们就可以利用gradle来构建Elasticsearch Bboss的源码了。

从以下地址依次下载和构建bboss相关源码工程：

https://gitee.com/bboss/bboss

https://gitee.com/bboss/bboss-data

https://gitee.com/bboss/bboss-http

https://gitee.com/bboss/bboss-plugins

https://gitee.com/bboss/bboss-elastic

https://gitee.com/bboss/bboss-elastic-tran  

然后分别按顺序在命令行源码根目录执行gradle publishToMavenLocal指令构建bboss 源码：

```shell
cd bboss
gradle publishToMavenLocal

cd bboss-data
gradle publishToMavenLocal

cd bboss-http
gradle publishToMavenLocal

cd bboss-plugins
gradle publishToMavenLocal


cd bboss-elastic
gradle publishToMavenLocal

cd bboss-elastic-tran
gradle publishToMavenLocal
```

# 开发交流

**Elasticsearch技术交流群：21220580,166471282**

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



# 支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️





<img src="images/alipay.png"  height="200" width="200">

<img src="images/wchat.png" style="zoom:50%;" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。