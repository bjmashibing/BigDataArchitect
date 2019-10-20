package com.mashibing.transformer.mr.pv;

import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.NullWritable;
import org.apache.log4j.Logger;

import com.mashibing.common.EventLogConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.TransformerBaseRunner;

/**
 * 计算website的pv值的mapreducer入口类<br/>
 * 从hbase中获取platform、servertime、browsername、browserversion以及url，
 * 并且只获取pageview事件的数据<br/>
 * 将计算得到的pv值保存到stats_device_browser表中。
 * 
 * @author 马士兵教育
 *
 */
public class PageViewRunner extends TransformerBaseRunner {
    private static final Logger logger = Logger.getLogger(PageViewRunner.class);

    public static void main(String[] args) {
        PageViewRunner runner = new PageViewRunner();
        runner.setupRunner("website_pageview", PageViewRunner.class, PageViewMapper.class, PageViewReducer.class, StatsUserDimension.class, NullWritable.class, StatsUserDimension.class, MapWritableValue.class);
        try {
            runner.startRunner(args);
        } catch (Exception e) {
            logger.error("计算pv任务出现异常", e);
            throw new RuntimeException("运行job异常", e);
        }
    }

    @Override
    protected Filter fetchHbaseFilter() {
        FilterList filterList = new FilterList();
        // 只需要pageview事件
        filterList.addFilter(new SingleColumnValueFilter(PageViewMapper.family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME), CompareOp.EQUAL, Bytes.toBytes(EventLogConstants.EventEnum.PAGEVIEW.alias)));
        // 定义mapper中需要获取的列名
        String[] columns = new String[] { EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME, // 获取事件名称
                EventLogConstants.LOG_COLUMN_NAME_CURRENT_URL, // 当前url
                EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, // 服务器时间
                EventLogConstants.LOG_COLUMN_NAME_PLATFORM, // 平台名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, // 浏览器名称
                EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION // 浏览器版本号
        };
        filterList.addFilter(this.getColumnFilter(columns));

        return filterList;
    }
}
