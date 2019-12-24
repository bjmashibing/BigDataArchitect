package com.msb.bigdata.spark.streaming

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
/*
接受socket的数据
那么receiver具备连接socket能力：host：port
 */
class CustormReceiver(host:String,port:Int) extends  Receiver[String](StorageLevel.DISK_ONLY){


  override def onStart(): Unit = {

    new Thread{
      override def run(): Unit = {
        ooxx()
      }
    }.start()
  }

  private def ooxx(): Unit ={


    val server = new Socket(host,port)

    val reader = new BufferedReader(new InputStreamReader(server.getInputStream))
    var line: String = reader.readLine()
    while(!isStopped()  &&  line != null ){
      store(line)
      line  = reader.readLine()
    }

  }
  override def onStop(): Unit = ???
}
