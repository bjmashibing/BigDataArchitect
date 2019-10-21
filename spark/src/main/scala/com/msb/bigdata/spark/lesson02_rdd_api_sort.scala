package com.msb.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object lesson02_rdd_api_sort {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("sort")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    //PV,UV
    //需求：根据数据计算各网站的PV,UV，同时，只显示top5
    //解题：要按PV值，或者UV值排序，取前5名

    val file: RDD[String] = sc.textFile("data/pvuvdata",5)

    //pv：
    //  187.144.73.116	浙江	2018-11-12	1542011090255	3079709729743411785	www.jd.com	Comment
    println("----------PV:-----------")

    val pair: RDD[(String, Int)] = file.map(line=>  (line.split("\t")(5),1)    )

    val reduce: RDD[(String, Int)] = pair.reduceByKey(_+_)
    val map: RDD[(Int, String)] = reduce.map(_.swap)
    val sorted: RDD[(Int, String)] = map.sortByKey(false)
    val res: RDD[(String, Int)] = sorted.map(_.swap)
    val pv: Array[(String, Int)] = res.take(5)
    pv.foreach(println)

    println("----------UV:-----------")

    //  187.144.73.116	浙江	2018-11-12	1542011090255	3079709729743411785	www.jd.com	Comment

    val keys: RDD[(String, String)] = file.map(
      line => {
        val strs: Array[String] = line.split("\t")
        (strs(5), strs(0))
      }
    )
    val key: RDD[(String, String)] = keys.distinct()
    val pairx: RDD[(String, Int)] = key.map(k => (k._1,1))
    val uvreduce: RDD[(String, Int)] = pairx.reduceByKey(_+_)
    val uvSorted: RDD[(String, Int)] = uvreduce.sortBy(_._2,false)
    val uv: Array[(String, Int)] = uvSorted.take(5)
    uv.foreach(println)




    while(true){

    }



  }

}
