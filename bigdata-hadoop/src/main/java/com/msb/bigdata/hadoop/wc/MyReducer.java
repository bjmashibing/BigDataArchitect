package com.msb.bigdata.hadoop.wc;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private IntWritable result = new IntWritable();

    //相同的key为一组 ，这一组数据调用一次reduce
    //hello 1
    //hello 1
    //hello 1
    //hello 1
    public void reduce(Text key, Iterable<IntWritable> values,/* 111111*/
                       Context context) throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        result.set(sum);
        context.write(key, result);
    }
}
