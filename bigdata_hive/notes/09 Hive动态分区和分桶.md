# Hive动态分区和分桶

### 1、Hive动态分区

##### 		1、hive的动态分区介绍

​		hive的静态分区需要用户在插入数据的时候必须手动指定hive的分区字段值，但是这样的话会导致用户的操作复杂度提高，而且在使用的时候会导致数据只能插入到某一个指定分区，无法让数据散列分布，因此更好的方式是当数据在进行插入的时候，根据数据的某一个字段或某几个字段值动态的将数据插入到不同的目录中，此时，引入动态分区。

##### 		2、hive的动态分区配置

```sql
--hive设置hive动态分区开启
	set hive.exec.dynamic.partition=true;
	默认：true
--hive的动态分区模式
	set hive.exec.dynamic.partition.mode=nostrict;
	默认：strict（至少有一个分区列是静态分区）
--每一个执行mr节点上，允许创建的动态分区的最大数量(100)
	set hive.exec.max.dynamic.partitions.pernode;
--所有执行mr节点上，允许创建的所有动态分区的最大数量(1000)	
	set hive.exec.max.dynamic.partitions;
--所有的mr job允许创建的文件的最大数量(100000)	
	set hive.exec.max.created.files;
```

##### 		3、hive动态分区语法

```sql
--Hive extension (dynamic partition inserts):
	INSERT OVERWRITE TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) 		select_statement FROM from_statement;
	INSERT INTO TABLE tablename PARTITION (partcol1[=val1], partcol2[=val2] ...) 			select_statement FROM from_statement;
```

### 2、Hive分桶

##### 		1、Hive分桶的介绍

```sql
	Bucketed tables are fantastic in that they allow much more efficient sampling than do non-bucketed tables, and they may later allow for time saving operations such as mapside joins. However, the bucketing specified at table creation is not enforced when the table is written to, and so it is possible for the table's metadata to advertise properties which are not upheld by the table's actual layout. This should obviously be avoided. Here's how to do it right.
```

​		注意：

​			1、Hive分桶表是对列值取hash值得方式，将不同数据放到不同文件中存储

​			2、对于hive中每一个表、分区都可以进一步进行分桶

​			3、由列的hash值除以桶的个数来决定每条数据划分在哪个桶中

##### 		2、Hive分桶的配置

```sql
--设置hive支持分桶
	set hive.enforce.bucketing=true;
```

##### 		3、Hive分桶的抽样查询

```sql
--案例
	select * from bucket_table tablesample(bucket 1 out of 4 on columns)
--TABLESAMPLE语法：
	TABLESAMPLE(BUCKET x OUT OF y)
		x：表示从哪个bucket开始抽取数据
		y：必须为该表总bucket数的倍数或因子
```

