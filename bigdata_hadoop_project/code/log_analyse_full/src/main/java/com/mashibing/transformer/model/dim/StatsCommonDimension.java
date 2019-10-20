package com.mashibing.transformer.model.dim;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.dim.base.DateDimension;
import com.mashibing.transformer.model.dim.base.KpiDimension;
import com.mashibing.transformer.model.dim.base.PlatformDimension;

/**
 * 公用的dimension信息组合
 * 
 * @author 马士兵教育
 *
 */
public class StatsCommonDimension extends StatsDimension {
    private DateDimension date = new DateDimension();
    private PlatformDimension platform = new PlatformDimension();
    private KpiDimension kpi = new KpiDimension();

    /**
     * close一个实例对象
     * 
     * @param dimension
     * @return
     */
    public static StatsCommonDimension clone(StatsCommonDimension dimension) {
        DateDimension date = new DateDimension(dimension.date.getId(), dimension.date.getYear(), dimension.date.getSeason(), dimension.date.getMonth(), dimension.date.getWeek(), dimension.date.getDay(), dimension.date.getType(), dimension.date.getCalendar());
        PlatformDimension platform = new PlatformDimension(dimension.platform.getId(), dimension.platform.getPlatformName());
        KpiDimension kpi = new KpiDimension(dimension.kpi.getId(), dimension.kpi.getKpiName());
        return new StatsCommonDimension(date, platform, kpi);
    }

    public StatsCommonDimension() {
        super();
    }

    public StatsCommonDimension(DateDimension date, PlatformDimension platform, KpiDimension kpi) {
        super();
        this.date = date;
        this.platform = platform;
        this.kpi = kpi;
    }

    public DateDimension getDate() {
        return date;
    }

    public void setDate(DateDimension date) {
        this.date = date;
    }

    public PlatformDimension getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformDimension platform) {
        this.platform = platform;
    }

    public KpiDimension getKpi() {
        return kpi;
    }

    public void setKpi(KpiDimension kpi) {
        this.kpi = kpi;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        this.date.write(out);
        this.platform.write(out);
        this.kpi.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.date.readFields(in);
        this.platform.readFields(in);
        this.kpi.readFields(in);
    }

    @Override
    public int compareTo(BaseDimension o) {
        if (this == o) {
            return 0;
        }

        StatsCommonDimension other = (StatsCommonDimension) o;
        int tmp = this.date.compareTo(other.date);
        if (tmp != 0) {
            return tmp;
        }
        tmp = this.platform.compareTo(other.platform);
        if (tmp != 0) {
            return tmp;
        }
        tmp = this.kpi.compareTo(other.kpi);
        return tmp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((kpi == null) ? 0 : kpi.hashCode());
        result = prime * result + ((platform == null) ? 0 : platform.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatsCommonDimension other = (StatsCommonDimension) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (kpi == null) {
            if (other.kpi != null)
                return false;
        } else if (!kpi.equals(other.kpi))
            return false;
        if (platform == null) {
            if (other.platform != null)
                return false;
        } else if (!platform.equals(other.platform))
            return false;
        return true;
    }

}
