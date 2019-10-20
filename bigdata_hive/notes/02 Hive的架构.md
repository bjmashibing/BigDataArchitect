# 02 Hive的架构

### 1、Hive的架构图

![Hive架构图](https://github.com/msbbigdata/hive/blob/master/images/hive架构图.png)

### 2、Hive的服务（角色）

##### 	1、用户访问接口

​		CLI（Command Line Interface）：用户可以使用Hive自带的命令行接口执行Hive QL、设置参数等功能

​		JDBC/ODBC：用户可以使用JDBC或者ODBC的方式在代码中操作Hive

​		Web GUI：浏览器接口，用户可以在浏览器中对Hive进行操作（2.2之后淘汰）

##### 	2、Thrift Server:

​		Thrift服务运行客户端使用Java、C++、Ruby等多种语言，通过编程的方式远程访问Hive

##### 	3、Driver

​		Hive Driver是Hive的核心，其中包含解释器、编译器、优化器等各个组件，完成从SQL语句到MapReduce任务的解析优化执行过程

##### 	4、metastore

​	Hive的元数据存储服务，一般将数据存储在关系型数据库中，为了实现Hive元数据的持久化操作，Hive的安装包中自带了Derby内存数据库，但是在实际的生产环境中一般使用mysql来存储元数据	

**未完待续。。。。。。。**			

### 3、Hive的访问流程图

![Hive访问流程图](https://github.com/msbbigdata/hive/blob/master/images/访问流程图.png)