package com.mashibing.transformer.mr.nu;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.mashibing.common.DateEnum;
import com.mashibing.common.EventLogConstants;
import com.mashibing.common.EventLogConstants.EventEnum;
import com.mashibing.common.GlobalConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.TransformerOutputFormat;
import com.mashibing.util.JdbcManager;
import com.mashibing.util.TimeUtil;

/**
 * 计算新增用户入口类
 * 
 * @author 马士兵教育
 *
 */
public class NewInstallUserRunner implements Tool {
    private static final Logger logger = Logger.getLogger(NewInstallUserRunner.class);
    private Configuration conf = new Configuration();

    /**
     * 入口main方法
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            ToolRunner.run(new Configuration(true), new NewInstallUserRunner(), args);
        } catch (Exception e) {
            logger.error("运行计算新用户的job出现异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setConf(Configuration conf) {
        conf.addResource("output-collector.xml");
        conf.addResource("query-mapping.xml");
        conf.addResource("transformer-env.xml");
        conf.set("mapreduce.app-submission.cross-platform","true");
        conf.set("mapreduce.framework.name","local");
    	conf.set("hbase.zookeeper.quorum", "node04");
        this.conf = HBaseConfiguration.create(conf);
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();
        // 处理参数
        this.processArgs(conf, args);

        Job job = Job.getInstance(conf, "new_install_user");

        job.setJarByClass(NewInstallUserRunner.class);
        // 本地运行
        TableMapReduceUtil.initTableMapperJob(
        		initScans(job), 
        		NewInstallUserMapper.class, 
        		StatsUserDimension.class, 
        		TimeOutputValue.class, 
        		job, 
        		false);
        // 集群运行：本地提交和打包(jar)提交
//         TableMapReduceUtil.initTableMapperJob(initScans(job), NewInstallUserMapper.class, StatsUserDimension.class, TimeOutputValue.class, job);
        job.setReducerClass(NewInstallUserReducer.class);
        job.setOutputKeyClass(StatsUserDimension.class);
        job.setOutputValueClass(MapWritableValue.class);
//        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TransformerOutputFormat.class);
        if (job.waitForCompletion(true)) {
            // 执行成功, 需要计算总用户
            this.calculateTotalUsers(conf);
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 计算总用户
     * 
     * @param conf
     */
    private void calculateTotalUsers(Configuration conf) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            long date = TimeUtil.parseString2Long(conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            // 获取今天的date dimension
            DateDimension todayDimension = DateDimension.buildDate(date, DateEnum.DAY);
            // 获取昨天的date dimension
            DateDimension yesterdayDimension = DateDimension.buildDate(date - GlobalConstants.DAY_OF_MILLISECONDS, DateEnum.DAY);
            int yesterdayDimensionId = -1;
            int todayDimensionId = -1;

            // 1. 获取时间id
            conn = JdbcManager.getConnection(conf, GlobalConstants.WAREHOUSE_OF_REPORT);
            // 获取执行时间的昨天的
            pstmt = conn.prepareStatement("SELECT `id` FROM `dimension_date` WHERE `year` = ? AND `season` = ? AND `month` = ? AND `week` = ? AND `day` = ? AND `type` = ? AND `calendar` = ?");
            int i = 0;
            pstmt.setInt(++i, yesterdayDimension.getYear());
            pstmt.setInt(++i, yesterdayDimension.getSeason());
            pstmt.setInt(++i, yesterdayDimension.getMonth());
            pstmt.setInt(++i, yesterdayDimension.getWeek());
            pstmt.setInt(++i, yesterdayDimension.getDay());
            pstmt.setString(++i, yesterdayDimension.getType());
            pstmt.setDate(++i, new Date(yesterdayDimension.getCalendar().getTime()));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                yesterdayDimensionId = rs.getInt(1);
            }

            // 获取执行时间当天的id
            pstmt = conn.prepareStatement("SELECT `id` FROM `dimension_date` WHERE `year` = ? AND `season` = ? AND `month` = ? AND `week` = ? AND `day` = ? AND `type` = ? AND `calendar` = ?");
            i = 0;
            pstmt.setInt(++i, todayDimension.getYear());
            pstmt.setInt(++i, todayDimension.getSeason());
            pstmt.setInt(++i, todayDimension.getMonth());
            pstmt.setInt(++i, todayDimension.getWeek());
            pstmt.setInt(++i, todayDimension.getDay());
            pstmt.setString(++i, todayDimension.getType());
            pstmt.setDate(++i, new Date(todayDimension.getCalendar().getTime()));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todayDimensionId = rs.getInt(1);
            }

            // 2.获取昨天的原始数据,存储格式为:platformid = totalusers
            Map<String, Integer> oldValueMap = new HashMap<String, Integer>();

            // 开始更新stats_user
            if (yesterdayDimensionId > -1) {
                pstmt = conn.prepareStatement("select `platform_dimension_id`,`total_install_users` from `stats_user` where `date_dimension_id`=?");
                pstmt.setInt(1, yesterdayDimensionId);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    int platformId = rs.getInt("platform_dimension_id");
                    int totalUsers = rs.getInt("total_install_users");
                    oldValueMap.put("" + platformId, totalUsers);
                }
            }

            // 添加今天的总用户
            pstmt = conn.prepareStatement("select `platform_dimension_id`,`new_install_users` from `stats_user` where `date_dimension_id`=?");
            pstmt.setInt(1, todayDimensionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int platformId = rs.getInt("platform_dimension_id");
                int newUsers = rs.getInt("new_install_users");
                if (oldValueMap.containsKey("" + platformId)) {
                    newUsers += oldValueMap.get("" + platformId);
                }
                oldValueMap.put("" + platformId, newUsers);
            }

            // 更新操作
            pstmt = conn.prepareStatement("INSERT INTO `stats_user`(`platform_dimension_id`,`date_dimension_id`,`total_install_users`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `total_install_users` = ?");
            for (Map.Entry<String, Integer> entry : oldValueMap.entrySet()) {
                pstmt.setInt(1, Integer.valueOf(entry.getKey()));
                pstmt.setInt(2, todayDimensionId);
                pstmt.setInt(3, entry.getValue());
                pstmt.setInt(4, entry.getValue());
                pstmt.execute();
            }

         // 开始更新stats_device_browser
            oldValueMap.clear();
            if (yesterdayDimensionId > -1) {
                pstmt = conn.prepareStatement("select `platform_dimension_id`,`browser_dimension_id`,`total_install_users` from `stats_device_browser` where `date_dimension_id`=?");
                pstmt.setInt(1, yesterdayDimensionId);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    int platformId = rs.getInt("platform_dimension_id");
                    int browserId = rs.getInt("browser_dimension_id");
                    int totalUsers = rs.getInt("total_install_users");
                    oldValueMap.put(platformId + "_" + browserId, totalUsers);
                }
            }

            // 添加今天的总用户
            pstmt = conn.prepareStatement("select `platform_dimension_id`,`browser_dimension_id`,`new_install_users` from `stats_device_browser` where `date_dimension_id`=?");
            pstmt.setInt(1, todayDimensionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int platformId = rs.getInt("platform_dimension_id");
                int browserId = rs.getInt("browser_dimension_id");
                int newUsers = rs.getInt("new_install_users");
                String key = platformId + "_" + browserId;
                if (oldValueMap.containsKey(key)) {
                    newUsers += oldValueMap.get(key);
                }
                oldValueMap.put(key, newUsers);
            }

            // 更新操作
            pstmt = conn.prepareStatement("INSERT INTO `stats_device_browser`(`platform_dimension_id`,`browser_dimension_id`,`date_dimension_id`,`total_install_users`) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `total_install_users` = ?");
            for (Map.Entry<String, Integer> entry : oldValueMap.entrySet()) {
                String[] key = entry.getKey().split("_");
                pstmt.setInt(1, Integer.valueOf(key[0]));
                pstmt.setInt(2, Integer.valueOf(key[1]));
                pstmt.setInt(3, todayDimensionId);
                pstmt.setInt(4, entry.getValue());
                pstmt.setInt(5, entry.getValue());
                pstmt.execute();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理参数
     * 
     * @param conf
     * @param args
     */
    private void processArgs(Configuration conf, String[] args) {
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
        System.out.println("----------------------" + date);
        conf.set(GlobalConstants.RUNNING_DATE_PARAMES, date);
    }

    /**
     * 初始化scan集合
     * 
     * @param job
     * @return
     */
    private List<Scan> initScans(Job job) {
        // 时间戳+....
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
        // 过滤数据，只分析launch事件
        filterList.addFilter(new SingleColumnValueFilter(Bytes.toBytes(EventLogConstants.EVENT_LOGS_FAMILY_NAME), Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME), CompareOp.EQUAL, Bytes.toBytes(EventEnum.LAUNCH.alias)));
        // 定义mapper中需要获取的列名
        String[] columns = new String[] {
        			EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME, 
        			EventLogConstants.LOG_COLUMN_NAME_UUID, 
        			EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, 
        			EventLogConstants.LOG_COLUMN_NAME_PLATFORM, 
        			EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, 
        			EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION };
//        scan.addColumn(family, qualifier)
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
