package com.msb.bigdata.hadoop.fof;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

import java.io.IOException;

public class FMapper extends Mapper<LongWritable, Text,Text, IntWritable> {


    Text mkey = new Text();
    IntWritable mval = new IntWritable();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //value:   马老师 一名老师 刚老师 周老师

        String[] strs = StringUtils.split(value.toString(), ' ');


        for (int i = 1; i < strs.length; i++) {
                  mkey.set(getFof(strs[0],strs[i]));
                  mval.set(0);
                  context.write(mkey,mval);
            for (int j = i+1; j < strs.length; j++) {
                mkey.set(getFof(strs[i],strs[j]));
                mval.set(1);
                context.write(mkey,mval);

            }

        }
    }

    public static String getFof(String s1,String s2){

        if(s1.compareTo(s2) > 0){
            return  s1+"-"+s2;
        }else{
            return  s2+"-"+s1;
        }

    }
}
