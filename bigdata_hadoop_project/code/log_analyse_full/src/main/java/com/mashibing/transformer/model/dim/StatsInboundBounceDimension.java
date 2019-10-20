package com.mashibing.transformer.model.dim;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mashibing.transformer.model.dim.base.BaseDimension;

/**
 * 统计inbound的跳出会话维度类
 * 
 * @author 马士兵教育
 *
 */
public class StatsInboundBounceDimension extends StatsDimension {
    private StatsCommonDimension statsCommon = new StatsCommonDimension();
    private String sid;
    private long serverTime;

    /**
     * 克隆一个对象
     * 
     * @param dimension
     * @return
     */
    public static StatsInboundBounceDimension clone(StatsInboundBounceDimension dimension) {
        return new StatsInboundBounceDimension(StatsCommonDimension.clone(dimension.statsCommon), dimension.sid, dimension.serverTime);
    }

    public StatsInboundBounceDimension() {
        super();
    }

    public StatsInboundBounceDimension(StatsCommonDimension statsCommon, String sid, long serverTime) {
        super();
        this.statsCommon = statsCommon;
        this.sid = sid;
        this.serverTime = serverTime;
    }

    public StatsCommonDimension getStatsCommon() {
        return statsCommon;
    }

    public void setStatsCommon(StatsCommonDimension statsCommon) {
        this.statsCommon = statsCommon;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        this.statsCommon.write(out);
        out.writeUTF(this.sid);
        out.writeLong(this.serverTime);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.statsCommon.readFields(in);
        this.sid = in.readUTF();
        this.serverTime = in.readLong();
    }

    @Override
    public int compareTo(BaseDimension o) {
        StatsInboundBounceDimension other = (StatsInboundBounceDimension) o;
        int tmp = this.statsCommon.compareTo(other.statsCommon);
        if (tmp != 0) {
            return tmp;
        }
        tmp = this.sid.compareTo(other.sid);
        if (tmp != 0) {
            return tmp;
        }
        tmp = Long.compare(this.serverTime, other.serverTime);
        return tmp;
    }

}
