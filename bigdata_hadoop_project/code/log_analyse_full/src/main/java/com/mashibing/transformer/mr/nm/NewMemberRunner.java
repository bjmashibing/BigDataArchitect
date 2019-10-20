package com.mashibing.transformer.mr.nm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.EventLogConstants;
import com.mashibing.common.GlobalConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.TransformerBaseRunner;
import com.mashibing.util.JdbcManager;
import com.mashibing.util.TimeUtil;

/**
 * 计算新增会员的入口类
 * 
 * @author 马士兵教育
 *
 */
public class NewMemberRunner extends TransformerBaseRunner {
    private static final Logger logger = Logger.getLogger(NewMemberRunner.class);

    public static void main(String[] args) {
        NewMemberRunner runner = new NewMemberRunner();
        runner.setupRunner("new-member", NewMemberRunner.class, NewMemberMapper.class, NewMemberReducer.class, StatsUserDimension.class, TimeOutputValue.class, StatsUserDimension.class, MapWritableValue.class);
        try {
            runner.startRunner(args);
        } catch (Exception e) {
            logger.error("运行new member任务出现异常", e);
            throw new RuntimeException(e);
        }
        try {
            ToolRunner.run(new Configuration(), new NewMemberRunner(), args);
        } catch (Exception e) {
            logger.error("统计新增会员&总会员失败，出现异常信息.", e);
            throw new RuntimeException("job执行异常", e);
        }
    }

    @Override
    protected Filter fetchHbaseFilter() {
        FilterList filterList = new FilterList();
        // 定义mapper中需要获取的列名
        String[] columns = new String[] { EventLogConstants.LOG_COLUMN_NAME_MEMBER_ID, // 会员id
                EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, // 服务器时间
                EventLogConstants.LOG_COLUMN_NAME_PLATFORM, // 平台名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, // 浏览器名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION // 浏览器版本信息
        };
        filterList.addFilter(this.getColumnFilter(columns));
        return filterList;
    }

    @Override
    protected void afterRunJob(Job job, Throwable error) throws IOException {
        try {
            if (error == null && job.isSuccessful()) {
                // job运行没有异常，而且运行成功，那么进行计算total member的代码
                this.calculateTotalMembers(job.getConfiguration());
            } else if (error == null) {
                // job运行没有产生异常，但是运行失败
                throw new RuntimeException("job 运行失败");
            }
        } catch (Throwable e) {
            if (error != null) {
                error = e;
            }
            throw new IOException("调用afterRunJob产生异常", e);
        } finally {
            super.afterRunJob(job, error);
        }
    }

    /**
     * 计算总会员
     * 
     * @param conf
     */
    private void calculateTotalMembers(Configuration conf) {
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
                pstmt = conn.prepareStatement("select `platform_dimension_id`,`total_members` from `stats_user` where `date_dimension_id`=?");
                pstmt.setInt(1, yesterdayDimensionId);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    int platformId = rs.getInt("platform_dimension_id");
                    int totalMembers = rs.getInt("total_members");
                    oldValueMap.put("" + platformId, totalMembers);
                }
            }

            // 添加今天的总用户
            pstmt = conn.prepareStatement("select `platform_dimension_id`,`new_members` from `stats_user` where `date_dimension_id`=?");
            pstmt.setInt(1, todayDimensionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int platformId = rs.getInt("platform_dimension_id");
                int newMembers = rs.getInt("new_members");
                if (oldValueMap.containsKey("" + platformId)) {
                    newMembers += oldValueMap.get("" + platformId);
                }
                oldValueMap.put("" + platformId, newMembers);
            }

            // 更新操作
            pstmt = conn.prepareStatement("INSERT INTO `stats_user`(`platform_dimension_id`,`date_dimension_id`,`total_members`) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE `total_members` = ?");
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
                pstmt = conn.prepareStatement("select `platform_dimension_id`,`browser_dimension_id`,`total_members` from `stats_device_browser` where `date_dimension_id`=?");
                pstmt.setInt(1, yesterdayDimensionId);
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    int platformId = rs.getInt("platform_dimension_id");
                    int browserId = rs.getInt("browser_dimension_id");
                    int totalMembers = rs.getInt("total_members");
                    oldValueMap.put(platformId + "_" + browserId, totalMembers);
                }
            }

            // 添加今天的总用户
            pstmt = conn.prepareStatement("select `platform_dimension_id`,`browser_dimension_id`,`new_members` from `stats_device_browser` where `date_dimension_id`=?");
            pstmt.setInt(1, todayDimensionId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int platformId = rs.getInt("platform_dimension_id");
                int browserId = rs.getInt("browser_dimension_id");
                int newMembers = rs.getInt("new_members");
                String key = platformId + "_" + browserId;
                if (oldValueMap.containsKey(key)) {
                    newMembers += oldValueMap.get(key);
                }
                oldValueMap.put(key, newMembers);
            }

            // 更新操作
            pstmt = conn.prepareStatement("INSERT INTO `stats_device_browser`(`platform_dimension_id`,`browser_dimension_id`,`date_dimension_id`,`total_members`) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `total_members` = ?");
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
}
