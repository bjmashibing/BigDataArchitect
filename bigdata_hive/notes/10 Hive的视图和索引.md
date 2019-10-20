# Hive的视图和索引

### 1、Hive Lateral View

##### 	1、基本介绍

​		Lateral View用于和UDTF函数（explode、split）结合来使用。
​		首先通过UDTF函数拆分成多行，再将多行结果组合成一个支持别名的虚拟表。主要解决在select使用UDTF做查询过程中，查询只能包含单个UDTF，不能包含其他字段、以及多个UDTF的问题。
​		语法：
​			LATERAL VIEW udtf(expression) tableAlias AS columnAlias (',' columnAlias)

##### 	2、案例

```sql
select count(distinct(myCol1)), count(distinct(myCol2)) from psn2 
LATERAL VIEW explode(likes) myTable1 AS myCol1 
LATERAL VIEW explode(address) myTable2 AS myCol2, myCol3;
```

### 2、Hive视图

##### 	1、Hive视图基本介绍

​		Hive 中的视图和RDBMS中视图的概念一致，都是一组数据的逻辑表示，本质上就是一条SELECT语句的结果集。视图是纯粹的逻辑对象，没有关联的存储(Hive 3.0.0引入的物化视图除外)，当查询引用视图时，Hive可以将视图的定义与查询结合起来，例如将查询中的过滤器推送到视图中。

##### 	2、Hive视图特点

​		1、不支持物化视图
​		2、只能查询，不能做加载数据操作
​		3、视图的创建，只是保存一份元数据，查询视图时才执行对应的子查询
​		4、view定义中若包含了ORDER BY/LIMIT语句，当查询视图时也进行ORDER BY/LIMIT语句操作，view当中			  定义的优先级更高
​		5、view支持迭代视图

##### 	3、Hive视图语法

```sql
--创建视图：
	CREATE VIEW [IF NOT EXISTS] [db_name.]view_name 
	  [(column_name [COMMENT column_comment], ...) ]
	  [COMMENT view_comment]
	  [TBLPROPERTIES (property_name = property_value, ...)]
	  AS SELECT ... ;
--查询视图：
	select colums from view;
--删除视图：
	DROP VIEW [IF EXISTS] [db_name.]view_name;
```

### 3、Hive索引

##### 	1、hive索引

​		为了提高数据的检索效率，可以使用hive的索引

##### 	2、hive基本操作

```sql
--创建索引：
	create index t1_index on table psn2(name) 
	as 'org.apache.hadoop.hive.ql.index.compact.CompactIndexHandler' with deferred 			rebuild in table t1_index_table;
--as：指定索引器；
--in table：指定索引表，若不指定默认生成在default__psn2_t1_index__表中
	create index t1_index on table psn2(name) 
	as 'org.apache.hadoop.hive.ql.index.compact.CompactIndexHandler' with deferred 			rebuild;
--查询索引
	show index on psn2;
--重建索引（建立索引之后必须重建索引才能生效）
	ALTER INDEX t1_index ON psn2 REBUILD;
--删除索引
	DROP INDEX IF EXISTS t1_index ON psn2;
```

