package com.mashibing.transformer.mr.au;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.mashibing.common.EventLogConstants;
import com.mashibing.common.GlobalConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.TransformerOutputFormat;
import com.mashibing.util.TimeUtil;

/**
 * 统计active user的入口类
 * 
 * @author 马士兵教育
 *
 */
public class ActiveUserRunner implements Tool {
    private static final Logger logger = Logger.getLogger(ActiveUserRunner.class);
    private Configuration conf = null;

    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(), new ActiveUserRunner(), args);
        } catch (Exception e) {
            logger.error("运行active user任务出现异常", e);
            throw new RuntimeException(e);
        }
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
    	conf.set("fs.defaultFS", "hdfs://node1:8020");
//     	conf.set("yarn.resourcemanager.hostname", "node3");
     	conf.set("hbase.zookeeper.quorum", "node4");
        conf.addResource("transformer-env.xml");
        conf.addResource("query-mapping.xml");
        conf.addResource("output-collector.xml");
        // 创建hbase相关的config对象(包含hbase配置文件)
        // hbase创建config的时候，会将指定参数的configuration所有的内容加载到内存中。
        this.conf = HBaseConfiguration.create(conf);
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        // 初始化参数
        this.processArgs(conf, args);

        // 创建job
        Job job = Job.getInstance(conf, "active_user");
        

        // 设置job相关配置参数
        job.setJarByClass(ActiveUserRunner.class);
        // hbase 输入mapper参数
        // 1. 本地运行
        TableMapReduceUtil.initTableMapperJob(this.initScans(job), ActiveUserMapper.class, StatsUserDimension.class, TimeOutputValue.class, job, false);
        // 2. 集群运行
        // TableMapReduceUtil.initTableMapperJob(null, ActiveUserMapper.class,
        // StatsUserDimension.class, TimeOutputValue.class, job);

        // 设置reducer相关参数
        job.setReducerClass(ActiveUserReducer.class);
        job.setOutputKeyClass(StatsUserDimension.class);
        job.setOutputValueClass(MapWritableValue.class);

        // 设置output相关参数
        job.setOutputFormatClass(TransformerOutputFormat.class);
        // 开始毫秒数
        long startTime = System.currentTimeMillis();
        try {
            return job.waitForCompletion(true) ? 0 : -1;
        } finally {
            // 结束的毫秒数
            long endTime = System.currentTimeMillis();
            logger.info("Job<" + job.getJobName() + ">是否执行成功:" + job.isSuccessful() + "; 开始时间:" + startTime + "; 结束时间:" + endTime + "; 用时:" + (endTime - startTime) + "ms");
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
    private List<Scan> initScans(Job job) {
        Configuration conf = job.getConfiguration();
        // 获取运行时间: yyyy-MM-dd
        String date = conf.get(GlobalConstants.RUNNING_DATE_PARAMES);
        long startDate = TimeUtil.parseString2Long(date);
        long endDate = startDate + GlobalConstants.DAY_OF_MILLISECONDS;

        Scan scan = new Scan();
        // 定义hbase扫描的开始rowkey和结束rowkey
        scan.setStartRow(Bytes.toBytes("" + startDate));
        scan.setStopRow(Bytes.toBytes("" + endDate));

        FilterList filterList = new FilterList();
        // 定义mapper中需要获取的列名
        String[] columns = new String[] { 
        		EventLogConstants.LOG_COLUMN_NAME_UUID, // 用户id
                EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, // 服务器时间
                EventLogConstants.LOG_COLUMN_NAME_PLATFORM, // 平台名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, // 浏览器名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION // 浏览器版本号
        };
        filterList.addFilter(this.getColumnFilter(columns));

        scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(EventLogConstants.HBASE_NAME_EVENT_LOGS));
        scan.setFilter(filterList);
        return Lists.newArrayList(scan);
    }

    /**
     * 获取这个列名过滤的column
     * 
     * @param columns
     * @return
     */
    private Filter getColumnFilter(String[] columns) {
        int length = columns.length;
        byte[][] filter = new byte[length][];
        for (int i = 0; i < length; i++) {
            filter[i] = Bytes.toBytes(columns[i]);
        }
        return new MultipleColumnPrefixFilter(filter);
    }
}
