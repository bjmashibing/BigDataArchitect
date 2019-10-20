# Hive参数操作和运行方式

### 1、Hive参数操作

##### 	1、hive参数介绍

​		hive当中的参数、变量都是以命名空间开头的，详情如下表所示：

| **命名空间** | **读写权限** | **含义**                                                     |
| ------------ | ------------ | ------------------------------------------------------------ |
| hiveconf     | 可读写       | hive-site.xml当中的各配置变量例：hive --hiveconf hive.cli.print.header=true |
| system       | 可读写       | 系统变量，包含JVM运行参数等例：system:user.name=root         |
| env          | 只读         | 环境变量例：env：JAVA_HOME                                   |
| hivevar      | 可读写       | 例：hive -d val=key                                          |

​		hive的变量可以通过${}方式进行引用，其中system、env下的变量必须以前缀开头

##### 	2、hive参数的设置方式

​		1、在${HIVE_HOME}/conf/hive-site.xml文件中添加参数设置

​			**注意：永久生效，所有的hive会话都会加载对应的配置**

​		2、在启动hive cli时，通过--hiveconf key=value的方式进行设置

​			例如：hive --hiveconf hive.cli.print.header=true

​			**注意：只在当前会话有效，退出会话之后参数失效**

​		3、在进入到cli之后，通过set命令设置

​			例如：set hive.cli.print.header=true;

```sql
--在hive cli控制台可以通过set对hive中的参数进行查询设置
--set设置
	set hive.cli.print.header=true;
--set查看
	set hive.cli.print.header
--set查看全部属性
	set
```

​		4、hive参数初始化设置

​			在当前用户的家目录下创建**.hiverc**文件，在当前文件中设置hive参数的命令，每次进入hive cli的时候，都会加载.hiverc的文件，执行文件中的命令。

​			**注意：在当前用户的家目录下还会存在.hivehistory文件，此文件中保存了hive cli中执行的所有命令**

### 2、hive运行方式

##### 	1、hive运行方式分类

​		（1）命令行方式或者控制台模式

​		（2）脚本运行方式（实际生产环境中用最多）

​		（3）JDBC方式：hiveserver2

​		（ 4）web GUI接口（hwi、hue等）

##### 	2、hive命令行模式详解

​		（1）在命令行中可以直接输入SQL语句，例如：select * from table_name

​		（2）在命令行中可以与HDFS交互，例如：dfs ls /

​		（3）在命令行中可以与linux交互，例如：! pwd或者! ls /

​					**注意：与linux交互的时候必须要加!**

##### 	3、hive脚本运行方式

```sql
--hive直接执行sql命令，可以写一个sql语句，也可以使用;分割写多个sql语句
	hive -e ""
--hive执行sql命令，将sql语句执行的结果重定向到某一个文件中
	hive -e "">aaa
--hive静默输出模式，输出的结果中不包含ok，time token等关键字
	hive -S -e "">aaa
--hive可以直接读取文件中的sql命令，进行执行
	hive -f file
--hive可以从文件中读取命令，并且执行初始化操作
	hive -i /home/my/hive-init.sql
--在hive的命令行中也可以执行外部文件中的命令
	hive> source file (在hive cli中运行)
```

​	4、hive JDBC访问方式，之前讲过，不再赘述

​	5、Hive GUI方式

​		![]([https://github.com/msbbigdata/hive/blob/master/images/hive%20hwi.png])