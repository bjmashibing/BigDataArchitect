package com.msb.bigdata.spark.streaming

import java.util

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, OffsetAndMetadata, OffsetCommitCallback}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies, OffsetRange}
import org.apache.spark.streaming.{Duration, StreamingContext}

object lesson05_spark_kafka_consumer{


  def main(args: Array[String]): Unit = {
    //写一个spark  streaming  on  kafka

    val conf: SparkConf = new SparkConf().setMaster("local[8]").setAppName("kafka01")
    conf.set("spark.streaming.backpressure.enabled","true")
    conf.set("spark.streaming.kafka.maxRatePerPartition","2")  //运行时状态
//    conf.set("spark.streaming.backpressure.initialRate","2")  //起步状态
    conf.set("spark.streaming.stopGracefullyOnShutdown","true")

    val ssc = new StreamingContext(conf,Duration(1000))

    ssc.sparkContext.setLogLevel("ERROR")
    //如何得到kakfa的DStream
    val map: Map[String, Object] = Map[String, Object](
      (ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "node04:9092"),
      (ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer]),
      (ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer]),
      (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
      //earliest    按   CURRENT-OFFSET   特殊状态：  group第一创建的时候，0
      // latest     按   LOG-END-OFFSET
      (ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),  //需要手动维护offset 1，kafka    2，第三方
      (ConsumerConfig.GROUP_ID_CONFIG, "BULA666")
//      (ConsumerConfig.MAX_POLL_RECORDS_CONFIG,"1")
    )


    /**
     * 访问数据库
     * 取曾经持久化的offset
     *
     */
//    val mapsql = Map[TopicPartition,Long](
//      (new TopicPartition("from mysql topic",0),33),
//      (new TopicPartition("from mysql topic",1),32)
//    )

    val kafka: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](List("ooxx"), map)

//      ConsumerStrategies.Subscribe[String, String](List("ooxx"), map,mapsql)
    )


    //  1，offset怎么来？   2，对着kafka提交offset的api哪里来？
    //  罪魁祸首即使第一个通过kafkautils创建的DStream  它自己提供的提交API，它内部包含的RDD提供了offset
    val dstream: DStream[(String, (String, String, Int, Long))] = kafka.map(
      record => {
        val t: String = record.topic()
        val p: Int = record.partition()
        val o: Long = record.offset()
        val k: String = record.key()
        val v: String = record.value()


        (k, (v, t, p, o))
      }
    )
    dstream.print()

    //完成了业务代码后

    //维护offset是为了什么，哪个时间点用起你维护的offset？：application重启的时候，driver重启的时候
    //维护offset的另一个语义是什么：持久化

    var ranges: Array[OffsetRange] = null;
    //正确的，讲提交offset的代码放到dstream对象的接受函数里，那么未来在调度线程里，这个函数每个job有机会调用一次，伴随着，提交offset
    kafka.foreachRDD(
      rdd=>{
        //driver端可以拿到offset
        println(s"foreachRDD..fun.......")
        ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        //闭包,通过kafkautils得到的第一个DStream向上转型，提交offset
        //1，维护/持久化offset到kafka
        kafka.asInstanceOf[CanCommitOffsets].commitAsync(ranges,new OffsetCommitCallback {
          override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit = {

            if(offsets != null){

              ranges.foreach(println)
              println("--------------")
              val iter: util.Iterator[TopicPartition] = offsets.keySet().iterator()
              while(iter.hasNext){
                val k: TopicPartition = iter.next()
                val v: OffsetAndMetadata = offsets.get(k)
                println(s"${k.partition()}...${v.offset()}")
              }



            }
                    }
        })
        //2，维护/持久化到  mysql   异步维护   同步
        //同步：
        val local: Array[(String, String)] = rdd.map(r=>(r.key(),r.value())).reduceByKey(_+_).collect()

        /**
         * 开启事务
         * 提交数据
         * 提交offset
         * commit
         */
      }
    )











//      kafka.foreachRDD(
//      rdd=>{
//        //driver端可以拿到offset
//        println(s"foreachRDD..fun.......")
//        ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//        //闭包,通过kafkautils得到的第一个DStream向上转型，提交offset
//        kafka.asInstanceOf[CanCommitOffsets].commitAsync(ranges)
//      }
//    )


    //因为提交offset代码写到了main线程里，其实没有起到作用
//    kafka.foreachRDD(
//      rdd=>{
//        //driver端可以拿到offset
//        println(s"foreachRDD..fun.......")
//        ranges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
//        //闭包,通过kafkautils得到的第一个DStream向上转型，提交offset
////        kafka.asInstanceOf[CanCommitOffsets].commitAsync(ranges)
//      }
//    )




   //通过KafkaUtils得到的第一个DStream要先去转换一下，其实这个DStream就是 consumer@poll回来的records
//    //将 record转换成业务逻辑的元素：只提取出key，value
//    val dstream: DStream[(String, (String, String, Int, Long))] = kafka.map(
//      record => {
//        val t: String = record.topic()
//        val p: Int = record.partition()
//        val o: Long = record.offset()
//        val k: String = record.key()
//        val v: String = record.value()
//
//
//        (k, (v, t, p, o))
//      }
//    )
//    dstream.print()




    ssc.start()

//    Thread.sleep(1000)
//    kafka.asInstanceOf[CanCommitOffsets].commitAsync(ranges)

    ssc.awaitTermination()



















  }
}
