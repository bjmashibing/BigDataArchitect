package com.msb.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

object lesson06_rdd_over {

  def main(args: Array[String]): Unit = {

    //综合应用算子
    //topN   分组取TopN  （二次排序）
    //2019-6-1	39
    //同月份中 温度最高的2天

    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("topN")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")


    val data: RDD[String] = sc.parallelize(List(
      "hello world",
      "hello spark",
      "hello world",
      "hello hadoop",
      "hello world",
      "hello msb",
      "hello world"
    ))
    val words: RDD[String] = data.flatMap(_.split(" "))
    val kv: RDD[(String, Int)] = words.map((_,1))
      val res: RDD[(String, Int)] = kv.reduceByKey(_+_)
//      val res01: RDD[(String, Int)] = res.map(x=>(x._1,x._2*10))
    val res01: RDD[(String, Int)] = res.mapValues(x=>x*10)
    val res02: RDD[(String, Iterable[Int])] = res01.groupByKey()
    res02.foreach(println)













//
//    implicit val sdfsdf = new Ordering[(Int, Int)] {
//              override def compare(x: (Int, Int), y: (Int, Int)) = y._2.compareTo(x._2)
//            }
//
//    val file: RDD[String] = sc.textFile("bigdata-spark/data/tqdata")
//    //2019-6-1	39
//    val data = file.map(line => line.split("\t")).map(arr => {
//      val arrs: Array[String] = arr(0).split("-")
//      //(year,month,day,wd)
//      (arrs(0).toInt, arrs(1).toInt, arrs(2).toInt, arr(1).toInt)
//    })

    //分组    取topN  (排序)

    //第五代
    //分布式计算的核心思想：调优天下无敌：combineByKey
    //分布式是并行的，离线批量计算有个特征就是后续步骤(stage)依赖其一步骤(stage)
    //如果前一步骤(stage)能够加上正确的combineByKey
    //我们自定的combineByKey的函数，是尽量压缩内存中的数据

//    val kv: RDD[((Int, Int), (Int, Int))] = data.map(t4=>((t4._1,t4._2),(t4._3,t4._4)))
//
//    val res: RDD[((Int, Int), Array[(Int, Int)])] = kv.combineByKey(
//
//      //第一条记录怎么放：
//      (v1: (Int, Int)) => {
//        Array(v1, (0, 0), (0, 0))
//      },
//      //第二条，以及后续的怎么放：
//      (oldv: Array[(Int, Int)], newv: (Int, Int)) => {
//        //去重，排序
//        var flg = 0 //  0,1,2 新进来的元素特征：  日 a)相同  1）温度大 2）温度小   日 b)不同
//
//        for (i <- 0 until oldv.length) {
//          if (oldv(i)._1 == newv._1) {
//            if (oldv(i)._2 < newv._2) {
//              flg = 1
//              oldv(i) = newv
//            } else {
//              flg = 2
//            }
//          }
//        }
//        if (flg == 0) {
//          oldv(oldv.length - 1) = newv
//        }
//
////        oldv.sorted
//        scala.util.Sorting.quickSort(oldv)
//        oldv
//
//      },
//      (v1: Array[(Int, Int)], v2: Array[(Int, Int)]) => {
//        //关注去重
//        val union: Array[(Int, Int)] = v1.union(v2)
//        union.sorted
//      }
//
//    )
//    res.map(x=>(x._1,x._2.toList)).foreach(println)





    //第四代
    //  用了groupByKey  容易OOM  取巧：用了spark 的RDD  sortByKey 排序  没有破坏多级shuffle的key的子集关系
//    val sorted: RDD[(Int, Int, Int, Int)] = data.sortBy(t4=>(t4._1,t4._2,t4._4),false)
//    val grouped: RDD[((Int, Int), Iterable[(Int, Int)])] = sorted.map(t4=>((t4._1,t4._2),(t4._3,t4._4))).groupByKey()
//    grouped.foreach(println)


    //第三代
    //  用了groupByKey  容易OOM  取巧：用了spark 的RDD 的reduceByKey 去重，用了sortByKey 排序
    //  注意：多级shuffle关注  后续书法的key一定得是前置rdd  key的子集
    //    val sorted: RDD[(Int, Int, Int, Int)] = data.sortBy(t4=>(t4._1,t4._2,t4._4),false)
    //    val reduced: RDD[((Int, Int, Int), Int)] = sorted.map(t4=>((t4._1,t4._2,t4._3),t4._4)).reduceByKey(  (x:Int,y:Int)=>if(y>x) y else x )
//    val maped: RDD[((Int, Int), (Int, Int))] = reduced.map(t2=>((t2._1._1,t2._1._2),(t2._1._3,t2._2)))
//    val grouped: RDD[((Int, Int), Iterable[(Int, Int)])] = maped.groupByKey()
//    grouped.foreach(println)







    //第二代
    //用了groupByKey  容易OOM  取巧：spark rdd  reduceByKey 的取 max间接达到去重  让自己的算子变动简单点
//    val reduced: RDD[((Int, Int, Int), Int)] = data.map(t4=>((t4._1,t4._2,t4._3),t4._4)).reduceByKey((x:Int,y:Int)=>if(y>x) y else x )
//    val maped: RDD[((Int, Int), (Int, Int))] = reduced.map(t2=>((t2._1._1,t2._1._2),(t2._1._3,t2._2)))
//    val grouped: RDD[((Int, Int), Iterable[(Int, Int)])] = maped.groupByKey()
//    grouped.mapValues(arr=>arr.toList.sorted.take(2)).foreach(println)
//




//  第一代
    //  用了groupByKey 容易OOM   且自己的算子实现了函数：去重、排序
//    val grouped = data.map(t4=>((t4._1,t4._2),(t4._3,t4._4))).groupByKey()
//    val res: RDD[((Int, Int), List[(Int, Int)])] = grouped.mapValues(arr => {
//      val map = new mutable.HashMap[Int, Int]()
//      arr.foreach(x => {
//        if (map.get(x._1).getOrElse(0) < x._2) map.put(x._1, x._2)
//      })
//      map.toList.sorted(new Ordering[(Int, Int)] {
//        override def compare(x: (Int, Int), y: (Int, Int)) = y._2.compareTo(x._2)
//      })
//    })
//    res.foreach(println)


    while(true){

    }








  }







}
