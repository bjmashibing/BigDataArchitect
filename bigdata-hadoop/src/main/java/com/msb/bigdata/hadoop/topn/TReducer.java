package com.msb.bigdata.hadoop.topn;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.Iterator;

public class TReducer extends Reducer <TKey, IntWritable, Text,IntWritable>{


    Text rkey = new Text();
    IntWritable rval = new IntWritable();
    @Override
    protected void reduce(TKey key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
        // 1970-6-4 33   33
        // 1970-6-4 32   32
        // 1970-6-22 31   31
        // 1970-6-4 22   22
        //问题：  对着value进行迭代，，key会不会变？
        //程序开发，方法传参：  值传递，引用传递

        Iterator<IntWritable> iter = values.iterator();

        int flg = 0 ;
        int day = 0;
        while(iter.hasNext()){
            IntWritable val = iter.next();  // -> context.nextKeyValue() ->  对key和value更新值！！！！

            if(flg == 0){
                rkey.set(key.getYear()+"-"+key.getMonth()+"-"+key.getDay()+"@"+key.getLocation());
                rval.set(key.getWd());
                context.write(rkey,rval);
                flg++;
                day = key.getDay();

            }

            if(flg != 0 &&  day != key.getDay()){
                rkey.set(key.getYear()+"-"+key.getMonth()+"-"+key.getDay()+"@"+key.getLocation());
                rval.set(key.getWd());
                context.write(rkey,rval);
                break;
            }



        }


    }
}
