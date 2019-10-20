package com.mashibing.transformer.model.dim;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.mashibing.transformer.model.dim.base.BaseDimension;
import com.mashibing.transformer.model.dim.base.LocationDimension;

/**
 * 统计location相关的维度类
 * 
 * @author 马士兵教育
 *
 */
public class StatsLocationDimension extends StatsDimension {
    private StatsCommonDimension statsCommon = new StatsCommonDimension();
    private LocationDimension location = new LocationDimension();

    /**
     * 根据现有的location对象复制一个
     * 
     * @param dimension
     * @return
     */
    public static StatsLocationDimension clone(StatsLocationDimension dimension) {
        StatsLocationDimension newDimesnion = new StatsLocationDimension();
        newDimesnion.statsCommon = StatsCommonDimension.clone(dimension.statsCommon);
        newDimesnion.location = LocationDimension.newInstance(dimension.location.getCountry(), dimension.location.getProvince(), dimension.location.getCity());
        newDimesnion.location.setId(dimension.location.getId());
        return newDimesnion;
    }

    public StatsLocationDimension() {
        super();
    }

    public StatsLocationDimension(StatsCommonDimension statsCommon, LocationDimension location) {
        super();
        this.statsCommon = statsCommon;
        this.location = location;
    }

    public StatsCommonDimension getStatsCommon() {
        return statsCommon;
    }

    public void setStatsCommon(StatsCommonDimension statsCommon) {
        this.statsCommon = statsCommon;
    }

    public LocationDimension getLocation() {
        return location;
    }

    public void setLocation(LocationDimension location) {
        this.location = location;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        this.statsCommon.write(out);
        this.location.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.statsCommon.readFields(in);
        this.location.readFields(in);
    }

    @Override
    public int compareTo(BaseDimension o) {
        StatsLocationDimension other = (StatsLocationDimension) o;
        int tmp = this.statsCommon.compareTo(other.statsCommon);
        if (tmp != 0) {
            return tmp;
        }
        tmp = this.location.compareTo(other.location);
        return tmp;
    }

}
