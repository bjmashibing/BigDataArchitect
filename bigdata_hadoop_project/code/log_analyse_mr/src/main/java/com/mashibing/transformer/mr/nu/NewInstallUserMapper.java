package com.mashibing.transformer.mr.nu;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.mashibing.common.DateEnum;
import com.mashibing.common.EventLogConstants;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsCommonDimension;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.dim.base.BrowserDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;

/**
 * 自定义的计算新用户的mapper类
 * 
 * @author 马士兵教育
 *
 */
public class NewInstallUserMapper extends TableMapper<StatsUserDimension, TimeOutputValue> {//每个分析条件（由各个维度组成的）作为key，uuid作为value
   
	private static final Logger logger = Logger.getLogger(NewInstallUserMapper.class);

	private StatsUserDimension statsUserDimension = new StatsUserDimension();
    private TimeOutputValue timeOutputValue = new TimeOutputValue();
    
    private byte[] family = Bytes.toBytes(EventLogConstants.EVENT_LOGS_FAMILY_NAME);
    
    //代表用户分析模块的统计
    private KpiDimension newInstallUserKpi = new KpiDimension(KpiType.NEW_INSTALL_USER.name);
    //浏览器分析模块的统计
    private KpiDimension newInstallUserOfBrowserKpi = new KpiDimension(KpiType.BROWSER_NEW_INSTALL_USER.name);

    /**
     * map 读取hbase中的数据，输入数据为：hbase表中每一行。
     * 输出key类型：StatsUserDimension
     * value类型：TimeOutputValue
     */
    @Override
    protected void map(ImmutableBytesWritable key, Result value, Context context) 
    		throws IOException, InterruptedException {
    	
        String uuid = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_UUID)));
        String serverTime = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME)));
        String platform = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_PLATFORM)));
        
        System.out.println(uuid + "-" + serverTime + "-" + platform);
        
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(serverTime) || StringUtils.isBlank(platform)) {
            logger.warn("uuid&servertime&platform不能为空");
            return;
        }
        
        long longOfTime = Long.valueOf(serverTime.trim());
        timeOutputValue.setId(uuid); // 设置id为uuid
        timeOutputValue.setTime(longOfTime); // 设置时间为服务器时间
        
        DateDimension dateDimension = DateDimension.buildDate(longOfTime, DateEnum.DAY);
        
        // 设置date维度
        StatsCommonDimension statsCommonDimension = this.statsUserDimension.getStatsCommon();
        statsCommonDimension.setDate(dateDimension);

        List<PlatformDimension> platformDimensions = PlatformDimension.buildList(platform);
        
        // browser相关的数据
        String browserName = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME)));
        String browserVersion = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION)));
        List<BrowserDimension> browserDimensions = BrowserDimension.buildList(browserName, browserVersion);
       
        //空浏览器维度，不考虑浏览器维度
        BrowserDimension defaultBrowser = new BrowserDimension("", "");
       
        for (PlatformDimension pf : platformDimensions) {
        	statsUserDimension.setBrowser(defaultBrowser);
            statsCommonDimension.setKpi(newInstallUserKpi);
            statsCommonDimension.setPlatform(pf);
            context.write(statsUserDimension, timeOutputValue);

            for (BrowserDimension br : browserDimensions) {
                statsCommonDimension.setKpi(newInstallUserOfBrowserKpi);
                statsUserDimension.setBrowser(br);
                context.write(statsUserDimension, timeOutputValue);
            }
        }
        
    }
}
