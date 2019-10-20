# HBase搭建--Fully-distributed

### 1、搭建方式说明

```
	By default, HBase runs in standalone mode. Both standalone mode and pseudo-distributed mode are provided for the purposes of small-scale testing. For a production environment, distributed mode is advised. In distributed mode, multiple instances of HBase daemons run on multiple servers in the cluster.
```

### 2、搭建步骤

1、将集群中的所有节点的hosts文件配置完成

2、将集群中的所有节点的防火墙关闭

3、将集群中的所有节点的时间设置一致

```
yum install ntpdate
ntpdate ntp1.aliyun.com
```

4、将所有的节点设置免密钥登陆

```
ssh-keygen
ssh-copy-id -i /root/.ssh/id_rsa.pub node01(节点名称)
```

5、解压hbase安装包

```
tar xzvf hbase-2.0.5-bin.tar.gz -C /opt/bigdata
cd hbase-2.0.5/
```

6、在/etc/profile文件中配置HBase的环境变量

```
export HBASE_HOME=/opt/bigdata/hbase-2.0.5
将$HBASE_HOME设置到PATH路径中
```

7、进入到/opt/bigdata/hbase-2.0.5/conf目录中，在hbase-env.sh文件中添加JAVA_HOME

```
设置JAVA的环境变量
JAVA_HOME=/usr/java/jdk1.8.0_181-amd64
设置是否使用自己的zookeeper实例
HBASE_MANAGES_ZK=false
```

8、进入到/opt/bigdata/hbase-2.0.5/conf目录中，在hbase-site.xml文件中添加hbase相关属性

```xml
<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>hdfs://mycluster/hbase</value>
  </property>
  <property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>node02,node03,node04</value>
  </property>
</configuration>
```

9、修改regionservers文件，设置regionserver分布在哪几台节点

```
node02
node03
node04
```

10、如果需要配置Master的高可用，需要在conf目录下创建backup-masters文件，并添加如下内容：

```
node04
```

11、拷贝hdfs-site.xml文件到conf目录下

```
cp /opt/bigdate/hbase-2.6.5/etc/hadoop/hdfs-site.xml /opt/bigdata/hbase-2.0.5/conf
```

12、在任意目录下运行hbase shell的命令，进入hbase的命令行进行相关操作。