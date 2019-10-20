package com.mashibing.transformer.mr.inbound.bounce;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsCommonDimension;
import com.mashibing.transformer.model.dim.StatsInboundBounceDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;
import com.mashibing.transformer.mr.TransformerBaseMapper;
import com.mashibing.transformer.service.impl.InboundDimensionService;
import com.mashibing.transformer.util.UrlUtil;

/**
 * 计算外链跳出会话的会话个数
 * 
 * @author 马士兵教育
 *
 */
public class InboundBounceMapper extends TransformerBaseMapper<StatsInboundBounceDimension, IntWritable> {
    private static final Logger logger = Logger.getLogger(InboundBounceMapper.class);
    /**
     * 默认inbound id，用于标示不是外链
     */
    public static final int DEFAULT_INBOUND_ID = 0;
    private Map<String, Integer> inbounds = null;
    private StatsInboundBounceDimension statsInboundBounceDimension = new StatsInboundBounceDimension();
    private IntWritable outputValue = new IntWritable();
    private KpiDimension inboundBounceKpi = new KpiDimension(KpiType.INBOUND_BOUNCE.name);

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        try {
            // 获取inbound相关数据
            this.inbounds = InboundDimensionService.getInboundByType(context.getConfiguration(), 0);
        } catch (SQLException e) {
            logger.error("获取外链id出现数据库异常", e);
            throw new IOException("出现异常", e);
        }
    }

    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
        this.inputRecords++;
        // 获取platform， servertime， referrer url，sid
        String platform = this.getPlatform(value);
        String serverTime = this.getServerTime(value);
        String referrerUrl = this.getReferrerUrl(value);
        String sid = this.getSessionId(value);

        // 过滤
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(serverTime) || StringUtils.isBlank(referrerUrl) || StringUtils.isBlank(sid) || !StringUtils.isNumeric(serverTime.trim())) {
            this.filterRecords++;
            logger.warn("平台&服务器时间&前一个页面的url&会话id不能为空，而且服务器时间必须为时间戳形式");
            return;
        }

        // 创建polatform
        List<PlatformDimension> platforms = PlatformDimension.buildList(platform);

        // 创建date
        long longOfTime = Long.valueOf(serverTime.trim());
        DateDimension dayOfDimension = DateDimension.buildDate(longOfTime, DateEnum.DAY);

        // 构建inbound；转换url为外链id
        int inboundId = DEFAULT_INBOUND_ID;
        try {
            inboundId = this.getInboundIdByHost(UrlUtil.getHost(referrerUrl));
        } catch (Throwable e) {
            logger.warn("获取referrer url对应的inbound id异常", e);
            inboundId = DEFAULT_INBOUND_ID;
        }

        // 输出定义
        this.outputValue.set(inboundId);
        StatsCommonDimension statsCommon = this.statsInboundBounceDimension.getStatsCommon();
        statsCommon.setDate(dayOfDimension);
        statsCommon.setKpi(this.inboundBounceKpi);
        this.statsInboundBounceDimension.setSid(sid);
        this.statsInboundBounceDimension.setServerTime(longOfTime);
        for (PlatformDimension pf : platforms) {
            statsCommon.setPlatform(pf);
            context.write(this.statsInboundBounceDimension, this.outputValue);
            this.outputRecords++;
        }
    }

    /**
     * 根据url的host来获取不同的inbound
     * id值，如果该host是统计统计网站的本身host，那么直接返回0，也就是说如果host不属于外链，那么返回0
     * 
     * @param host
     * @return
     */
    private int getInboundIdByHost(String host) {
        int id = DEFAULT_INBOUND_ID;
        if (UrlUtil.isValidateInboundHost(host)) {
            // 是一个有效的外链host，那么进行inbound id获取操作
            id = InboundDimensionService.OTHER_OF_INBOUND_ID;

            // 查看是否是一个具体的inbound id值
            for (Map.Entry<String, Integer> entry : this.inbounds.entrySet()) {
                String urlRegex = entry.getKey();
                if (host.equals(urlRegex) || host.startsWith(urlRegex) || host.matches(urlRegex)) {
                    id = entry.getValue();
                    break;
                }
            }
        }
        return id;
    }
}
