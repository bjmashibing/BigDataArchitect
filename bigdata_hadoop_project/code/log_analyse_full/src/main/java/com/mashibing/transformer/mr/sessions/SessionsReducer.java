package com.mashibing.transformer.mr.sessions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.mashibing.common.DateEnum;
import com.mashibing.common.GlobalConstants;
import com.mashibing.common.KpiType;
import com.mashibing.transformer.model.dim.StatsUserDimension;
import com.mashibing.transformer.model.value.map.TimeOutputValue;
import com.mashibing.transformer.model.value.reduce.MapWritableValue;
import com.mashibing.transformer.util.TimeChain;
import com.mashibing.util.TimeUtil;

/**
 * 计算会话个数和会话时长的一个reducer类
 * 
 * @author 马士兵教育
 *
 */
public class SessionsReducer extends Reducer<StatsUserDimension, TimeOutputValue, StatsUserDimension, MapWritableValue> {
    private Map<Integer, Map<String, TimeChain>> hourlyTimeChainMap = new HashMap<Integer, Map<String, TimeChain>>(); // 用于计算hourly的会话个数和会话时长
    private Map<String, TimeChain> timeChainMap = new HashMap<String, TimeChain>();
    private MapWritableValue outputValue = new MapWritableValue();
    private MapWritable map = new MapWritable();
    private MapWritable hourlySessionsMap = new MapWritable();
    private MapWritable hourlySessionsLengthMap = new MapWritable();

    /**
     * 初始化方法
     */
    private void startUp() {
        // 初始化操作
        this.map.clear();
        this.hourlySessionsMap.clear();
        this.hourlySessionsLengthMap.clear();
        this.timeChainMap.clear();
        this.hourlyTimeChainMap.clear();
        for (int i = 0; i < 24; i++) {
            this.hourlySessionsMap.put(new IntWritable(i), new IntWritable(0));
            this.hourlySessionsLengthMap.put(new IntWritable(i), new IntWritable(0));
            this.hourlyTimeChainMap.put(i, new HashMap<String, TimeChain>());
        }
    }

    @Override
    protected void reduce(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        this.startUp(); // 初始化操作，清空
        String kpiName = key.getStatsCommon().getKpi().getKpiName();

        if (KpiType.SESSIONS.name.equals(kpiName)) {
            // 计算stats_user表的sessions和sessions_length；同时也计算hourly_sessions&sessions_length
            this.handleSessions(key, values, context);
        } else if (KpiType.BROWSER_SESSIONS.equals(kpiName)) {
            // 处理browser维度的统计信息
            this.handleBrowserSessions(key, values, context);
        }
    }

    /**
     * 处理普通的sessions分析
     * 
     * @param key
     * @param values
     * @param context
     * @throws InterruptedException
     * @throws IOException
     */
    private void handleSessions(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        for (TimeOutputValue value : values) {
            String sid = value.getId();
            long time = value.getTime();

            // 处理正常统计
            TimeChain chain = this.timeChainMap.get(sid);
            if (chain == null) {
                chain = new TimeChain(time);
                this.timeChainMap.put(sid, chain); // 保存
            }
            chain.addTime(time); // 更新时间

            // 处理hourly统计
            int hour = TimeUtil.getDateInfo(time, DateEnum.HOUR);
            Map<String, TimeChain> htcm = this.hourlyTimeChainMap.get(hour);
            TimeChain hourlyChain = htcm.get(sid);
            if (hourlyChain == null) {
                hourlyChain = new TimeChain(time);
                htcm.put(sid, hourlyChain);
                this.hourlyTimeChainMap.put(hour, htcm);
            }
            hourlyChain.addTime(time); // 更新时间
        }

        // 计算hourly的会话个数和会话长度信息
        for (Map.Entry<Integer, Map<String, TimeChain>> entry : this.hourlyTimeChainMap.entrySet()) {
            this.hourlySessionsMap.put(new IntWritable(entry.getKey()), new IntWritable(entry.getValue().size())); // 设置当前小时的session个数
            int presl = 0; // 统计每小时的会话时长
            for (Map.Entry<String, TimeChain> entry2 : entry.getValue().entrySet()) {
                long tmp = entry2.getValue().getTimeOfMillis(); // 间隔毫秒数
                if (tmp < 0 || tmp > 3600000) {
                    // 会话时长小于0或者大于1个小时
                    continue;
                }
                presl += tmp;
            }

            // 2. 计算间隔秒数, 如果毫秒不足一秒，算做一秒
            if (presl % 1000 == 0) {
                presl = presl / 1000;
            } else {
                presl = presl / 1000 + 1;
            }
            this.hourlySessionsLengthMap.put(new IntWritable(entry.getKey()), new IntWritable(presl));
        }

        // 进行hourly sessions输出
        this.outputValue.setValue(this.hourlySessionsMap);
        // 填充kpi
        this.outputValue.setKpi(KpiType.HOURLY_SESSIONS);
        context.write(key, this.outputValue);

        // 进行hourly sessions length输出
        this.outputValue.setValue(this.hourlySessionsLengthMap);
        // 填充kpi
        this.outputValue.setKpi(KpiType.HOURLY_SESSIONS_LENGTH);
        context.write(key, this.outputValue);

        // 计算正常的sessions和sessionslength
        // 计算总的间隔秒数
        int sessionsLength = 0;
        // 1. 计算间隔毫秒数
        for (Map.Entry<String, TimeChain> entry : this.timeChainMap.entrySet()) {
            long tmp = entry.getValue().getTimeOfMillis(); // 间隔毫秒数
            if (tmp < 0 || tmp > GlobalConstants.DAY_OF_MILLISECONDS) {
                // 如果计算的值是小于0或者是大于一天的毫秒数，直接过滤
                continue;
            }
            sessionsLength += tmp;
        }
        // 2. 计算间隔秒数, 如果毫秒不足一秒，算做一秒
        if (sessionsLength % 1000 == 0) {
            sessionsLength = sessionsLength / 1000;
        } else {
            sessionsLength = sessionsLength / 1000 + 1;
        }

        // 填充value
        this.map.put(new IntWritable(-1), new IntWritable(this.timeChainMap.size())); // 填充会话个数
        this.map.put(new IntWritable(-2), new IntWritable(sessionsLength)); // 会话长度
        this.outputValue.setValue(this.map);
        // 填充kpi
        this.outputValue.setKpi(KpiType.SESSIONS);
        context.write(key, this.outputValue);
    }

    /**
     * 处理添加browser维度的sessions分析
     * 
     * @param key
     * @param values
     * @param context
     * @throws InterruptedException
     * @throws IOException
     */
    private void handleBrowserSessions(StatsUserDimension key, Iterable<TimeOutputValue> values, Context context) throws IOException, InterruptedException {
        for (TimeOutputValue value : values) {
            TimeChain chain = this.timeChainMap.get(value.getId());
            if (chain == null) {
                chain = new TimeChain(value.getTime());
                this.timeChainMap.put(value.getId(), chain); // 保存
            }
            chain.addTime(value.getTime()); // 更新时间
        }

        // 计算总的间隔秒数
        int sessionsLength = 0;
        // 1. 计算间隔毫秒数
        for (Map.Entry<String, TimeChain> entry : this.timeChainMap.entrySet()) {
            long tmp = entry.getValue().getTimeOfMillis(); // 间隔毫秒数
            if (tmp < 0 || tmp > GlobalConstants.DAY_OF_MILLISECONDS) {
                // 如果计算的值是小于0或者是大于一天的毫秒数，直接过滤
                continue;
            }
            sessionsLength += tmp;
        }
        // 2. 计算间隔秒数, 如果毫秒不足一秒，算做一秒
        if (sessionsLength % 1000 == 0) {
            sessionsLength = sessionsLength / 1000;
        } else {
            sessionsLength = sessionsLength / 1000 + 1;
        }

        // 填充value
        this.map.put(new IntWritable(-1), new IntWritable(this.timeChainMap.size())); // 填充会话个数
        this.map.put(new IntWritable(-2), new IntWritable(sessionsLength)); // 会话长度
        this.outputValue.setValue(this.map);
        // 填充kpi
        this.outputValue.setKpi(KpiType.BROWSER_SESSIONS);
        context.write(key, this.outputValue);
    }
}
