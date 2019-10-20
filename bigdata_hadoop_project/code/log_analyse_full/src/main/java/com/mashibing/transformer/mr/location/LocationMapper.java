package com.mashibing.transformer.mr.location;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsCommonDimension;
import com.mashibing.transformer.model.dim.StatsLocationDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.LocationDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;
import com.mashibing.transformer.model.value.map.TextsOutputValue;
import com.mashibing.transformer.mr.TransformerBaseMapper;

/**
 * 统计location维度信息的mapper类<br/>
 * 输入: country、province、city、platform、servertime、uuid、sid<br/>
 * 一条输入对应6条输出
 * 
 * @author 马士兵教育
 *
 */
public class LocationMapper extends TransformerBaseMapper<StatsLocationDimension, TextsOutputValue> {
    private static final Logger logger = Logger.getLogger(LocationMapper.class);
    private StatsLocationDimension statsLocationDimension = new StatsLocationDimension();
    private TextsOutputValue outputValue = new TextsOutputValue();
    private KpiDimension locationKpiDimension = new KpiDimension(KpiType.LOCATION.name);

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        this.inputRecords++;
        // 获取平台名称、服务器时间、用户id、会话id
        String platform = this.getPlatform(value);
        String serverTime = this.getServerTime(value);
        String uuid = this.getUuid(value);
        String sid = this.getSessionId(value);

        // 过滤无效数据
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(uuid) || StringUtils.isBlank(sid) || StringUtils.isBlank(serverTime) || !StringUtils.isNumeric(serverTime.trim())) {
            logger.warn("平台&uuid&会话id&服务器时间不能为空，而且服务器时间必须为时间戳类型");
            this.filterRecords++;
            return;
        }

        // 时间维度创建
        long longOfTime = Long.valueOf(serverTime.trim());
        DateDimension dayOfDimension = DateDimension.buildDate(longOfTime, DateEnum.DAY);

        // platform维度创建
        List<PlatformDimension> platforms = PlatformDimension.buildList(platform);

        // location维度创建
        String country = this.getCountry(value);
        String province = this.getProvince(value);
        String city = this.getCity(value);
        List<LocationDimension> locations = LocationDimension.buildList(country, province, city);

        // 进行输出定义
        this.outputValue.setUuid(uuid);
        this.outputValue.setSid(sid);
        StatsCommonDimension statsCommon = this.statsLocationDimension.getStatsCommon();
        statsCommon.setDate(dayOfDimension);
        statsCommon.setKpi(this.locationKpiDimension);
        for (PlatformDimension pf : platforms) {
            statsCommon.setPlatform(pf);

            for (LocationDimension location : locations) {
                this.statsLocationDimension.setLocation(location);
                context.write(this.statsLocationDimension, this.outputValue);
                this.outputRecords++;
            }
        }
    }
}
