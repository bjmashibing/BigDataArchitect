package com.mashibing.transformer.mr;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;

import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.value.BaseStatsValueWritable;
import com.mashibing.transformer.service.IDimensionConverter;

/**
 * 自定义的配合自定义output进行具体sql输出的类
 * 
 * @author 马士兵教育
 *
 */
public interface IOutputCollector {

    /**
     * 具体执行统计数据插入的方法
     * 
     * @param conf
     * @param key
     * @param value
     * @param pstmt
     * @param converter
     * @throws SQLException
     * @throws IOException
     */
    public void collect(Configuration conf, BaseDimension key, BaseStatsValueWritable value, PreparedStatement pstmt, IDimensionConverter converter) throws SQLException, IOException;
}
