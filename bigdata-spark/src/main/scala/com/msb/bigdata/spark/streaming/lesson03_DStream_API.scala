package com.msb.bigdata.spark.streaming

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, MapWithStateDStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Duration, Seconds, State, StateSpec, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object lesson03_DStream_API {

  def main(args: Array[String]): Unit = {


    //spark streaming  100ms batch    1ms
    //low level api
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName("testAPI")
    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")
    sc.setCheckpointDir(".")
//    sc.setCheckpointDir("hdfs://mycluster/ooxx/spark/sdfs")
    val ssc = new StreamingContext(sc,Duration(1000))  //最小粒度  约等于：  win：  1000   slide：1000



    //有状态计算
    //状态<-  历史数据  join、关联  历史的计算要存下来，当前的计算最后还要合到历史数据里
    //  持久化下来 历史的数据状态
    //persist    blockmanager  速度快  可靠性差
    // checkpoin    外界系统   成本高  可靠性好
    //persist  调用后  再做 checkpoin  =>数据会在2个地方都存储

    val data: ReceiverInputDStream[String] = ssc.socketTextStream("localhost",8889)
    val mapdata: DStream[(String, Int)] = data.map(_.split(" ")).map(x=>(x(0),1))
//    mapdata.reduceByKey(_+_)
//    val res: DStream[(String, Int)] = mapdata.updateStateByKey(
//      (nv: Seq[Int], ov: Option[Int]) => {
//
//        println("....updata...fun")
//        //每个批次的job里  对着nv求和
//        val count: Int = nv.count(_ > 0)
//        val oldVal: Int = ov.getOrElse(0)
//        Some(count + oldVal)
//
//      })


    val res: MapWithStateDStream[String, Int, Int, (String, Int)] = mapdata.mapWithState(StateSpec.function(

      (k: String, nv: Option[Int], ov: State[Int]) => {

        println(s"*************k:$k  nv:${nv.getOrElse(0)}   ov ${ov.getOption().getOrElse(0)}")
        (k, nv.getOrElse(0) + ov.getOption().getOrElse(0))

      }


    ))



    res.print()






//    Seconds(1)

//    val rdd: RDD[Int] = sc.parallelize( 1 to 10)

//    while(true){
//
//      rdd.map(x=>{
//        println("sfsdfsdf")
//        x
//      })
//      Thread.sleep(1000)
//    }


    /**
     * 1.需求：将计算延缓
     * 2.一个数据源，要保证，1秒级的数据频率和5秒级的输出频率
     * 3.而且，是在数据输出的时候计算输出时间点的历史数据
     *
     * *.数据源是1s中一个hello 2 个hi
     */

      //这个数据源的粗粒度： 1s  来自于 StreamContext
//    val resource: ReceiverInputDStream[String] = ssc.socketTextStream("localhost",8889)

    /*
    hello 1
    hi 1
    hi1

    hello 2
    hi 2
    hi 2
     */
//    val format: DStream[(String, Int)] = resource.map(_.split(" ")).map(x=>(x(0),1))

    /*
    hello 1
    hi 1
    hi 1

    hello 1
    hi 1
    hi 1

     */



//    format.window(Duration(3000),Duration(2000))

    //-------------------------------------DSteam 转换到RDD 及 代码作用域-----------------------------------------------

    /*
    转换到RDD的操作
    有2中途径
    重点是作用域
     */

    /**
     * 作用域分为三个级别：
     * application
     * job
     * rdd：task
     *
     * RDD是一个单向链表
     * DStream也是一个单向链表
     * 如果把最后一个DStream给SSC
     * 那么ssc可以启动一个独立的线程无while(true){最后一个DStream遍历 ； }
     */

    //广播变量：


//    val bc: Broadcast[List[Int]] = sc.broadcast((1 to 5).toList) //application
//    var bc:Broadcast[List[Int]] = null
//    var jobNum = 0 //怎么能令jobNum的值随着job的提交执行，递增
////    val res: DStream[(String, Int)] = format.filter(x=>{bc.value.contains(x._2)})
//    println("aaaaaaa")  //application
//
//    val res: DStream[(String, Int)] = format.transform(
//
//      rdd => {
//        jobNum +=1  //每job级别递增 是在ssc的另一个while（true）线程里，Driver端执行的
//        println(s"jobNum: $jobNum")
//        if(jobNum <=5){
//          bc =  sc.broadcast((1 to 5).toList)
//        }else{
//          bc =  sc.broadcast((6 to 15).toList)
//
//        }
//        rdd.filter(x=>bc.value.contains(x._2)) //无论多少次job的运行都是相同的bc  只有rdd接受的函数，才是executor端的，才是task端的
//      }
//    )
//    res.print()

//    val res: DStream[(String, Int)] = format.transform( //每job调用一次
//      (rdd) => {
//        //我们的函数式每job级别的
//        println("bbbbbbbb")   //job
//        rdd.map(x=>{
//          println("cccccc")  //task
//          x
//        })
//      }
//    )
//    res

//    res.print()



    //末端处理
//    format.foreachRDD(    //StreamingContext  有一个独立的线程  执行while（true)  ,你在主线程中写下的代码是放到执行线程去执行
//      (rdd)=>{
//        rdd.foreach(x=>{
//          x...to redis
//          to mysql
//          call webservice
//        })
//      }
//    )

    //transform  中途加工
//    val res: DStream[(String, Int)] = format.transform( //硬性要求：  返回值是RDD
//      ( rdd  )  =>{
//
//        rdd.foreach(println);  //产生job
//        val rddres: RDD[(String, Int)] = rdd.map(x => (x._1, x._2 * 10))//只是在做转换
//
//        rddres
//
//      }
//    )
//
//    res.print()





    //-------------------------------------window  api-----------------------------------------------

    /**
     * 总结一下，其实一直有窗口的概念，默认，val ssc = new StreamingContext(sc,Duration(1000))  //最小粒度  约等于：  win：  1000   slide：1000
     */
    //每秒中看到历史5秒的统计
//    val res: DStream[(String, Int)] = format.reduceByKey(_+_)
//    val res: DStream[(String, Int)] = format.window(Duration(5000)).reduceByKey(_+_)

//    val reduce: DStream[(String, Int)] = format.reduceByKey(_+_)   //  窗口量是  1000  slide  1000
//    val res: DStream[(String, Int)] = reduce.window(Duration(5000))

//    val win: DStream[(String, Int)] = format.window(Duration(5000))  //先调整量
//    val res: DStream[(String, Int)] = win.reduceByKey(_+_)  //在基于上一步的量上整体发生计算

//    val res: DStream[(String, Int)] = format.reduceByKeyAndWindow(_+_,Duration(5000))

//    val resource: ReceiverInputDStream[String] = ssc.socketTextStream("localhost",8889)
//    val format: DStream[(String, Int)] = resource.map(_.split(" ")).map(x=>(x(0),x(1).toInt))
//    //调优：
//    //reduceByKey 对  combinationBykey的封装  放入函数，聚合函数，combina函数
//    val res: DStream[(String, Int)] = format.reduceByKeyAndWindow(
//      //计算新进入的batch的数据
//      (ov:Int,nv:Int)=>{
//        println("first fun......")
//        println(s"ov:$ov  nv:$nv")
//
//        ov+nv
//    }
//
//      ,
//      //挤出去的batch的数据
//      (ov:Int,oov:Int)=>{
////        println("di 2 ge fun.....")
////        println(s"ov:$ov   oov:$oov")
//        ov-oov
//      }
//      ,
//      Duration(6000),Duration(2000)
//    )
//
//    res.print()
//
//
//
//    res.print()

//    format.print()

//    val res1s1batch: DStream[(String, Int)] = format.reduceByKey(_+_)
////    res1s1batch.mapPartitions(iter=>{println("1s");iter}).print()//打印的频率：1秒打印1次
//
//
//    val newDS: DStream[(String, Int)] = format.window(Duration(5000),Duration(5000))
//
//    val res5s5batch: DStream[(String, Int)] = newDS.reduceByKey(_+_)
//    res5s5batch.mapPartitions(iter=>{println("5s");iter}).print() //打印频率：1秒打印1次


    ssc.start()
    ssc.awaitTermination()



  }








}
