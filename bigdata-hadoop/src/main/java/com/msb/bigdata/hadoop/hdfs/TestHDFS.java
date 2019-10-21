package com.msb.bigdata.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class TestHDFS {

    public Configuration conf = null;
    public FileSystem fs = null;

    //C/S
    @Before
    public void conn() throws Exception {
        conf = new Configuration(true);//true
//        fs = FileSystem.get(conf);
//       <property>
//        <name>fs.defaultFS</name>
//        <value>hdfs://mycluster</value>
//       </property>
        //去环境变量 HADOOP_USER_NAME  god
        fs = FileSystem.get(URI.create("hdfs://mycluster/"),conf,"god");
    }

    @Test
    public void mkdir() throws Exception {

        Path dir = new Path("/msb01");
        if(fs.exists(dir)){
            fs.delete(dir,true);
        }
        fs.mkdirs(dir);

    }

    @Test
    public void upload() throws Exception {

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(new File("./data/hello.txt")));
        Path outfile   = new Path("/msb/out.txt");
        FSDataOutputStream output = fs.create(outfile);

        IOUtils.copyBytes(input,output,conf,true);
    }

    @Test
    public void blocks() throws Exception {

        Path file = new Path("/user/god/data.txt");
        FileStatus fss = fs.getFileStatus(file);
        BlockLocation[] blks = fs.getFileBlockLocations(fss, 0, fss.getLen());
        for (BlockLocation b : blks) {
            System.out.println(b);
        }
//        0,        1048576,        node04,node02  A
//        1048576,  540319,         node04,node03  B
        //计算向数据移动~！
        //其实用户和程序读取的是文件这个级别~！并不知道有块的概念~！
        FSDataInputStream in = fs.open(file);  //面向文件打开的输入流  无论怎么读都是从文件开始读起~！

//        blk01: he
//        blk02: llo msb 66231

        in.seek(1048576);
        //计算向数据移动后，期望的是分治，只读取自己关心（通过seek实现），同时，具备距离的概念（优先和本地的DN获取数据--框架的默认机制）
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
        System.out.println((char)in.readByte());
    }











    @After
    public void close() throws Exception {
        fs.close();
    }

}
