package com.msb.bigdata.hadoop.topn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MyTopN {


    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration(true);

        conf.set("mapreduce.framework.name","local");
        conf.set("mapreduce.app-submission.cross-platform","true");


        String[] other = new GenericOptionsParser(conf, args).getRemainingArgs();

        Job  job = Job.getInstance(conf);
        job.setJarByClass(MyTopN.class);

        job.setJobName("TopN");

        job.setJar("C:\\Users\\Administrator\\IdeaProjects\\msbhadoop\\target\\hadoop-hdfs-1.0-0.1.jar");
        //客户端规划的时候讲join的右表cache到mapTask出现的节点上
        job.addCacheFile(new Path("/data/topn/dict/dict.txt").toUri());





        //初学者，关注的是client端的代码梳理：因为把这块写明白了，其实你也就真的知道这个作业的开发原理；

        //maptask
        //input

        TextInputFormat.addInputPath(job,new Path(other[0]));

        Path outPath = new Path(other[1]);

        if(outPath.getFileSystem(conf).exists(outPath))  outPath.getFileSystem(conf).delete(outPath,true);
        TextOutputFormat.setOutputPath(job,outPath);


        //key
        //map
        job.setMapperClass(TMapper.class);
        job.setMapOutputKeyClass(TKey.class);
        job.setMapOutputValueClass(IntWritable.class);

        //partitioner  按  年，月  分区  -》  分区 > 分组  按 年分区！！！！！！
            //分区器潜台词：满足  相同的key获得相同的分区号就可以~！
        job.setPartitionerClass(TPartitioner.class);
        //sortComparator  年，月，温度  且  温度倒序
        job.setSortComparatorClass(TSortComparator.class);
        //combine
//        job.setCombinerClass();




        //reducetask
        //groupingComparator
        job.setGroupingComparatorClass(TGroupingComparator.class);
        //reduce
        job.setReducerClass(TReducer.class);

        job.waitForCompletion(true);



    }


}
