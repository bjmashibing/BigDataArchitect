package com.msb.bigdata.spark

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

object lesson08_other {


  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("contrl").setMaster("local")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")


//    val data: RDD[Int] = sc.parallelize(1 to 10,2)
//
//
//    var n = 0
//    val ox: LongAccumulator = sc.longAccumulator("ooxx")
//
//
//    val count: Long = data.map(x => {
//
//      if(x%2==0) ox.add(1)  else ox.add(100)
//      n+=1
//      println(s"executor:n: $n")
//      x
//    }).count()
//
//    println(s"count:  $count")
//    println(s"Driver:n: $n")
//    println(s"Driver:ox:${ox}")
//    println(s"Driver:ox.avg:${ox.avg}")
//    println(s"Driver:ox.sum:${ox.sum}")
//    println(s"Driver:ox.count:${ox.count}")










    val data: RDD[String] = sc.parallelize(List(
      "hello world",
      "hello spark",
      "hi world",
      "hello msb",
      "hello world",
      "hello hadoop"
    ))



    val data1: RDD[String] = data.flatMap(_.split(" "))

    val list: Array[String] = data1.map((_,1)).reduceByKey(_+_).sortBy(_._2,false).keys.take(2)
    //推送出去到executor执行，然后结果回收回Driver端


//    val list = List("hello","world")//有的时候是未知的？

    val blist: Broadcast[Array[String]] = sc.broadcast(list)  //第一次见到broadcast是什么时候  taskbinery

    val res: RDD[String] = data1.filter(    x=>  blist.value.contains(x)     )   //闭包 发生在Driver，执行发送到executor <--想闭包进函数，必须实现了序列化
    res.foreach(println)

    /**
     * 广播变量也好，还是直接闭包数据
     * 垂直join，map端join
     *
     *
     */




  }






}
