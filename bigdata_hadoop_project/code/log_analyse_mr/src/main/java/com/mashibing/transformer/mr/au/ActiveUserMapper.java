package com.mashibing.transformer.mr.au;

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
 * Active user的mapper类
 * 
 * @author 马士兵教育
 *
 */
public class ActiveUserMapper extends TableMapper<StatsUserDimension, TimeOutputValue> {
	private static final Logger logger = Logger.getLogger(ActiveUserMapper.class);
	private byte[] family = Bytes.toBytes(EventLogConstants.EVENT_LOGS_FAMILY_NAME);
	private StatsUserDimension outputKey = new StatsUserDimension();
	private TimeOutputValue outputValue = new TimeOutputValue();
	private BrowserDimension defaultBrowser = new BrowserDimension("", ""); // 默认的browser对象
	private KpiDimension activeUserKpi = new KpiDimension(KpiType.ACTIVE_USER.name);
	private KpiDimension activeUserOfBrowserKpi = new KpiDimension(KpiType.BROWSER_ACTIVE_USER.name);

	@Override
	protected void map(ImmutableBytesWritable key, Result value, Context context)
			throws IOException, InterruptedException {
		// 获取uuid&platform&serverTime，从hbase返回的结果集Result中
		String uuid = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_UUID)));
		String platform = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_PLATFORM)));
		String serverTime = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME)));

		// 过滤无效数据
		if (StringUtils.isBlank(uuid) || StringUtils.isBlank(platform)
				|| StringUtils.isBlank(serverTime) || !StringUtils.isNumeric(serverTime.trim())) {
			logger.warn("uuid&platform&serverTime不能为空，而且serverTime必须为时间戳");
			return;
		}

		this.outputValue.setId(uuid);

		long longOfServerTime = Long.valueOf(serverTime.trim());

		DateDimension dateDimension = DateDimension.buildDate(longOfServerTime, DateEnum.DAY);

		// 进行platform的构建
		List<PlatformDimension> platforms = PlatformDimension.buildList(platform); // 进行platform创建
		
		// 获取browser name和browser version
		String browser = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME)));
		String browserVersion = Bytes.toString(value.getValue(family, Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION)));
		// 进行browser的维度信息构建
		List<BrowserDimension> browsers = BrowserDimension.buildList(browser, browserVersion);

		// 开始进行输出
		StatsCommonDimension statsCommonDimension = this.outputKey.getStatsCommon();
		// 设置date dimension
		statsCommonDimension.setDate(dateDimension);
		for (PlatformDimension pf : platforms) {
			this.outputKey.setBrowser(defaultBrowser); // 进行覆盖操作
			// 设置platform dimension
			statsCommonDimension.setPlatform(pf);
			// 设置kpi dimension
			statsCommonDimension.setKpi(activeUserKpi);
			context.write(this.outputKey, this.outputValue);

			// 输出browser维度统计
			statsCommonDimension.setKpi(activeUserOfBrowserKpi);
			for (BrowserDimension bw : browsers) {
				this.outputKey.setBrowser(bw); // 设置对应的browsers
				context.write(this.outputKey, this.outputValue);
			}
		}
	}
}
