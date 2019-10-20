# Hive远程数据库模式安装

### 安装hive的步骤：

##### 	1、解压安装

##### 	2、修改环境变量

```
		vi /etc/profile
		export HIVE_HOME=/opt/bigdata/hive-2.3.4
		将bin目录添加到PATH路径中
```

##### 	3、修改配置文件，进入到/opt/bigdata/hive-2.3.4/conf

```java
	//修改文件名称，必须修改，文件名称必须是hive-site.xml
		mv hive-default.xml.template hive-site.xml
	//增加配置：
			进入到文件之后，将文件原有的配置删除，但是保留最后一行，
			从<configuration></configuration>，将光标移动到<configuration>这一行，
			在vi的末行模式中输入以下命令
			:.,$-1d
	//增加如下配置信息：
			<property>
				<name>hive.metastore.warehouse.dir</name>
				<value>/user/hive/warehouse</value>
			</property>
			<property>
				<name>javax.jdo.option.ConnectionURL</name>
				<value>jdbc:mysql://node01:3306/hive?createDatabaseIfNotExist=true</value>
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

##### 	4、添加MySQL的驱动包拷贝到lib目录

##### 	5、执行初始化元数据数据库的步骤

```
		schematool -dbType mysql -initSchema
```

##### 	6、执行hive启动对应的服务

##### 	7、执行相应的hive SQL的基本操作
