package com.msb.bigdata.hadoop.topn;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class TGroupingComparator  extends WritableComparator {

    public  TGroupingComparator(){
        super(TKey.class,true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        TKey k1 = (TKey)a;
        TKey k2 = (TKey)b;
        //  按着 年，月分组
        int c1 = Integer.compare(k1.getYear(), k2.getYear());
        if(c1 == 0 ){
            return  Integer.compare(k1.getMonth(), k2.getMonth());

        }

        return c1;


    }


}
