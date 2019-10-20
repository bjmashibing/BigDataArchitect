# 压缩和存储

### 1、 Hadoop压缩配置

##### **1)** MR支持的压缩编码

| 压缩格式 | 工具  | 算法    | 文件扩展名 | 是否可切分 |
| -------- | ----- | ------- | ---------- | ---------- |
| DEFAULT  | 无    | DEFAULT | .deflate   | 否         |
| Gzip     | gzip  | DEFAULT | .gz        | 否         |
| bzip2    | bzip2 | bzip2   | .bz2       | 是         |
| LZO      | lzop  | LZO     | .lzo       | 否         |
| LZ4      | 无    | LZ4     | .lz4       | 否         |
| Snappy   | 无    | Snappy  | .snappy    | 否         |

为了支持多种压缩/解压缩算法，Hadoop引入了编码/解码器，如下表所示

| 压缩格式 | 对应的编码/解码器                          |
| -------- | ------------------------------------------ |
| DEFLATE  | org.apache.hadoop.io.compress.DefaultCodec |
| gzip     | org.apache.hadoop.io.compress.GzipCodec    |
| bzip2    | org.apache.hadoop.io.compress.BZip2Codec   |
| LZO      | com.hadoop.compression.lzo.LzopCodec       |
| LZ4      | org.apache.hadoop.io.compress.Lz4Codec     |
| Snappy   | org.apache.hadoop.io.compress.SnappyCodec  |

##### **2)** 压缩配置参数

要在Hadoop中启用压缩，可以配置如下参数（mapred-site.xml文件中）：

| 参数                                              | 默认值                                                       | 阶段        | 建议                                         |
| ------------------------------------------------- | ------------------------------------------------------------ | ----------- | -------------------------------------------- |
| io.compression.codecs   （在core-site.xml中配置） | org.apache.hadoop.io.compress.DefaultCodec, org.apache.hadoop.io.compress.GzipCodec, org.apache.hadoop.io.compress.BZip2Codec,org.apache.hadoop.io.compress.Lz4Codec | 输入压缩    | Hadoop使用文件扩展名判断是否支持某种编解码器 |
| mapreduce.map.output.compress                     | false                                                        | mapper输出  | 这个参数设为true启用压缩                     |
| mapreduce.map.output.compress.codec               | org.apache.hadoop.io.compress.DefaultCodec                   | mapper输出  | 使用LZO、LZ4或snappy编解码器在此阶段压缩数据 |
| mapreduce.output.fileoutputformat.compress        | false                                                        | reducer输出 | 这个参数设为true启用压缩                     |
| mapreduce.output.fileoutputformat.compress.codec  | org.apache.hadoop.io.compress. DefaultCodec                  | reducer输出 | 使用标准工具或者编解码器，如gzip和bzip2      |
| mapreduce.output.fileoutputformat.compress.type   | RECORD                                                       | reducer输出 | SequenceFile输出使用的压缩类型：NONE和BLOCK  |

##### **3)** 开启Map输出阶段压缩

​		开启map输出阶段压缩可以减少job中map和Reduce task间数据传输量。具体配置如下：

​		案例实操：

```sql
--1）开启hive中间传输数据压缩功能
	hive (default)>set hive.exec.compress.intermediate=true;
--2）开启mapreduce中map输出压缩功能
	hive (default)>set mapreduce.map.output.compress=true;
--3）设置mapreduce中map输出数据的压缩方式
	hive (default)>set mapreduce.map.output.compress.codec= org.apache.hadoop.io.compress.SnappyCodec;

--4）执行查询语句
hive (default)> select count(*) from aaaa;
```

##### **4)** 开启Reduce输出阶段压缩

​		当Hive将输出写入到表中时，输出内容同样可以进行压缩。属性hive.exec.compress.output控制着这个功能。用户可能需要保持默认设置文件中的默认值false，这样默认的输出就是非压缩的纯文本文件了。用户可以通过在查询语句或执行脚本中设置这个值为true，来开启输出结果压缩功能。

案例实操：

```sql
--1）开启hive最终输出数据压缩功能
	hive (default)>set hive.exec.compress.output=true;
--2）开启mapreduce最终输出数据压缩
	hive (default)>set mapreduce.output.fileoutputformat.compress=true;
--3）设置mapreduce最终数据输出压缩方式
	hive (default)> set mapreduce.output.fileoutputformat.compress.codec = org.apache.hadoop.io.compress.SnappyCodec;
--4）设置mapreduce最终数据输出压缩为块压缩
	hive (default)> set mapreduce.output.fileoutputformat.compress.type=BLOCK;
--5）测试一下输出结果是否是压缩文件
	hive (default)> insert overwrite local directory '/root/data' select * from aaaa;
```

### 2、文件存储格式

​		Hive支持的存储数的格式主要有：TEXTFILE 、SEQUENCEFILE、ORC、PARQUET。

##### 1) 列式存储和行式存储

![行式存储与列式存储](https://github.com/msbbigdata/hive/blob/master/images/%E8%A1%8C%E5%BC%8F%E5%AD%98%E5%82%A8%E4%B8%8E%E5%88%97%E5%BC%8F%E5%AD%98%E5%82%A8.png)

上图左边为逻辑表，右边第一个为行式存储，第二个为列式存储。

**行存储的特点：** 查询满足条件的一整行数据的时候，列存储则需要去每个聚集的字段找到对应的每个列的值，行存储只需要找到其中一个值，其余的值都在相邻地方，所以此时行存储查询的速度更快。

**列存储的特点：** 因为每个字段的数据聚集存储，在查询只需要少数几个字段的时候，能大大减少读取的数据量；每个字段的数据类型一定是相同的，列式存储可以针对性的设计更好的设计压缩算法。

TEXTFILE和SEQUENCEFILE的存储格式都是基于行存储的；

ORC和PARQUET是基于列式存储的。

##### **2)** **TEXTFILE格式**

默认格式，数据不做压缩，磁盘开销大，数据解析开销大。可结合Gzip、Bzip2使用(系统自动检查，执行查询时自动解压)，但使用这种方式，hive不会对数据进行切分，从而无法对数据进行并行操作。

##### **3)** ORC格式

​		Orc (Optimized Row Columnar)是hive 0.11版里引入的新的存储格式。

​		可以看到每个Orc文件由1个或多个stripe组成，每个stripe250MB大小，这个Stripe实际相当于RowGroup概念，不过大小由4MB->250MB，这样应该能提升顺序读的吞吐率。每个Stripe里有三部分组成，分别是Index Data,Row Data,Stripe Footer：

![orc文件格式](https://github.com/msbbigdata/hive/blob/master/images/orc%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F.png)

   		1）Index Data：一个轻量级的index，默认是每隔1W行做一个索引。这里做的索引应该只是记录某行的各字段在Row Data中的offset。

   		2）Row Data：存的是具体的数据，先取部分行，然后对这些行按列进行存储。对每个列进行了编码，分成多个Stream来存储。

​    	   3）Stripe Footer：存的是各个Stream的类型，长度等信息。

​		每个文件有一个File Footer，这里面存的是每个Stripe的行数，每个Column的数据类型信息等；每个文件的尾部是一个PostScript，这里面记录了整个文件的压缩类型以及FileFooter的长度信息等。在读取文件时，会seek到文件尾部读PostScript，从里面解析到File Footer长度，再读FileFooter，从里面解析到各个Stripe信息，再读各个Stripe，即从后往前读。

##### **4)** PARQUET格式

​		Parquet是面向分析型业务的列式存储格式，由Twitter和Cloudera合作开发，2015年5月从Apache的孵化器里毕业成为Apache顶级项目。

​		Parquet文件是以二进制方式存储的，所以是不可以直接读取的，文件中包括该文件的数据和元数据，因此Parquet格式文件是自解析的。

​		通常情况下，在存储Parquet数据的时候会按照Block大小设置行组的大小，由于一般情况下每一个Mapper任务处理数据的最小单位是一个Block，这样可以把每一个行组由一个Mapper任务处理，增大任务执行并行度。Parquet文件的格式如下图所示。

![parquet文件格式](https://github.com/msbbigdata/hive/blob/master/images/parquet%E6%96%87%E4%BB%B6%E6%A0%BC%E5%BC%8F.png)

​		上图展示了一个Parquet文件的内容，一个文件中可以存储多个行组，文件的首位都是该文件的Magic Code，用于校验它是否是一个Parquet文件，Footer length记录了文件元数据的大小，通过该值和文件长度可以计算出元数据的偏移量，文件的元数据中包括每一个行组的元数据信息和该文件存储数据的Schema信息。除了文件中每一个行组的元数据，每一页的开始都会存储该页的元数据，在Parquet中，有三种类型的页：数据页、字典页和索引页。数据页用于存储当前行组中该列的值，字典页存储该列值的编码字典，每一个列块中最多包含一个字典页，索引页用来存储当前行组下该列的索引，目前Parquet中还不支持索引页。

##### **5)** 主流文件存储格式对比实验

​		从存储文件的压缩比和查询速度两个角度对比。

​		存储文件的压缩比测试：

```sql
--1）TextFile
--（1）创建表，存储数据格式为TEXTFILE
	create table log_text (track_time string,url string,session_id string,referer string,ip string,end_user_id string,city_id string)row format delimited fields terminated by '\t'stored as textfile ;
--（2）向表中加载数据
	hive (default)> load data local inpath '/root/log' into table log_text ;
--（3）查看表中数据大小
	dfs -du -h /user/hive/warehouse/log_text;
	18.1 M  /user/hive/warehouse/log_text/log.data
--2）ORC
--（1）创建表，存储数据格式为ORC
	create table log_orc(track_time string,url string,session_id string,referer string,ip string,end_user_id string,city_id string)row format delimited fields terminated by '\t'stored as orc ;
--（2）向表中加载数据
	insert into table log_orc select * from log_text ;
--（3）查看表中数据大小
	dfs -du -h /user/hive/warehouse/log_orc/ ;
	2.8 M  /user/hive/warehouse/log_orc/000000_0
--3）Parquet
--（1）创建表，存储数据格式为parquet
	create table log_parquet(track_time string,url string,session_id string,referer string,ip string,end_user_id string,city_id string)row format delimited fields terminated by '\t'stored as parquet ;	
--（2）向表中加载数据
	insert into table log_parquet select * from log_text ;
--（3）查看表中数据大小
	dfs -du -h /user/hive/warehouse/log_parquet/ ;
	13.1 M  /user/hive/warehouse/log_parquet/000000_0
--存储文件的压缩比总结：
	ORC >  Parquet >  textFile
```

### 3、存储和压缩结合

​		官网：https://cwiki.apache.org/confluence/display/Hive/LanguageManual+ORC

​		ORC存储方式的压缩：

| Key                      | Default    | Notes                                                        |
| ------------------------ | ---------- | ------------------------------------------------------------ |
| orc.compress             | ZLIB       | high level compression (one of NONE, ZLIB, SNAPPY)           |
| orc.compress.size        | 262,144    | number of bytes in each compression chunk                    |
| orc.stripe.size          | 67,108,864 | number of bytes in each stripe                               |
| orc.row.index.stride     | 10,000     | number of rows between index entries (must be >= 1000)       |
| orc.create.index         | true       | whether to create row indexes                                |
| orc.bloom.filter.columns | ""         | comma separated list of column names for which bloom filter should be created |
| orc.bloom.filter.fpp     | 0.05       | false positive probability for bloom filter (must >0.0 and <1.0) |

```sql
--1）创建一个非压缩的的ORC存储方式
--（1）建表语句
	create table log_orc_none(track_time string,url string,session_id string,referer string,ip string,end_user_id string,city_id string)row format delimited fields terminated by '\t'stored as orc tblproperties ("orc.compress"="NONE");
--（2）插入数据
	insert into table log_orc_none select * from log_text ;
--（3）查看插入后数据
	dfs -du -h /user/hive/warehouse/log_orc_none/ ;
	7.7 M  /user/hive/warehouse/log_orc_none/000000_0
--2）创建一个SNAPPY压缩的ORC存储方式
--（1）建表语句
	create table log_orc_snappy(track_time string,url string,session_id string,referer string,ip string,end_user_id string,city_id string)row format delimited fields terminated by '\t'stored as orc tblproperties ("orc.compress"="SNAPPY");
--（2）插入数据
	insert into table log_orc_snappy select * from log_text ;
--（3）查看插入后数据
	dfs -du -h /user/hive/warehouse/log_orc_snappy/ ;
	3.8 M  /user/hive/warehouse/log_orc_snappy/000000_0
--3）上一节中默认创建的ORC存储方式，导入数据后的大小为
	2.8 M  /user/hive/warehouse/log_orc/000000_0
--总结
	比Snappy压缩的还小。原因是orc存储文件默认采用ZLIB压缩。比snappy压缩的小。
```

 

 