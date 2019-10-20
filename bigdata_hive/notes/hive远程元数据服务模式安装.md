# hive远程元数据服务模式安装：

##### 1、选择两台虚拟机，node03作为服务端，node04作为客户端

##### 2、分别在Node03和node04上解压hive的安装包，或者在从node02上远程拷贝hive的安装包到Node03和node04

##### 3、node03修改hive-site.xml配置：

```
	<property>
		<name>hive.metastore.warehouse.dir</name>
		<value>/user/hive_remote/warehouse</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionURL</name>
		<value>jdbc:mysql://node01:3306/hive_remote?createDatabaseIfNotExist=true</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionDriverName</name>
		<value>com.mysql.jdbc.Driver</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionUserName</name>
		<value>root</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionPassword</name>
		<value>123</value>
	</property>
```

##### 4、node04修改hive-site.xml配置：

```
	<property>
		<name>hive.metastore.warehouse.dir</name>
		<value>/user/hive_remote/warehouse</value>
	</property>
	<property>
		<name>hive.metastore.uris</name>
		<value>thrift://node03:9083</value>
	</property>
```

##### 5、在node03服务端执行元数据库的初始化操作，schematool -dbType mysql -initSchema	

##### 6、在node03上执行hive --service metastore,启动hive的元数据服务，是阻塞式窗口

##### 7、在node04上执行hive，进入到hive的cli窗口