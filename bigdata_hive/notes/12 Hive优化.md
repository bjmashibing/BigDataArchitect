# Hive优化

​		Hive的存储层依托于HDFS，Hive的计算层依托于MapReduce，一般Hive的执行效率主要取决于SQL语句的执行效率，因此，Hive的优化的核心思想是MapReduce的优化。

### 1、查看Hive执行计划（小白慎用）

​		Hive的SQL语句在执行之前需要将SQL语句转换成MapReduce任务，因此需要了解具体的转换过程，可以在SQL语句中输入如下命令查看具体的执行计划。

```sql
--查看执行计划，添加extended关键字可以查看更加详细的执行计划
explain [extended] query
```

### 2、Hive的抓取策略

​		Hive的某些SQL语句需要转换成MapReduce的操作，某些SQL语句就不需要转换成MapReduce操作，但是同学们需要注意，理论上来说，所有的SQL语句都需要转换成MapReduce操作，只不过Hive在转换SQL语句的过程中会做部分优化，使某些简单的操作不再需要转换成MapReduce，例如：

​		（1）select 仅支持本表字段

​		（2）where仅对本表字段做条件过滤

```sql
--查看Hive的数据抓取策略
Set hive.fetch.task.conversion=none/more;
```

### 3、Hive本地模式

​		类似于MapReduce的操作，Hive的运行也分为本地模式和集群模式，在开发阶段可以选择使用本地执行，提高SQL语句的执行效率，验证SQL语句是否正确。

```sql
--设置本地模式
set hive.exec.mode.local.auto=true;
```

​		注意：要想使用Hive的本地模式，加载数据文件大小不能超过128M,如果超过128M,就算设置了本地模式，也会按照集群模式运行。

```sql
--设置读取数据量的大小限制
set hive.exec.mode.local.auto.inputbytes.max=128M
```

### 4、Hive并行模式

​		在SQL语句足够复杂的情况下，可能在一个SQL语句中包含多个子查询语句，且多个子查询语句之间没有任何依赖关系，此时，可以Hive运行的并行度

```sql
--设置Hive SQL的并行度
set hive.exec.parallel=true;
```

​		注意：Hive的并行度并不是无限增加的，在一次SQL计算中，可以通过以下参数来设置并行的job的个数

```sql
--设置一次SQL计算允许并行执行的job个数的最大值
set hive.exec.parallel.thread.number
```

### 5、Hive严格模式

​		Hive中为了提高SQL语句的执行效率，可以设置严格模式，充分利用Hive的某些特点。

```sql
-- 设置Hive的严格模式
set hive.mapred.mode=strict;
```

​		注意：当设置严格模式之后，会有如下限制：

​				（1）对于分区表，必须添加where对于分区字段的条件过滤

​				（2）order by语句必须包含limit输出限制

​				（3）限制执行笛卡尔积的查询

### 6、Hive排序

​		在编写SQL语句的过程中，很多情况下需要对数据进行排序操作，Hive中支持多种排序操作适合不同的应用场景。

​		1、Order By - 对于查询结果做全排序，只允许有一个reduce处理
​			（当数据量较大时，应慎用。严格模式下，必须结合limit来使用）
​		2、Sort By - 对于单个reduce的数据进行排序
​		3、Distribute By - 分区排序，经常和Sort By结合使用
​		4、Cluster By - 相当于 Sort By + Distribute By
​			（Cluster By不能通过asc、desc的方式指定排序规则；
​				可通过 distribute by column sort by column asc|desc 的方式）

### 7、Hive join

​		1、Hive 在多个表的join操作时尽可能多的使用相同的连接键，这样在转换MR任务时会转换成少的MR的任务。

​		2、手动Map join:在map端完成join操作

```sql
--SQL方式，在SQL语句中添加MapJoin标记（mapjoin hint）
SELECT  /*+ MAPJOIN(smallTable) */  smallTable.key,  bigTable.value 
FROM  smallTable  JOIN  bigTable  ON  smallTable.key  =  bigTable.key;
```

​		3、开启自动的Map Join

```sql
--通过修改以下配置启用自动的mapjoin：
set hive.auto.convert.join = true;
--（该参数为true时，Hive自动对左边的表统计量，如果是小表就加入内存，即对小表使用Map join）
--相关配置参数：
hive.mapjoin.smalltable.filesize;  
--（大表小表判断的阈值，如果表的大小小于该值则会被加载到内存中运行）
hive.ignore.mapjoin.hint；
--（默认值：true；是否忽略mapjoin hint 即mapjoin标记）
```

​		4、大表join大表

​		（1）空key过滤：有时join超时是因为某些key对应的数据太多，而相同key对应的数据都会发送到相同的reducer上，从而导致内存不够。此时我们应该仔细分析这些异常的key，很多情况下，这些key对应的数据是异常数据，我们需要在SQL语句中进行过滤。
​		（2）空key转换：有时虽然某个key为空对应的数据很多，但是相应的数据不是异常数据，必须要包含在join的结果中，此时我们可以表a中key为空的字段赋一个随机的值，使得数据随机均匀地分不到不同的reducer上

### 8、Map-Side聚合	

​		Hive的某些SQL操作可以实现map端的聚合，类似于MR的combine操作

```sql
--通过设置以下参数开启在Map端的聚合：
set hive.map.aggr=true;
--相关配置参数：
--map端group by执行聚合时处理的多少行数据（默认：100000）
hive.groupby.mapaggr.checkinterval： 
--进行聚合的最小比例（预先对100000条数据做聚合，若聚合之后的数据量/100000的值大于该配置0.5，则不会聚合）
hive.map.aggr.hash.min.reduction： 
--map端聚合使用的内存的最大值
hive.map.aggr.hash.percentmemory： 
--是否对GroupBy产生的数据倾斜做优化，默认为false
hive.groupby.skewindata
```

### 9、合并小文件

​		Hive在操作的时候，如果文件数目小，容易在文件存储端造成压力，给hdfs造成压力，影响效率

```sql
--设置合并属性
--是否合并map输出文件：
set hive.merge.mapfiles=true
--是否合并reduce输出文件：
set hive.merge.mapredfiles=true;
--合并文件的大小：
set hive.merge.size.per.task=256*1000*1000
```

### 10、合理设置Map以及Reduce的数量

```sql
--Map数量相关的参数
--一个split的最大值，即每个map处理文件的最大值
set mapred.max.split.size
--一个节点上split的最小值
set mapred.min.split.size.per.node
--一个机架上split的最小值
set mapred.min.split.size.per.rack
--Reduce数量相关的参数
--强制指定reduce任务的数量
set mapred.reduce.tasks
--每个reduce任务处理的数据量
set hive.exec.reducers.bytes.per.reducer
--每个任务最大的reduce数
set hive.exec.reducers.max
```

### 11、JVM重用

```sql
/*
适用场景：
	1、小文件个数过多
	2、task个数过多
缺点：
	设置开启之后，task插槽会一直占用资源，不论是否有task运行，直到所有的task即整个job全部执行完成时，才会释放所有的task插槽资源！
*/
set mapred.job.reuse.jvm.num.tasks=n;--（n为task插槽个数）

```

