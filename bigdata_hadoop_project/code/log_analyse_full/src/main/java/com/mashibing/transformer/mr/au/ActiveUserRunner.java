package com.mashibing.transformer.mr.au;

import java.io.IOException;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.log4j.Logger;

import com.mashibing.common.EventLogConstants;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.TransformerBaseRunner;

/**
 * 统计active user的入口类
 * 
 * @author 马士兵教育
 *
 */
public class ActiveUserRunner extends TransformerBaseRunner {
    private static final Logger logger = Logger.getLogger(ActiveUserRunner.class);

    public static void main(String[] args) {
        ActiveUserRunner runner = new ActiveUserRunner();
        runner.setupRunner("active-user", ActiveUserRunner.class, ActiveUserMapper.class, ActiveUserReducer.class, StatsUserDimension.class, TimeOutputValue.class, StatsUserDimension.class, MapWritableValue.class);
        try {
            runner.startRunner(args);
        } catch (Exception e) {
            logger.error("运行active user任务出现异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void configure(String... resourceFiles) {
        super.configure(resourceFiles);
        conf.set("mapred.child.java.opts", "-Xmx500m");
        conf.set("mapreduce.map.output.compress", "true");
    }

    @Override
    protected void beforeRunJob(Job job) throws IOException {
        super.beforeRunJob(job);
        job.setNumReduceTasks(3); // 每个统计维度一个reducer
        job.setPartitionerClass(ActiveUserPartitioner.class); // 设置分区类
        // 不启动推测执行
        job.setMapSpeculativeExecution(false);
        job.setReduceSpeculativeExecution(false);
    }

    @Override
    protected Filter fetchHbaseFilter() {
        FilterList filterList = new FilterList();
        // 定义mapper中需要获取的列名
        String[] columns = new String[] { EventLogConstants.LOG_COLUMN_NAME_UUID, // 用户id
                EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, // 服务器时间
                EventLogConstants.LOG_COLUMN_NAME_PLATFORM, // 平台名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, // 浏览器名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION // 浏览器版本号
        };
        filterList.addFilter(this.getColumnFilter(columns));
        return filterList;
    }

    /**
     * 自定义分区类
     * 
     * @author root
     *
     */
    public static class ActiveUserPartitioner extends Partitioner<StatsUserDimension, TimeOutputValue> {

        @Override
        public int getPartition(StatsUserDimension key, TimeOutputValue value, int numPartitions) {
            if (KpiType.ACTIVE_USER.name.equals(key.getStatsCommon().getKpi().getKpiName())) {
                return 0; // 处理activeuser
            } else if (KpiType.BROWSER_ACTIVE_USER.name.equals(key.getStatsCommon().getKpi().getKpiName())) {
                return 1; // 处理browser active user
            } else if (KpiType.HOURLY_ACTIVE_USER.name.equals(key.getStatsCommon().getKpi().getKpiName())) {
                return 2; // 处理hourly active user
            }
            throw new IllegalArgumentException("无法获取分区id，当前kpi:" + key.getStatsCommon().getKpi().getKpiName() + "，当前reducer个数:" + numPartitions);
        }

    }
}
