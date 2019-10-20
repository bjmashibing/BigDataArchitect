# HBase搭建--Standalone HBase

### 1、搭建方式说明

```
	the setup of a single-node standalone HBase. A standalone instance has all
HBase daemons — the Master, RegionServers, and ZooKeeper — running in a single JVM persisting to the local filesystem.
```

### 2、搭建步骤

1、虚拟机中必须安装JDK，JDK的版本建议使用1.8（如果已经安装了，忽略此步骤）

2、在官网中下载HBase对应的安装包，此次课程使用2.0.5版本

3、理论上可以将HBase上传到任意一台虚拟机，但是因为HBase需要zookeeper担任分布式协作服务的角色，而HBase的安装包中包含了zookeeper，而我们在开启虚拟机之后，一般会将高可用的Hadoop集群准备好，因此集群中已经包含zookeeper的服务，**因此，建议将单节点的HBase配置在没有安装zookeeper的节点上**

4、解压hbase安装包

```
tar xzvf hbase-2.0.5-bin.tar.gz -C /opt/bigdata
cd hbase-2.0.5/
```

5、在/etc/profile文件中配置HBase的环境变量

```
export HBASE_HOME=/opt/bigdata/hbase-2.0.5
将$HBASE_HOME设置到PATH路径中
```

6、进入到/opt/bigdata/hbase-2.0.5/conf目录中，在hbase-env.sh文件中添加JAVA_HOME

```
JAVA_HOME=/usr/java/jdk1.8.0_181-amd64
```

7、进入到/opt/bigdata/hbase-2.0.5/conf目录中，在hbase-site.xml文件中添加hbase相关属性

```xml
<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>file:///home/testuser/hbase</value>
  </property>
  <property>
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/home/testuser/zookeeper</value>
  </property>
  <property>
    <name>hbase.unsafe.stream.capability.enforce</name>
    <value>false</value>
    <description>
      Controls whether HBase will check for stream capabilities (hflush/hsync).

      Disable this if you intend to run on LocalFileSystem, denoted by a rootdir
      with the 'file://' scheme, but be mindful of the NOTE below.

      WARNING: Setting this to false blinds you to potential data loss and
      inconsistent system state in the event of process and/or node failures. If
      HBase is complaining of an inability to use hsync or hflush it's most
      likely not a false positive.
    </description>
  </property>
</configuration>
```

8、在任意目录下运行hbase shell的命令，进入hbase的命令行进行相关操作。