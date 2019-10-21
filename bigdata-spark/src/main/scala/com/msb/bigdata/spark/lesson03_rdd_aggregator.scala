package com.msb.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object lesson03_rdd_aggregator {

  def main(args: Array[String]): Unit = {


    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("test")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val data: RDD[(String, Int)] = sc.parallelize(List(
      ("zhangsan", 234),
      ("zhangsan", 5667),
      ("zhangsan", 343),
      ("lisi", 212),
      ("lisi", 44),
      ("lisi", 33),
      ("wangwu", 535),
      ("wangwu", 22)
    ))

    //key  value->一组

    val group: RDD[(String, Iterable[Int])] = data.groupByKey()
    group.foreach(println)

    //  行列转换

    val res01: RDD[(String, Int)] = group.flatMap(  e =>   e._2.map(    x=>(e._1,x)    ) .iterator    )
    res01.foreach(println)

    println("--------------------")
    group.flatMapValues(e=> e.iterator).foreach(println)

    println("--------------------")

    group.mapValues( e => e.toList.sorted.take(2) ).foreach(println)
    println("--------------------")

    group.flatMapValues( e => e.toList.sorted.take(2) ).foreach(println)

    println("--------sum,count,min,max,avg------------")

    val sum: RDD[(String, Int)] = data.reduceByKey(_+_)
    val max: RDD[(String, Int)] = data.reduceByKey(  (ov,nv)=> if(ov>nv)  ov else nv     )
    val min: RDD[(String, Int)] = data.reduceByKey(  (ov,nv)=> if(ov<nv)  ov else nv     )
    val count: RDD[(String, Int)] = data.mapValues(e=>1).reduceByKey(_+_)
    val tmp: RDD[(String, (Int, Int))] = sum.join(count)
    val avg: RDD[(String, Int)] = tmp.mapValues(e=> e._1/e._2)
    println("--------sum------------")
    sum.foreach(println)
    println("--------max------------")
    max.foreach(println)
    println("--------min------------")
    min.foreach(println)
    println("--------count------------")
    count.foreach(println)
    println("--------avg------------")
    avg.foreach(println)

    println("--------avg-----combine-------")

    val tmpx: RDD[(String, (Int, Int))] = data.combineByKey(
      //      createCombiner: V => C,
      //第一条记录的 value  怎么放入 hashmap
      (value: Int) => (value, 1),
      //      mergeValue: (C, V) => C,
      //如果有第二条记录，第二条以及以后的他们的value怎么放到hashmap里：
      (oldValue: (Int, Int), newValue: Int) => (oldValue._1 + newValue, oldValue._2 + 1),
      //      mergeCombiners: (C, C) => C,
      //合并溢写结果的函数：
      (v1: (Int, Int), v2: (Int, Int)) => (v1._1 + v2._1, v1._2 + v2._2)
    )
    tmpx.mapValues(e=>e._1/e._2).foreach(println)
















    while(true){

    }


  }

}
