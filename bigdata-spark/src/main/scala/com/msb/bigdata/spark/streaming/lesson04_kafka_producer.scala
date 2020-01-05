package com.msb.bigdata.spark.streaming

import java.util.Properties
import java.util.concurrent.Future

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

object lesson04_kafka_producer {


  def main(args: Array[String]): Unit = {


    val pros = new Properties()
    pros.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"node02:9092,node03:9092,node04:9092")
    pros.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,classOf[StringSerializer])
    pros.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,classOf[StringSerializer])


    val producer = new KafkaProducer[String,String](pros)


    while(true){

      for(i <- 1 to 3;j <- 1 to 3){
        val record = new ProducerRecord[String,String]("ooxx",s"item$j",s"action$i")
        val records: Future[RecordMetadata] = producer.send(record)
        val metadata: RecordMetadata = records.get()
        val partition: Int = metadata.partition()
        val offset: Long = metadata.offset()
        println(s"item$j  action$i  partiton: $partition  offset: $offset")
      }

//      Thread.sleep(1000)

    }

    producer.close()



  }

}
