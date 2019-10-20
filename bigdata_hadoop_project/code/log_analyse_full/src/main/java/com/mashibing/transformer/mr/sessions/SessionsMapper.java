package com.mashibing.transformer.mr.sessions;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsCommonDimension;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.BrowserDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.mr.TransformerBaseMapper;

public class SessionsMapper extends TransformerBaseMapper<StatsUserDimension, TimeOutputValue> {
    private static final Logger logger = Logger.getLogger(SessionsMapper.class);
    private StatsUserDimension outputKey = new StatsUserDimension();
    private TimeOutputValue outputValue = new TimeOutputValue();
    private BrowserDimension defaultBrowserDimension = new BrowserDimension("", "");
    private KpiDimension sessionsKpi = new KpiDimension(KpiType.SESSIONS.name);
    private KpiDimension sessionsOfBrowserKpi = new KpiDimension(KpiType.BROWSER_SESSIONS.name);

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        // 获取会话id，serverTime， 平台
        String sessionId = this.getSessionId(value);
        String platform = this.getPlatform(value);
        String serverTime = this.getServerTime(value);

        // 过滤无效数据
        if (StringUtils.isBlank(sessionId) || StringUtils.isBlank(platform) || StringUtils.isBlank(serverTime) || !StringUtils.isNumeric(serverTime.trim())) {
            logger.warn("会话id&platform&服务器时间不能为空，而且服务器时间必须为时间戳形式.");
            return;
        }

        // 创建date 维度
        long longOfTime = Long.valueOf(serverTime.trim());
        DateDimension dayOfDimension = DateDimension.buildDate(longOfTime, DateEnum.DAY);
        // 创建 platform维度
        List<PlatformDimension> platforms = PlatformDimension.buildList(platform);
        // 创建browser维度
        String browserName = this.getBrowserName(value);
        String browserVersion = this.getBrowserVersion(value);
        List<BrowserDimension> browsers = BrowserDimension.buildList(browserName, browserVersion);

        // 进行输出设置
        this.outputValue.setId(sessionId.trim()); // 会话id
        this.outputValue.setTime(longOfTime); // 服务器时间
        StatsCommonDimension statsCommon = this.outputKey.getStatsCommon();
        statsCommon.setDate(dayOfDimension); // 设置时间维度
        for (PlatformDimension pf : platforms) {
            this.outputKey.setBrowser(this.defaultBrowserDimension);
            statsCommon.setPlatform(pf);
            statsCommon.setKpi(this.sessionsKpi);
            context.write(this.outputKey, this.outputValue); // 输出设置

            // browser输出
            statsCommon.setKpi(this.sessionsOfBrowserKpi); // 将kpi更改为输出browser session
            for (BrowserDimension br : browsers) {
                this.outputKey.setBrowser(br);
                context.write(this.outputKey, this.outputValue);
            }
        }
    }
}
