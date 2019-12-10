package com.msb.bigdata.spark.sql

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

object lesson04_sql_standalone_hive {

  def main(args: Array[String]): Unit = {

    /**
     * 我们时学spark - sql  不会对【【【【hive】】】】进行更多的复习
     *
     */
    val ss: SparkSession = SparkSession
      .builder()
      .master("local")
      .appName("sdsfd")
      .config("spark.sql.shuffle.partitions", 1)
      .config("spark.sql.warehouse.dir", "d:/spark/warehouse")
      .enableHiveSupport()   //开启hive支持   ？   自己会启动hive的metastore
      .getOrCreate()
    val sc: SparkContext = ss.sparkContext
//    sc.setLogLevel("ERROR")

    import ss.sql


    /**
     * 一定要记住有数据库的概念~！！！！！！！！！！！！！！！！！！！！
     * use default
     * mysql  是个软件
     * 一个mysql可以创建很多的库 database 隔离的
     * so，公司只装一个mysql，不同的项目组，自己用自己的库  database
     *
     * spark/hive 一样的
     *
     *
     */
    //    ss.sql("create table xxx(name string,age int)")
//
//    ss.sql("insert into xxx values ('zhangsan',18),('lisi',22)")


//    sql("create database msb")
//    sql("create table msb.xxxx (name string,age int)")
//    sql("create table  xxxx (name string,age int)")

//    sql("insert into msb.xxxx values ('zhangsan',18),('lisi',22)")
//    sql("insert into  xxxx values ('zhangsan',18),('lisi',22)")


    ss.catalog.listTables().show()  //作用再current库

    sql("create database msb")
    sql("create table table01(name string)")  //作用再current库
    ss.catalog.listTables().show() //作用再current库

    println("--------------------------------")

    sql("use msb")
    ss.catalog.listTables().show() //作用再msb这个库

    sql("create table table02(name string)")  //作用再current库
    ss.catalog.listTables().show() //作用再msb这个库

    //    sql("select * from msb.xxxx").show()
//    sql("select * from  xxxx").show()


    //    sql("select * from xxx").show





  }






}
