# 03 Hive的安装搭建

Hive可以从源码中编译安装，也可以直接使用官网下载的安装包，在此处我们选择安装包解压安装的方式。

**Hive中最最重要的角色就是metastore**

因此按照metastore的管理共有四种hive的安装搭建方式：官网参考地址如下：

https://cwiki.apache.org/confluence/display/Hive/AdminManual+Metastore+Administration

##### Hive安装分类：

​	1、Local/Embedded Metastore Database（Derby）

​	2、Remote Metastore Database

​	3、Local/Embedded Metastore Server

​	4、Remote Metastore Server

​	根据上述分类，可以简单归纳为以下三类

​	1、使用Hive自带的内存数据库Derby作为元数据存储

​	2、使用远程数据库mysql作为元数据存储

​	3、使用本地/远程元数据服务模式安装Hive

##### 详细操作：

​	1、使用Hive自带的内存数据库Derby作为元数据存储

![1563262098436](https://github.com/msbbigdata/hive/blob/master/images/本地数据库模式安装.png)

​	2、使用远程数据库mysql作为元数据存储

![1563262117701](https://github.com/msbbigdata/hive/blob/master/images/远程数据库模式安装.png))

​	3、使用本地/远程元数据服务模式安装Hive

![1563262138267](https://github.com/msbbigdata/hive/blob/master/images/远程元数据服务安装.png)