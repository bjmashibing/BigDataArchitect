package com.mashibing.transformer.mr.nm;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import com.mashibing.common.GlobalConstants;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.value.BaseStatsValueWritable;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.mr.IOutputCollector;
import com.mashibing.transformer.service.IDimensionConverter;

public class NewMemberCollector implements IOutputCollector {

    @Override
    public void collect(Configuration conf, BaseDimension key, BaseStatsValueWritable value, PreparedStatement pstmt, IDimensionConverter converter) throws SQLException, IOException {
        StatsUserDimension statsUser = (StatsUserDimension) key;
        MapWritableValue mapWritableValue = (MapWritableValue) value;

        int i = 0;
        // 设置参数
        switch (mapWritableValue.getKpi()) {
        case NEW_MEMBER: // 统计new member的kpi
            IntWritable v1 = (IntWritable) mapWritableValue.getValue().get(new IntWritable(-1));
            pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getPlatform()));
            pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getDate()));
            pstmt.setInt(++i, v1.get());
            pstmt.setString(++i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            pstmt.setInt(++i, v1.get());
            break;
        case BROWSER_NEW_MEMBER: // 统计browser new member 的kpi
            IntWritable v2 = (IntWritable) mapWritableValue.getValue().get(new IntWritable(-1));
            pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getPlatform()));
            pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getStatsCommon().getDate()));
            pstmt.setInt(++i, converter.getDimensionIdByValue(statsUser.getBrowser()));
            pstmt.setInt(++i, v2.get());
            pstmt.setString(++i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            pstmt.setInt(++i, v2.get());
            break;
        case INSERT_MEMBER_INFO: // 插入member info信息
            Text v3 = (Text) mapWritableValue.getValue().get(new IntWritable(-1));
            pstmt.setString(++i, v3.toString());
            pstmt.setString(++i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            pstmt.setString(++i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            pstmt.setString(++i, conf.get(GlobalConstants.RUNNING_DATE_PARAMES));
            break;
        default:
            throw new RuntimeException("不支持该种kpi输出操作" + mapWritableValue.getKpi());
        }

        // 添加batch
        pstmt.addBatch();
    }

}
