package com.msb.bigdata.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, Encoders, SparkSession}

object haha {


  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("test")
    val session: SparkSession = SparkSession
      .builder()
      .config(conf)
      //      .appName("test")
      //      .master("local")
      //      .enableHiveSupport()   //开启这个选项时  spark sql on  hive  才支持DDL，没开启，spark只有catalog
      .getOrCreate()

    val ds01: Dataset[String] = session.read.textFile("./bigdata-spark/data/person.txt")

    val res= ds01.map(
      line => {
        val strs: Array[String] = line.split(" ")

        (strs(0), strs(1).toInt)

      }
    )(Encoders.tuple(Encoders.STRING, Encoders.scalaInt))
    res.show()







  }

}
