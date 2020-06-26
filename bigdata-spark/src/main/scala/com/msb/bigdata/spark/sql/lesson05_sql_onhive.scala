package com.msb.bigdata.spark.sql

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}

object lesson05_sql_onhive {


  def main(args: Array[String]): Unit = {


    val ss: SparkSession = SparkSession
      .builder()
      .appName("test on hive ")
      .master("local")
      .config("hive.metastore.uris", "thrift://node01:9083")
      .enableHiveSupport()
      .getOrCreate()
    val sc: SparkContext = ss.sparkContext

    sc.setLogLevel("ERROR")

    import ss.implicits._

    val df01: DataFrame = List(
      "zhangsan",
      "lisi"
    ).toDF("name")
    df01.createTempView("ooxx")

//    ss.sql("create table xxoo ( id int)")  //DDL

    ss.sql("insert into xxoo values (3),(6),(7)")  //DML  数据是通过spark自己和hdfs进行访问
    df01.write.saveAsTable("oxox")





    ss.catalog.listTables().show()  //能不能看到表？

    //如果没有hive的时候，表最开始一定是  DataSet/DataFrame
//    ss.sql("use default")
    val df: DataFrame = ss.sql("show tables")
    df.show()






  }

}
