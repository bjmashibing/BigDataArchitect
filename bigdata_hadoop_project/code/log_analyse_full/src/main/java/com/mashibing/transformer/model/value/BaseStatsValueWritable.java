package com.mashibing.transformer.model.value;

import com.mashibing.common.KpiType;
import org.apache.hadoop.io.Writable;

import com.mashibing.common.KpiType;

/**
 * 自定义顶级的输出value父类
 * 
 * @author 马士兵教育
 *
 */
public abstract class BaseStatsValueWritable implements Writable {
    /**
     * 获取当前value对应的kpi值
     * 
     * @return
     */
    public abstract KpiType getKpi();
}
