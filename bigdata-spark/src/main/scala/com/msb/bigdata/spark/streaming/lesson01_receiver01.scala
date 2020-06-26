package com.msb.bigdata.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object lesson01_receiver01 {


  def main(args: Array[String]): Unit = {


    val conf: SparkConf = new SparkConf().setAppName("sdfdsf").setMaster("local[9]")
    //local[n]  2个就够了：
    // 1个给receiverjob的task，
    // 另一个给beatch计算的job（只不过如果batch比较大，你期望n>2,因为多出来的线程可以跑并行的batch@job@task）



    //微批的流式计算，时间去定义批次 （while->时间间隔触发job）
    val ssc = new StreamingContext(conf,Seconds(5))
    ssc.sparkContext.setLogLevel("ERROR")

    val dataDStream: ReceiverInputDStream[String] = ssc.socketTextStream("localhost",8889)
//    //hello world
//    val flatDStream: DStream[String] = dataDStream.flatMap(_.split(" "))
//    val resDStream: DStream[(String, Int)] = flatDStream.map((_,1)).reduceByKey(_+_)
//    resDStream.print()  //输出算子

    val res: DStream[(String, String)] = dataDStream.map(_.split(" ")).map(vars=>{
      Thread.sleep(20000)
      (vars(0),vars(1))
    })

    res.print()


    ssc.start()
    ssc.awaitTermination()







  }


}
