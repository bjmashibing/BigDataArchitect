package com.mashibing.transformer.mr.au;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;

import com.mashibing.common.GlobalConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.value.BaseStatsValueWritable;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.IOutputCollector;
import com.mashibing.transformer.service.IDimensionConverter;

public class HourlyActiveUserCollector implements IOutputCollector {

    @Override
    public void collect(Configuration conf, BaseDimension key, BaseStatsValueWritable value, PreparedStatement pstmt, IDimensionConverter converter) throws SQLException, IOException {
        StatsUserDimension statsUser = (StatsUserDimension) key;
        MapWritableValue mapWritableValue = (MapWritableValue) value;
        MapWritable map = mapWritableValue.getValue();

        // hourly_active_user
        int i = 0;
        pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getPlatform()));
        pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getDate()));
        pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getKpi())); // 根据kpi

        // 设置每个小时的情况
        for (i++; i < 28; i++) {
            int v = ((IntWritable)map.get(new IntWritable(i - 4))).get();
            pstmt.setInt(i, v);
            pstmt.setInt(i + 25, v);
        }

        pstmt.setString(i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
        pstmt.addBatch();
    }

}
