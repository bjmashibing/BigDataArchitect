package com.mashibing.transformer.mr.pv;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;

/**
 * 统计website的page view数量的reducer类<br/>
 * 不涉及到去重，直接统计输入到reducer的同一组key中，value的个数。
 * 
 * @author 马士兵教育
 *
 */
public class PageViewReducer extends Reducer<StatsUserDimension, NullWritable, StatsUserDimension, MapWritableValue> {
    private MapWritableValue mapWritableValue = new MapWritableValue();
    private MapWritable map = new MapWritable();

    @SuppressWarnings("unused")
    @Override
    protected void reduce(StatsUserDimension key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
        int pvCount = 0;
        for (NullWritable value : values) {
            // pv++，每一条数据算一个pv，不涉及到去重
            pvCount++;
        }
        
        // 填充value
        this.map.put(new IntWritable(-1), new IntWritable(pvCount));
        this.mapWritableValue.setValue(this.map);

        // 填充kpi
        this.mapWritableValue.setKpi(KpiType.valueOfName(key.getStatsCommon().getKpi().getKpiName()));

        // 输出
        context.write(key, this.mapWritableValue);
    }
}
