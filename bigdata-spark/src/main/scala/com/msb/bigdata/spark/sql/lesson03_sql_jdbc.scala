package com.msb.bigdata.spark.sql

import java.util.Properties

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object lesson03_sql_jdbc {

  def main(args: Array[String]): Unit = {


    val ss: SparkSession = SparkSession
      .builder()
      .appName("sdfsf")
      .master("local")
      .config("spark.sql.shuffle.partitions", "1")  //默认会有200并行度的参数  ，时shuffle
      .getOrCreate()
    val sc: SparkContext = ss.sparkContext
    sc.setLogLevel("INFO")

    val pro = new Properties ()
    pro.put("url","jdbc:mysql://192.168.150.99/spark")
    pro.put("user","root")
    pro.put("password","hadoop")
    pro.put("driver","com.mysql.jdbc.Driver")


    val usersDF: DataFrame = ss.read.jdbc(pro.get("url").toString,"users",pro)
    val scoreDF: DataFrame = ss.read.jdbc(pro.get("url").toString,"score",pro)

    usersDF.createTempView("userstab")
    scoreDF.createTempView("scoretab")

    val resDF: DataFrame = ss.sql("select  userstab.id,userstab.name,userstab.age, scoretab.score   from    userstab join scoretab on userstab.id = scoretab.id")
    resDF.show()
    resDF.printSchema()

//    println(resDF.rdd.partitions.length)
//    val resDF01: Dataset[Row] = resDF.coalesce(1)
//    println(resDF01.rdd.partitions.length)

    resDF.write.jdbc(pro.get("url").toString,"bbbb",pro)












    //什么数据源拿到的都是DS/DF
//    val jdbcDF: DataFrame = ss.read.jdbc(pro.get("url").toString,"ooxx",pro)
//    jdbcDF.show()
//
//    jdbcDF.createTempView("ooxx")
//
//    ss.sql(" select * from ooxx  ").show()
//
//
//
//    jdbcDF.write.jdbc(pro.get("url").toString,"xxxx",pro)




  }








}
