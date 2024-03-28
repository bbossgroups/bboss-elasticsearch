# Bboss源码构建指南
本文介绍如何从源码构建[bboss](https://esdoc.bbossgroups.com/#/README)，我们采用gradle来管理bboss源码工程，因此需先安装和配置好gradle（参考章节【[2.Gradle安装和配置](https://esdoc.bbossgroups.com/#/bboss-build?id=_2gradle%e5%ae%89%e8%a3%85%e5%92%8c%e9%85%8d%e7%bd%ae)】），然后利用gradle来构建Bboss。
# 1.从源码构建bboss

bboss采用模块化管理，多个模块相互依赖，可以从以下地址依次下载和构建各个模块源码工程：

| 源码地址                                   | 说明                                                         |
| ------------------------------------------ | ------------------------------------------------------------ |
| https://gitee.com/bboss/bboss              | [基础框架](https://doc.bbossgroups.com/#/)：包含ioc、持久层、mvc、任务调度管理、序列化/反序列化以及[配置管理](https://doc.bbossgroups.com/#/aop/IntroduceIoc)等功能 |
| https://gitee.com/bboss/bboss-data         | [Redis](https://doc.bbossgroups.com/#/redis)、MongoDB客户端封装 |
| https://gitee.com/bboss/bboss-http         | 轻量级[http](https://esdoc.bbossgroups.com/#/httpproxy)微服务框架 |
| https://gitee.com/bboss/bboss-plugins      | [kafka](https://doc.bbossgroups.com/#/kafka)、apollo对接框架 |
| https://gitee.com/bboss/bboss-elastic      | Elasticsearch Java [RestClient](https://esdoc.bbossgroups.com/#/quickstart) |
| https://gitee.com/bboss/bboss-elastic-tran | [数据采集](https://esdoc.bbossgroups.com/#/db-es-tool)ETL&[流批一体化](https://esdoc.bbossgroups.com/#/etl-metrics)计算框架 |

然后分别按顺序在命令行源码根目录执行gradle publishToMavenLocal指令构建各模块：

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

# 2.Gradle安装和配置

参考以下步骤配置和安装gradle运行环境,首先下载**最新**（与开发工具Idea或者Eclipse兼容即可）的gradle版本：

[下载gradle](https://gradle.org/releases) 

![image-20210619095208788](images\gradle.png)

下载完毕后解压，然后配置好gradle环境变量： 

GRADLE_HOME:  指定gradle安装目录

GRADLE_USER_HOME: 指定gradle从maven中央库下载依赖包本地存放目录 

M2_HOME: 一般还需要通过M2_HOME指定maven安装地址，这样gradle 构建的本地包才能被maven项目引用到，gradle 通过M2_HOME环境变量查找maven安装目录，一定要与idea或者eclipse中配置的maven安装目录一致

在系统环境变量Path添加gradle bin目录

![img](images/gradle_path.png)

添加GRADLE_HOME和GRADLE_USER_HOME环境变量：

![img](images/gradle_home.png)

配置M2_HOME环境变量： 

![img](images/m2_home.jpg)

M2_HOME变量中的maven安装路径要与idea中maven配置保持一致,进入setting，配置maven：

![image-20200510093315247](images/maven-idea.png)

新版本的idea必须调整导入工程的gradle配置，进入setting，设置工程的gradle配置：

![](images/mongodb/settingprojectgradle.png)



# 3.开发交流

**Elasticsearch技术交流群：21220580,166471282,3625720,154752521,166471103,166470856**

<img src="images/qrcode.jpg"  height="200" width="200"><img src="images/douyin.png"  height="200" width="200"><img src="images/wvidio.png"  height="200" width="200">



# 4.支持我们

如果您正在使用bboss，或是想支持我们继续开发，您可以通过如下方式支持我们：

1.Star并向您的朋友推荐或分享

[bboss elasticsearch client](https://gitee.com/bboss/bboss-elastic)🚀

[数据采集&流批一体化处理](https://gitee.com/bboss/bboss-elastic-tran)🚀

2.通过[爱发电 ](https://afdian.net/a/bbossgroups)直接捐赠，或者扫描下面二维码进行一次性捐款赞助，请作者喝一杯咖啡☕️





<img src="images/alipay.png"  height="200" width="200">

<img src="images/wchat.png" style="zoom:50%;" />

非常感谢您对开源精神的支持！❤您的捐赠将用于bboss社区建设、QQ群年费、网站云服务器租赁费用。