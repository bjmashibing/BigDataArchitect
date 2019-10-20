package com.mashibing.transformer.mr.pv;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsCommonDimension;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.BrowserDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;
import com.mashibing.transformer.mr.TransformerBaseMapper;

/**
 * 统计pv的mapper类<br/>
 * 输入时hbase的数据，包括: platform、serverTime、browserName、browserVersion、url<br/>
 * 输出<StatsUserDimension, NullWritable>键值对，输出key中包含platform、date以及browser的维度信息
 * 
 * @author 马士兵教育
 *
 */
public class PageViewMapper extends TransformerBaseMapper<StatsUserDimension, NullWritable> {
    private static final Logger logger = Logger.getLogger(PageViewMapper.class);
    private StatsUserDimension statsUserDimension = new StatsUserDimension();
    private KpiDimension websitePageViewDimension = new KpiDimension(KpiType.WEBSITE_PAGEVIEW.name);

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        // 1. 获取platform、time、url
        String platform = this.getPlatform(value);
        String serverTime = this.getServerTime(value);
        String url = this.getCurrentUrl(value);

        // 2. 过滤数据
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(url) || StringUtils.isBlank(serverTime) || !StringUtils.isNumeric(serverTime.trim())) {
            logger.warn("平台&服务器时间&当前url不能为空，而且服务器时间必须为时间戳形式的字符串");
            return ;
        }

        // 3. 创建platform维度信息
        List<PlatformDimension> platforms = PlatformDimension.buildList(platform);
        // 4. 创建browser维度信息
        String browserName = this.getBrowserName(value);
        String browserVersion = this.getBrowserVersion(value);
        List<BrowserDimension> browsers = BrowserDimension.buildList(browserName, browserVersion);
        // 5. 创建date维度信息
        DateDimension dayOfDimenion = DateDimension.buildDate(Long.valueOf(serverTime.trim()), DateEnum.DAY);

        // 6. 输出的写出
        StatsCommonDimension statsCommon = this.statsUserDimension.getStatsCommon();
        statsCommon.setDate(dayOfDimenion); // 设置date dimension
        statsCommon.setKpi(this.websitePageViewDimension); // 设置kpi dimension
        for (PlatformDimension pf : platforms) {
            statsCommon.setPlatform(pf); // 设置platform dimension
            for (BrowserDimension br : browsers) {
                this.statsUserDimension.setBrowser(br); // 设置browser dimension
                // 输出
                context.write(this.statsUserDimension, NullWritable.get());
            }
        }
    }
}
