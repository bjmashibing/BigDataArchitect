package com.msb.bigdata.spark.streaming

import java.time.Duration
import java.util
import java.util.Properties
import java.util.regex.Pattern

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRebalanceListener, ConsumerRecord, ConsumerRecords, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

object lesson04_kafka_consumer {

  /**
   * kafka-consumer
   * 1，自动维护offset：ENABLE_AUTO_COMMIT_CONFIG  true   poll数据之后先去写offset，在去计算，会有丢失数据
   * 2，手动维护offset：ENABLE_AUTO_COMMIT_CONFIG  false
   *    a)维护到kafka自己的__consumer_offset_   这个topic中  且你还能通过  kafka-consumer-groups.sh  查看
   *    b)维护到其他的位置：mysql，zk
   *      *)牵扯到通过：ConsumerRebalanceListener  onPartitionsAssigned 方法回调后 自己seek到查询的位置
   *3，AUTO_OFFSET_RESET_CONFIG   自适应  必须参考  __consumer_offset_维护的
   *        //earliest    按   CURRENT-OFFSET   特殊状态：  group第一创建的时候，0
   *        // latest     按   LOG-END-OFFSET
   *4，seek覆盖性最强的
   *
   *
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val pros = new Properties()
    pros.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"node04:9092")
    pros.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,classOf[StringDeserializer])
    pros.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,classOf[StringDeserializer])

    pros.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest")
    //earliest    按   CURRENT-OFFSET   特殊状态：  group第一创建的时候，0
    // latest     按   LOG-END-OFFSET

    pros.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"FALSE")

    pros.put(ConsumerConfig.GROUP_ID_CONFIG,"bula5")

//    pros.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,"1")


    val consumer = new KafkaConsumer[String,String](pros)

    consumer.subscribe(Pattern.compile("ooxx"), new ConsumerRebalanceListener {
      override def onPartitionsRevoked(partitions: util.Collection[TopicPartition]): Unit = {
        println(s"onPartitionsRevoked")
        val iter: util.Iterator[TopicPartition] = partitions.iterator()
        while(iter.hasNext){
          println(iter.next())
        }
      }

      override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = {
        println(s"onPartitionsAssigned.........")

        val iter: util.Iterator[TopicPartition] = partitions.iterator()

        while(iter.hasNext){
          println(iter.next())
        }

        //调用了一个数据库
        consumer.seek(new TopicPartition("ooxx",1),46446)


        Thread.sleep(5000)
      }
    })

//    consumer.assign()

    val offMap = new util.HashMap[TopicPartition,OffsetAndMetadata]()
    var record: ConsumerRecord[String, String] = null;

    while(true){
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(0))

      if(!records.isEmpty){
        println(s"------------${records.count()}-------------------")

        val iter: util.Iterator[ConsumerRecord[String, String]] = records.iterator()
        while(iter.hasNext){
          record = iter.next()
          val topic: String = record.topic()
          val partition: Int = record.partition()
          val offset = record.offset()

          val key: String = record.key()
          val value: String = record.value()
          println(s"key: $key  value: $value  partition: $partition  offset: $offset ")


        }

        //手动维护offset有2类地方：  防止丢失数据！
        // 1)手动维护到kafka
        //2）zk，mysql。。。。
        val partition = new TopicPartition("ooxx",record.partition())
        val offset = new OffsetAndMetadata(record.offset())

        offMap.put(partition,offset)
        consumer.commitSync(offMap)
        //如果在运行时：手动提交offset到mysql  -->  那么：
        //一单程序重启，可以通过ConsumerRebalanceListener协商后获得的分区，去mysql查询该分区上次消费记录的位置

      }
    }



  }

}
