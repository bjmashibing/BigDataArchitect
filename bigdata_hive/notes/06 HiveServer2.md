# HiveServer2

### 基本概念介绍

1、HiveServer2基本介绍

```sql
HiveServer2 (HS2) is a server interface that enables remote clients to execute queries against Hive and retrieve the results (a more detailed intro here). The current implementation, based on Thrift RPC, is an improved version of HiveServer and supports multi-client concurrency and authentication. It is designed to provide better support for open API clients like JDBC and ODBC.
```

​	 	HiveServer2是一个服务接口，能够允许远程的客户端去执行SQL请求且得到检索结果。HiveServer2的实现，依托于Thrift RPC，是HiveServer的提高版本，它被设计用来提供更好的支持对于open API例如JDBC和ODBC。

```
HiveServer is an optional service that allows a remote client to submit requests to Hive, using a variety of programming languages, and retrieve results. HiveServer is built on Apache ThriftTM (http://thrift.apache.org/), therefore it is sometimes called the Thrift server although this can lead to confusion because a newer service named HiveServer2 is also built on Thrift. Since the introduction of HiveServer2, HiveServer has also been called HiveServer1.
```

​		HiveServer是一个可选的服务，只允许一个远程的客户端去提交请求到hive中。（目前已被淘汰）

2、Beeline

​		HiveServer2 supports a command shell Beeline that works with HiveServer2. It's a JDBC client that is based on the SQLLine CLI 。

​		HiveServer2提供了一种新的命令行接口，可以提交执行SQL语句。

### hiveserver2的搭建使用

​	在搭建hiveserver2服务的时候需要修改hdfs的超级用户的管理权限，修改配置如下：

```sql
--在hdfs集群的core-site.xml文件中添加如下配置文件
	<property>
		<name>hadoop.proxyuser.root.groups</name>	
		<value>*</value>
    </property>
    <property>
		<name>hadoop.proxyuser.root.hosts</name>	
		<value>*</value>
    </property>
--配置完成之后重新启动集群，或者在namenode的节点上执行如下命令
	hdfs dfsadmin -fs hdfs://node01:8020 -refreshSuperUserGroupsConfiguration
	hdfs dfsadmin -fs hdfs://node02:8020 -refreshSuperUserGroupsConfiguration

```

1、独立hiveserver2模式

​	1、将现有的所有hive的服务停止，不需要修改任何服务，在node03机器上执行hiveserver2或者hive --service hiveserver2的命令，开始启动hiveserver2的服务，hiveserver2的服务也是一个阻塞式窗口，当开启服务后，会开启一个10000的端口，对外提供服务。

​	2、在node04上使用beeline的方式进行登录

2、共享metastore server的hiveserver2模式搭建

​	1、在node03上执行hive --service metastore启动元数据服务

​	2、在node04上执行hiveserver2或者hive --service hiveserver2两个命令其中一个都可以

​	3、在任意一台包含beeline脚本的虚拟机中执行beeline的命令进行连接

### HiveServer2的访问方式

##### 	1、beeline的访问方式

​	（1）beeline -u jdbc:hive2://<host>:<port>/<db> -n name

​	（2）beeline进入到beeline的命令行
​				beeline> !connect jdbc:hive2://<host>:<port>/<db> root 123

​		注意：

​			1、使用beeline方式登录的时候，默认的用户名和密码是不验证的，也就是说随便写用户名和密码即可

​			2、使用第一种beeline的方式访问的时候，用户名和密码可以不输入

​			3、使用第二种beeline方式访问的时候，必须输入用户名和密码，用户名和密码是什么无所谓

##### 	2、jdbc的访问方式

​	1、创建普通的java项目，将hive的jar包添加到classpath中，最精简的jar包如下：

```
commons-lang-2.6.jar
commons-logging-1.2.jar
curator-client-2.7.1.jar
curator-framework-2.7.1.jar
guava-14.0.1.jar
hive-exec-2.3.4.jar
hive-jdbc-2.3.4.jar
hive-jdbc-handler-2.3.4.jar
hive-metastore-2.3.4.jar
hive-service-2.3.4.jar
hive-service-rpc-2.3.4.jar
httpclient-4.4.jar
httpcore-4.4.jar
libfb303-0.9.3.jar
libthrift-0.9.3.jar
log4j-1.2-api-2.6.2.jar
log4j-api-2.6.2.jar
log4j-core-2.6.2.jar
log4j-jul-2.5.jar
log4j-slf4j-impl-2.6.2.jar
log4j-web-2.6.2.jar
zookeeper-3.4.6.jar
```

​	

​	2、编辑如下代码：

```java
package com.mashibing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HiveJdbcClient {

	private static String driverName = "org.apache.hive.jdbc.HiveDriver";

	public static void main(String[] args) throws SQLException {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Connection conn = DriverManager.getConnection("jdbc:hive2://node04:10000/default", "root", "");
		Statement stmt = conn.createStatement();
		String sql = "select * from psn limit 5";
		ResultSet res = stmt.executeQuery(sql);
		while (res.next()) {
			System.out.println(res.getString(1) + "-" + res.getString("name"));
		}
	}
}

```

运行之后，即可得到最终结果。

