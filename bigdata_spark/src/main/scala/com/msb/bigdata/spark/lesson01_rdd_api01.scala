package com.msb.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object lesson01_rdd_api01 {

//面向数据集操作：
    //*，带函数的非聚合：  map，flatmap
    //1，单元素：union，cartesion  没有函数计算
    //2，kv元素：cogroup，join   没有函数计算
    //3，排序
    //4，聚合计算  ： reduceByKey  有函数   combinerByKey

  //cogroup
  //combinerByKey



  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("test01")
    val sc = new SparkContext(conf)

    sc.setLogLevel("ERROR")




    val dataRDD: RDD[Int] = sc.parallelize( List(1,2,3,4,5,4,3,2,1) )


//    dataRDD.map()
//    dataRDD.flatMap()
//    dataRDD.filter((x:Int)=>{ x> 3})
    val filterRDD: RDD[Int] = dataRDD.filter(   _> 3    )
    val res01: Array[Int] = filterRDD.collect()
    res01.foreach(println)
    println("-----------------------")

    val res: RDD[Int] = dataRDD.map((_,1)).reduceByKey(_+_).map(_._1)
    res.foreach(println)

    val resx: RDD[Int] = dataRDD.distinct()
    resx.foreach(println)

    //面向数据集开发  面向数据集的API  1，基础API   2，复合API
    //RDD  （HadoopRDD,MappartitionsRDD,ShuffledRDD...）
    //map,flatMap,filter
    //distinct...
    //reduceByKey:  复合  ->  combineByKey（）


    //  面向数据集：  交并差  关联 笛卡尔积

    //面向数据集： 元素 -->  单元素，K,V元素  --> 机构化、非结构化






    //spark很人性，面向数据集提供了不同的方法的封装，且，方法已经经过经验，常识，推算出自己的实现方式
    //人不需要干预（会有一个算子）
//    val rdd1: RDD[Int] = sc.parallelize( List( 1,2,3,4,5)  )
//    val rdd2: RDD[Int] = sc.parallelize( List( 3,4,5,6,7)  )

//    //差集：提供了一个方法：  有方向的
//    val subtract: RDD[Int] = rdd1.subtract(rdd2)
//    subtract.foreach(println)

//    val intersection: RDD[Int] = rdd1.intersection(rdd2)
//    intersection.foreach(println)


//    //  如果数据，不需要区分每一条记录归属于那个分区。。。间接的，这样的数据不需要partitioner。。不需要shuffle
//    //因为shuffle的语义：洗牌  ---》面向每一条记录计算他的分区号
//    //如果有行为，不需要区分记录，本地IO拉去数据，那么这种直接IO一定比先Parti。。计算，shuffle落文件，最后在IO拉去速度快！！！
//    val cartesian: RDD[(Int, Int)] = rdd1.cartesian(rdd2)
//
//    cartesian.foreach(println)






//    println(rdd1.partitions.size)
//    println(rdd2.partitions.size)
//    val unitRDD: RDD[Int] = rdd1.union(rdd2)
//    println(unitRDD.partitions.size)
//
////    unitRDD.map(sdfsdf)
////    rdd1.map()
////    rdd2.map()
//
//
//    unitRDD.foreach(println)


    val kv1: RDD[(String, Int)] = sc.parallelize(List(
      ("zhangsan", 11),
      ("zhangsan", 12),
      ("lisi", 13),
      ("wangwu", 14)
    ))
     val kv2: RDD[(String, Int)] = sc.parallelize(List(
      ("zhangsan", 21),
      ("zhangsan", 22),
      ("lisi", 23),
      ("zhaoliu", 28)
    ))

    val cogroup: RDD[(String, (Iterable[Int], Iterable[Int]))] = kv1.cogroup(kv2)

    cogroup.foreach(println)


//    val join: RDD[(String, (Int, Int))] = kv1.join(kv2)
//
//    join.foreach(println)
//
//    val left: RDD[(String, (Int, Option[Int]))] = kv1.leftOuterJoin(kv2)
//
//    left.foreach(println)
//
//    val right: RDD[(String, (Option[Int], Int))] = kv1.rightOuterJoin(kv2)
//    right.foreach(println)
//
//    val full: RDD[(String, (Option[Int], Option[Int]))] = kv1.fullOuterJoin(kv2)
//    full.foreach(println)

    while(true){

    }



















  }

}
