# linux安装mysql步骤：

##### 1、在linux上使用yum的方式安装软件

```
yum install mysql-server -y
```

##### 2、启动mysql的服务

```
service mysqld start
```

##### 3、将mysql服务设置成开机启动（有些同学关闭虚拟机之后，下次重新启动的时候可能忘记开启服务，因此将mysql服务设置完开机启动，不会占用太多的开机时间）

```
chkconfig mysqld on
```

##### 4、进入到mysql的命令行，敲击以下命令

```
mysql
```

##### 5、mysql命令行命令(安装完mysql之后需要修改mysql的登录权限)

```sql
--切换数据库
	use mysql
--查看表
	show tables
--查看表的数据
	select  host,user,password from user;
--插入权限数据
	grant all privileges on *.* to 'root'@'%' identified by '123' with grant option
--删除本机的用户访问权限（可以执行也可以不执行）	
	delete from user where host!='%'
--刷新权限或者重启mysqld的服务
	service mysqld restart;--（重启mysql服务）
	flush privileges;--(刷新权限)	
```

