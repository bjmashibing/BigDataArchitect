package com.mashibing.transformer.mr;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.mashibing.common.EventLogConstants;
import com.mashibing.common.GlobalConstants;
import com.mashibing.util.TimeUtil;

/**
 * 所有transformer相关mr程序入口类的公用父类
 * 
 * @author 马士兵教育
 *
 */
public abstract class TransformerBaseRunner implements Tool {
    private static final Logger logger = Logger.getLogger(TransformerBaseRunner.class);
    protected Configuration conf;
    private String jobName;
    private Class<?> runnerClass;
    private Class<? extends TableMapper<?, ?>> mapperClass;
    private Class<? extends Reducer<?, ?, ?, ?>> reducerClass;
    private Class<? extends OutputFormat<?, ?>> outputFormatClass;
    private Class<? extends WritableComparable<?>> mapOutputKeyClass;
    private Class<? extends Writable> mapOutputValueClass;
    private Class<?> outputKeyClass;
    private Class<?> outputValueClass;
    private long startTime;
    private boolean isCallSetUpRunnerMethod = false;

    /**
     * 设置job参数
     * 
     * @param jobName
     *            job名称
     * @param runnerClass
     *            runne class
     * @param mapperClass
     *            mapper的class
     * @param reducerClass
     *            reducer的class
     * @param outputKeyClass
     *            输出key
     * @param outputValueClass
     *            输出value
     */
    public void setupRunner(String jobName, Class<?> runnerClass, Class<? extends TableMapper<?, ?>> mapperClass, Class<? extends Reducer<?, ?, ?, ?>> reducerClass, Class<? extends WritableComparable<?>> outputKeyClass, Class<? extends Writable> outputValueClass) {
        this.setupRunner(jobName, runnerClass, mapperClass, reducerClass, outputKeyClass, outputValueClass, outputKeyClass, outputValueClass, TransformerOutputFormat.class);
    }

    /**
     * 设置参数
     * 
     * @param jobName
     * @param runnerClass
     * @param mapperClass
     * @param reducerClass
     * @param outputKeyClass
     * @param outputValueClass
     * @param outputFormatClass
     */
    public void setupRunner(String jobName, Class<?> runnerClass, Class<? extends TableMapper<?, ?>> mapperClass, Class<? extends Reducer<?, ?, ?, ?>> reducerClass, Class<? extends WritableComparable<?>> outputKeyClass, Class<? extends Writable> outputValueClass, Class<? extends OutputFormat<?, ?>> outputFormatClass) {
        this.setupRunner(jobName, runnerClass, mapperClass, reducerClass, outputKeyClass, outputValueClass, outputKeyClass, outputValueClass, outputFormatClass);
    }

    /**
     * 设置参数
     * 
     * @param jobName
     * @param runnerClass
     * @param mapperClass
     * @param reducerClass
     * @param mapOutputKeyClass
     * @param mapOutputValueClass
     * @param outputKeyClass
     * @param outputValueClass
     */
    public void setupRunner(String jobName, Class<?> runnerClass, Class<? extends TableMapper<?, ?>> mapperClass, Class<? extends Reducer<?, ?, ?, ?>> reducerClass, Class<? extends WritableComparable<?>> mapOutputKeyClass, Class<? extends Writable> mapOutputValueClass, Class<? extends WritableComparable<?>> outputKeyClass, Class<? extends Writable> outputValueClass) {
        this.setupRunner(jobName, runnerClass, mapperClass, reducerClass, mapOutputKeyClass, mapOutputValueClass, outputKeyClass, outputValueClass, TransformerOutputFormat.class);
    }

    /**
     * 具体设置参数
     * 
     * @param jobName
     * @param runnerClass
     * @param mapperClass
     * @param reducerClass
     * @param mapOutputKeyClass
     * @param mapOutputValueClass
     * @param outputKeyClass
     * @param outputValueClass
     * @param outputFormatClass
     */
    public void setupRunner(String jobName, Class<?> runnerClass, Class<? extends TableMapper<?, ?>> mapperClass, Class<? extends Reducer<?, ?, ?, ?>> reducerClass, Class<? extends WritableComparable<?>> mapOutputKeyClass, Class<? extends Writable> mapOutputValueClass, Class<? extends WritableComparable<?>> outputKeyClass, Class<? extends Writable> outputValueClass,
            Class<? extends OutputFormat<?, ?>> outputFormatClass) {
        this.jobName = jobName;
        this.runnerClass = runnerClass;
        this.mapperClass = mapperClass;
        this.reducerClass = reducerClass;
        this.mapOutputKeyClass = mapOutputKeyClass;
        this.mapOutputValueClass = mapOutputValueClass;
        this.outputKeyClass = outputKeyClass;
        this.outputValueClass = outputValueClass;
        this.outputFormatClass = outputFormatClass;
        this.isCallSetUpRunnerMethod = true;
    }

    /**
     * 代码执行函数
     * 
     * @throws Exception
     */
    public void startRunner(String[] args) throws Exception {
        ToolRunner.run(new Configuration(), this, args);
    }

    @Override
    public void setConf(Configuration conf) {
        // Configuration的执行顺序是：按照resource的添加(add)顺序添加的，后面添加的会覆盖前面添加的。
        // 但是有一点需要注意，就是如果某一个值已经在内存中了(从文件中读入到内存), 那么此时在今天添加文件操作，不会产生覆盖效果。
        // 假设: a.xml文件中有一对key/value是fs.defaultFS=file:///;
        // b.xml文件中有一对key/value是fs.defaultFS=hdfs://hh:8020:
        // 执行顺序，1. 添加a.xml文件；2. 获取fs.defaultFS值；3.添加b.xml文件; 4. 获取fs.defaultFs的值
        // 结果: 2和4都是返回的是file:///

        // 添加自定义的配置文件
        conf.addResource("transformer-env.xml");
        conf.addResource("query-mapping.xml");
        conf.addResource("output-collector.xml");
        // 创建hbase相关的config对象(包含hbase配置文件)
        // hbase创建config的时候，会将指定参数的configuration所有的内容加载到内存中。
        this.conf = HBaseConfiguration.create(conf);
        // 调用设置自定义的函数
        this.configure();
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public int run(String[] args) throws Exception {
        if (!this.isCallSetUpRunnerMethod) {
            throw new RuntimeException("必须调用setupRunner方法进行参数设置");
        }
        Configuration conf = this.getConf(); // 获取configuration对象
        // 初始化参数
        this.processArgs(conf, args);
        Job job = this.initJob(conf); // 创建job
        // 执行job
        this.beforeRunJob(job); // 在job执行前运行
        Throwable error = null;
        try {
            this.startTime = System.currentTimeMillis();
            return job.waitForCompletion(true) ? 0 : -1;
        } catch (Throwable e) {
            error = e;
            logger.error("执行" + this.jobName + "job出现异常", e);
            throw new RuntimeException(e);
        } finally {
            this.afterRunJob(job, error); // 在代码执行后运行
        }
    }

    /**
     * 创建job
     * 
     * @param conf
     * @return
     * @throws IOException
     */
    protected Job initJob(Configuration conf) throws IOException {
        Job job = Job.getInstance(conf, this.jobName);

        job.setJarByClass(this.runnerClass);
        // 本地运行
        TableMapReduceUtil.initTableMapperJob(initScans(job), this.mapperClass, this.mapOutputKeyClass, this.mapOutputValueClass, job, false);
        // 集群运行：本地提交和打包(jar)提交
        // TableMapReduceUtil.initTableMapperJob(initScans(job),
        // this.mapperClass, this.mapOutputKeyClass, this.mapOutputValueClass,
        // job);
        job.setReducerClass(this.reducerClass);
        job.setOutputKeyClass(this.outputKeyClass);
        job.setOutputValueClass(this.outputValueClass);
        job.setOutputFormatClass(this.outputFormatClass);
        return job;
    }

    /**
     * 在执行job前运行该方法
     * 
     * @param job
     * @throws IOException
     */
    protected void beforeRunJob(Job job) throws IOException {
        // nothing
    }

    /**
     * 在执行后运行该方法
     * 
     * @param job
     *            job对象
     * @param error
     *            job运行期间产生的异常信息，如果没有产生异常，那么传入null。
     * @throws IOException
     */
    protected void afterRunJob(Job job, Throwable error) throws IOException {
        try {
            // 结束的毫秒数
            long endTime = System.currentTimeMillis();
            logger.info("Job<" + this.jobName + ">是否执行成功:" + (error == null ? job.isSuccessful() : "false") + "; 开始时间:" + startTime + "; 结束时间:" + endTime + "; 用时:" + (endTime - startTime) + "ms" + (error == null ? "" : "; 异常信息为:" + error.getMessage()));
        } catch (Throwable e) {
            // nothing
        }
    }

    /**
     * 将指定的配置文件信息添加到config中去。
     * 
     * @param resourceFiles
     */
    protected void configure(String... resourceFiles) {
        if (this.conf == null) {
            this.conf = HBaseConfiguration.create();
        }
        // 开始添加指定的资源文件
        if (resourceFiles != null) {
            for (String resource : resourceFiles) {
                this.conf.addResource(resource);
            }
        }
    }

    /**
     * 处理参数
     * 
     * @param conf
     * @param args
     */
    protected void processArgs(Configuration conf, String[] args) {
        String date = null;
        for (int i = 0; i < args.length; i++) {
            if ("-d".equals(args[i])) {
                if (i + 1 < args.length) {
                    date = args[++i];
                    break;
                }
            }
        }

        // 要求date格式为: yyyy-MM-dd
        if (StringUtils.isBlank(date) || !TimeUtil.isValidateRunningDate(date)) {
            // date是一个无效时间数据
            date = TimeUtil.getYesterday(); // 默认时间是昨天
        }
        conf.set(GlobalConstants.RUNNING_DATE_PARAMES, date);
    }

    /**
     * 初始化scan集合
     * 
     * @param job
     * @return
     */
    protected List<Scan> initScans(Job job) {
        Configuration conf = job.getConfiguration();
        // 获取运行时间: yyyy-MM-dd
        String date = conf.get(GlobalConstants.RUNNING_DATE_PARAMES);
        long startDate = TimeUtil.parseString2Long(date);
        long endDate = startDate + GlobalConstants.DAY_OF_MILLISECONDS;

        Scan scan = new Scan();
        // 定义hbase扫描的开始rowkey和结束rowkey
        scan.setStartRow(Bytes.toBytes("" + startDate));
        scan.setStopRow(Bytes.toBytes("" + endDate));

        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(EventLogConstants.HBASE_NAME_EVENT_LOGS));
        Filter filter = this.fetchHbaseFilter();
        if (filter != null) {
            scan.setFilter(filter);
        }

        // 优化设置cache
        scan.setBatch(500);
        scan.setCacheBlocks(true); // 启动cache blocks
        scan.setCaching(1000); // 设置每次返回的行数，默认值100，设置较大的值可以提高速度(减少rpc操作)，但是较大的值可能会导致内存异常。
        return Lists.newArrayList(scan);
    }

    /**
     * 获取hbase操作的过滤filter对象
     * 
     * @return
     */
    protected Filter fetchHbaseFilter() {
        return null;
    }

    /**
     * 获取这个列名过滤的column
     * 
     * @param columns
     * @return
     */
    protected Filter getColumnFilter(String[] columns) {
        int length = columns.length;
        byte[][] filter = new byte[length][];
        for (int i = 0; i < length; i++) {
            filter[i] = Bytes.toBytes(columns[i]);
        }
        return new MultipleColumnPrefixFilter(filter);
    }
}
