# HBase基本操作

### 1、HBase Shell操作

##### 1、通用命令

```java
//展示regionserver的task列表
hbase(main):000:0>processlist
//展示集群的状态
hbase(main):000:0>status
//table命令的帮助手册
hbase(main):000:0>table_help
//显示hbase的版本
hbase(main):000:0>version
//展示当前hbase的用户
hbase(main):000:0>whoami
```

##### 2、DDL操作

```java
//修改表的属性
hbase(main):000:0>alter 't1', NAME => 'f1', VERSIONS => 5
//创建表
hbase(main):000:0>create 'test', 'cf'
//查看表描述，只会展示列族的详细信息
hbase(main):000:0>describe 'test'
//禁用表
hbase(main):000:0>disable 'test'
//禁用所有表
hbase(main):000:0>disable_all
//删除表
hbase(main):000:0>drop 'test'
//删除所有表
hbase(main):000:0>drop_all
//启用表
hbase(main):000:0>enable 'test'
//启用所有表
hbase(main):000:0>enable_all
//判断表是否存在
hbase(main):000:0>exists 'test'
//获取表
hbase(main):000:0>get_table 'test'
//判断表是否被禁用
hbase(main):000:0>is_disabled 'test'
//判断表是否被启用
hbase(main):000:0>is_enabled 'test'
//展示所有表
hbase(main):000:0>list
//展示表占用的region
hbase(main):000:0>list_regions
//定位某个rowkey所在的行在哪一个region
hbase(main):000:0>locate_region
//展示所有的过滤器
hbase(main):000:0>show_filters
```

##### 3、namespace操作

```java
//修改命名空间的属性
hbase(main):000:0>alter_namespace 'my_ns', {METHOD => 'set', 'PROPERTY_NAME' => 'PROPERTY_VALUE'}
//创建命名空间
hbase(main):000:0>create_namespace 'my_ns'
//获取命名空间的描述信息
hbase(main):000:0>describe_namespace 'my_ns'
//删除命名空间
hbase(main):000:0>drop_namespace 'my_ns'
//展示所有的命名空间
hbase(main):000:0>list_namespace
//展示某个命名空间下的所有表
hbase(main):000:0>list_namespace_tables 'my_ns'
```

##### 4、dml操作

```java
//向表中追加一个具体的值
hbase(main):000:0>append 't1', 'r1', 'c1', 'value', ATTRIBUTES=>{'mykey'=>'myvalue'}
//统计表的记录条数，默认一千条输出一次
hbase(main):000:0>count 'test'
//删除表的某一个值
hbase(main):000:0>delete 't1', 'r1', 'c1', ts1
//删除表的某一个列的所有值
hbase(main):000:0>deleteall 't1', 'r1', 'c1'
//获取表的一行记录
hbase(main):000:0>get 't1', 'r1'
//获取表的一个列的值的个数
hbase(main):000:0>get_counter 't1', 'r1', 'c1'
//获取表的切片
hbase(main):000:0>get_splits 't1'
//增加一个cell对象的值
hbase(main):000:0>incr 't1', 'r1', 'c1'
//向表中的某一个列插入值
hbase(main):000:0>put 't1', 'r1', 'c1', 'value’, ts1
//扫描表的全部数据
hbase(main):000:0>scan 't1'
//清空表的所有数据
hbase(main):000:0>truncate
```

