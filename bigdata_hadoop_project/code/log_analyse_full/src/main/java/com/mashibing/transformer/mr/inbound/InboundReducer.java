package com.mashibing.transformer.mr.inbound;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.mapreduce.Reducer;

import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsInboundDimension;
import com.mashibing.transformer.model.value.map.TextsOutputValue;
import com.mashibing.transformer.model.value.reduce.InboundReduceValue;

/**
 * 计算reducer类
 * 
 * @author 马士兵教育
 *
 */
public class InboundReducer extends Reducer<StatsInboundDimension, TextsOutputValue, StatsInboundDimension, InboundReduceValue> {
    private Set<String> uvs = new HashSet<String>();
    private Set<String> visits = new HashSet<String>();
    private InboundReduceValue outputValue = new InboundReduceValue();

    @Override
    protected void reduce(StatsInboundDimension key, Iterable<TextsOutputValue> values, Context context) throws IOException, InterruptedException {
        try {
            for (TextsOutputValue value : values) {
                this.uvs.add(value.getUuid());
                this.visits.add(value.getSid());
            }

            this.outputValue.setKpi(KpiType.valueOfName(key.getStatsCommon().getKpi().getKpiName()));
            this.outputValue.setUvs(this.uvs.size());
            this.outputValue.setVisit(this.visits.size());
            context.write(key, this.outputValue);
        } finally {
            // 清空操作
            this.uvs.clear();
            this.visits.clear();
        }
    }
}
