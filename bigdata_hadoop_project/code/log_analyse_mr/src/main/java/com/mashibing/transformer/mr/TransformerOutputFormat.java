package com.mashibing.transformer.mr;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.log4j.Logger;

import com.mashibing.common.GlobalConstants;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.value.BaseStatsValueWritable;
import com.mashibing.transformer.service.IDimensionConverter;
import com.mashibing.transformer.service.impl.DimensionConverterImpl;
import com.mashibing.util.JdbcManager;

/**
 * 自定义输出到mysql的outputformat类
 * BaseDimension:reducer输出的key
 * BaseStatsValueWritable：reducer输出的value
 * @author 马士兵教育
 *
 */
public class TransformerOutputFormat extends OutputFormat<BaseDimension, BaseStatsValueWritable> {
    private static final Logger logger = Logger.getLogger(TransformerOutputFormat.class);

    /**
     * 定义每条数据的输出格式，一条数据就是reducer任务每次执行write方法输出的数据。
     */
    @Override
	public RecordWriter<BaseDimension, BaseStatsValueWritable> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        Connection conn = null;
        IDimensionConverter converter = new DimensionConverterImpl();
        try {
            conn = JdbcManager.getConnection(conf, GlobalConstants.WAREHOUSE_OF_REPORT);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            logger.error("获取数据库连接失败", e);
            throw new IOException("获取数据库连接失败", e);
        }
        return new TransformerRecordWriter(conn, conf, converter);
    }

    @Override
    public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        // 检测输出空间，输出到mysql不用检测
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
        return new FileOutputCommitter(FileOutputFormat.getOutputPath(context), context);
    }

    /**
     * 自定义具体数据输出writer
     * 
     * @author root
     *
     */
    public class TransformerRecordWriter extends RecordWriter<BaseDimension, BaseStatsValueWritable> {
        private Connection conn = null;
        private Configuration conf = null;
        private IDimensionConverter converter = null;
        private Map<KpiType, PreparedStatement> map = new HashMap<KpiType, PreparedStatement>();
        private Map<KpiType, Integer> batch = new HashMap<KpiType, Integer>();

        public TransformerRecordWriter(Connection conn, Configuration conf, IDimensionConverter converter) {
            super();
            this.conn = conn;
            this.conf = conf;
            this.converter = converter;
        }

        @Override
        /**
         * 当reduce任务输出数据是，由计算框架自动调用。把reducer输出的数据写到mysql中
         */
        public void write(BaseDimension key, BaseStatsValueWritable value) throws IOException, InterruptedException {
            if (key == null || value == null) {
                return;
            }

            try {
                KpiType kpi = value.getKpi();
                PreparedStatement pstmt = null;//每一个pstmt对象对应一个sql语句
                int count = 1;//sql语句的批处理，一次执行10
                if (map.get(kpi) == null) {
                    // 使用kpi进行区分，返回sql保存到config中
                    pstmt = this.conn.prepareStatement(conf.get(kpi.name));
                    map.put(kpi, pstmt);
                } else {
                    pstmt = map.get(kpi);
                    count = batch.get(kpi);
                    count++;
                }
                batch.put(kpi, count); // 批量次数的存储

                String collectorName = conf.get(GlobalConstants.OUTPUT_COLLECTOR_KEY_PREFIX + kpi.name);
                Class<?> clazz = Class.forName(collectorName);
                IOutputCollector collector = (IOutputCollector) clazz.newInstance();//把value插入到mysql的方法。由于kpi维度不一样。插入到不能表里面。
                collector.collect(conf, key, value, pstmt, converter);

                if (count % Integer.valueOf(conf.get(GlobalConstants.JDBC_BATCH_NUMBER, GlobalConstants.DEFAULT_JDBC_BATCH_NUMBER)) == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    batch.put(kpi, 0); // 对应批量计算删除
                }
            } catch (Throwable e) {
                logger.error("在writer中写数据出现异常", e);
                throw new IOException(e);
            }
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            try {
                for (Map.Entry<KpiType, PreparedStatement> entry : this.map.entrySet()) {
                    entry.getValue().executeBatch();
                }
            } catch (SQLException e) {
                logger.error("执行executeUpdate方法异常", e);
                throw new IOException(e);
            } finally {
                try {
                    if (conn != null) {
                        conn.commit(); // 进行connection的提交动作
                    }
                } catch (Exception e) {
                    // nothing
                } finally {
                    for (Map.Entry<KpiType, PreparedStatement> entry : this.map.entrySet()) {
                        try {
                            entry.getValue().close();
                        } catch (SQLException e) {
                            // nothing
                        }
                    }
                    if (conn != null)
                        try {
                            conn.close();
                        } catch (Exception e) {
                            // nothing
                        }
                }
            }
        }

    }
}
