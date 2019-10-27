
##  Cluster Manager Types
```
Standalone – a simple cluster manager included with Spark that makes it easy to set up a cluster.
Apache Mesos – a general cluster manager that can also run Hadoop MapReduce and service applications.
Hadoop YARN – the resource manager in Hadoop 2.
Kubernetes – an open-source system for automating deployment, scaling, and management of containerized applications.
```








##  hadoop
yarn-site.xml
```
<property>
	<name>yarn.nodemanager.resource.memory-mb</name>
	<value>4096</value>
</property>
<property>
	<name>yarn.nodemanager.resource.cpu-vcores</name>
	<value>4</value>
</property>
<property>
	<name>yarn.nodemanager.vmem-check-enabled</name>
	<value>false</value>
</property>
```
mapred-site.xml  :node03# mr-jobhistory-daemon.sh start historyserver
```
<property>
	<name>mapred.job.history.server.embedded</name>
	<value>true</value>
</property>
<property>
	<name>mapreduce.jobhistory.address</name>
	<value>node03:10020</value>
</property>
<property>
	<name>mapreduce.jobhistory.webapp.address</name>
	<value>node03:50060</value>
</property>
<property>
	<name>mapreduce.jobhistory.intermediate-done-dir</name>
	<value>/work/mr_history_tmp</value>
</property>
<property>
	<name>mapreduce.jobhistory.done-dir</name>
	<value>/work/mr-history_done</value>
</property>
```

1.  基础设施：

    jdk：1.8.xxx
    
    节点的配置
    
    部署hadoop：hdfs  zookeeper
    
    免密必须做
    
2.  wget spark.tar

    tar xf spark.tar.gz
    
    mv sparkhome /opt/bigdata/

# standalone
    
3.  部署细节：

    vi /etc/profile
    
    $SPARK_HOME/conf
    
    slaves
    ```
    node02
    node03
    node04
    ```
    spark-env.sh
    ```
    export HADOOP_CONF_DIR=/opt/bigdata/hadoop-2.6.5/etc/hadoop
    export SPARK_MASTER_HOST=node01   # 每台master改成自己的主机名
    export SPARK_MASTER_PORT=7077
    export SPARK_MASTER_WEBUI_PORT=8080
    export SPARK_WORKER_CORES=4
    export SPARK_WORKER_MEMORY=4g
    ```
    
    先启动 zookeeper
    
    在启动 hdfs
    
    最后启动 spark  （资源管理层）  master  workers
    
    spark-defaults.conf
    ```
    spark.deploy.recoveryMode       ZOOKEEPER
    spark.deploy.zookeeper.url      node02:2181,node03:2181,node04:2181
    spark.deploy.zookeeper.dir      /msbspark
    
    spark.eventLog.enabled true
    spark.eventLog.dir hdfs://mycluster/spark_log
    spark.history.fs.logDirectory  hdfs://mycluster/spark_log
    ```
    
    a)修改配置一定要分发、重启服务
    
    b)计算层会自己将自己的计算日志存入hdfs
    
    c)手动启动：./sbin/start-history-server.sh
    
    e)访问 启动有  history-server主机的主机名 默认端口是  18080
    
###   提交程序： spark-submit
```
./bin/spark-submit \
  --class <main-class> \
  --master <master-url> \
  --deploy-mode <deploy-mode> \
  --conf <key>=<value> \
  ... # other options
  <application-jar> \
  [application-arguments]
```
  ```
      ../../bin/spark-submit  \--master spark://node01:7077,node02:7077   --class org.apache.spark.examples.SparkPi  ./spark
    -examples_2.11-2.3.4.jar    100000
  
  ```
  
  vi submit.sh
  ```
    class=$1
    jar=$2
    $SPARK_HOME/bin/spark-submit   \
    --master spark://node01:7077,node02:7077 \
    --class $class  \
    $jar \
    1000
  
  ```
  ```
  . submit.sh   'org.apache.spark.examples.SparkPi'  "$SPARK_HOME/examples/jars/spark-examples_2.11-2.3.4.jar"
  ```
  
### 调度

```
--deploy-mode  
--driver-memory  1024m  driver 
    #有能力拉取计算结果回自己的jvm
    #driver、executor jvm中会存储中间计算过程的数据的元数据

--driver-cores  # cluster  mode

--total-executor-cores  #standalone  yarn  每 executor 1  core
--executor-cores 1
--executor-memory  1024m
```
  
  
##  YARN

Spark On Yarn！

Kylin -> 麒麟

### 部署

1.  退掉 spark的  master 、worker
2.  spark on yarn ：不需要 master，worker的配置,rm -fr slaves
3.  只需要启动yarn的角色


####    配置：
spark-env.sh
```
export HADOOP_CONF_DIR=/opt/bigdata/hadoop-2.6.5/etc/hadoop
```
spark-defaults.conf
```
    spark.eventLog.enabled true
    spark.eventLog.dir hdfs://mycluster/spark_log
    spark.history.fs.logDirectory  hdfs://mycluster/spark_log
    spark.yarn.jars  hdfs://mycluster/work/spark_lib/jars/*
```

```
client:ExecutorLauncher  
cluster:ApplicationMaster
spark-shell  只支持  client模式
spark-submit  跑非repl 的可以是client、cluster
```

```
#--total-executor-cores 6 \
#--executor-cores 4 \
class=org.apache.spark.examples.SparkPi

jar=$SPARK_HOME/examples/jars/spark-examples_2.11-2.3.4.jar
#master=spark://node01:7077,node02:7077
master=yarn



$SPARK_HOME/bin/spark-submit   \
--master $master \
--deploy-mode cluster \
--class $class  \
$jar \
100000
```

### 慢~！
```
spark.yarn.jars  hdfs://mycluster/work/spark_lib/jars/*
./spark-shell --master yarn 
hdfs dfs -ls -h /user/root/.sparkStaging/
```

### 调度

```
--driver-memory MEM
--executor-memory MEM

--executor-cores NUM
--num-executors NUM
```















