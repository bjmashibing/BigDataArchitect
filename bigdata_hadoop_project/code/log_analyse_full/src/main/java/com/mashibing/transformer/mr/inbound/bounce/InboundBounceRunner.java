package com.mashibing.transformer.mr.inbound.bounce;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;

import java.io.IOException;

import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.log4j.Logger;

import com.mashibing.common.EventLogConstants;
import com.mashibing.common.EventLogConstants.EventEnum;
import com.mashibing.transformer.model.dim.StatsInboundBounceDimension;
import com.mashibing.transformer.model.dim.StatsInboundDimension;
import com.mashibing.transformer.model.value.reduce.InboundBounceReduceValue;
import com.mashibing.transformer.mr.TransformerBaseRunner;
import com.mashibing.transformer.mr.inbound.InboundMapper;
import com.mashibing.transformer.mr.inbound.bounce.InboundBounceSecondSort.InboundBounceGroupingComparator;
import com.mashibing.transformer.mr.inbound.bounce.InboundBounceSecondSort.InboundBouncePartitioner;

/**
 * 计算inbound 跳出率的入口类
 * 
 * @author 马士兵教育
 *
 */
public class InboundBounceRunner extends TransformerBaseRunner {
    private static final Logger logger = Logger.getLogger(InboundBounceRunner.class);

    public static void main(String[] args) {
        InboundBounceRunner runner = new InboundBounceRunner();
        runner.setupRunner("inbound_bounce", InboundBounceRunner.class, InboundBounceMapper.class, InboundBounceReducer.class, StatsInboundBounceDimension.class, IntWritable.class, StatsInboundDimension.class, InboundBounceReduceValue.class);
        try {
            runner.startRunner(args);
        } catch (Exception e) {
            logger.error("执行异常", e);
            throw new RuntimeException("执行异常", e);
        }
    }

    @Override
    protected void beforeRunJob(Job job) throws IOException {
        super.beforeRunJob(job);
        // 自定义二次排序
        job.setGroupingComparatorClass(InboundBounceGroupingComparator.class);
        job.setPartitionerClass(InboundBouncePartitioner.class);
    }

    @Override
    protected Filter fetchHbaseFilter() {
        FilterList list = new FilterList();
        String[] columns = new String[] { EventLogConstants.LOG_COLUMN_NAME_REFERRER_URL, // 前一个页面的url
                EventLogConstants.LOG_COLUMN_NAME_SESSION_ID, // 会话id
                EventLogConstants.LOG_COLUMN_NAME_PLATFORM, // 平台名称
                EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, // 服务器时间
                EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME // 事件名称
        };
        list.addFilter(this.getColumnFilter(columns));
        list.addFilter(new SingleColumnValueFilter(InboundMapper.family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME), CompareOp.EQUAL, Bytes.toBytes(EventEnum.PAGEVIEW.alias)));
        return list;
    }
}
