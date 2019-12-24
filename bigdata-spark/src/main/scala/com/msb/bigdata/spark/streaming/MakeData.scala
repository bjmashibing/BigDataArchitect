package com.msb.bigdata.spark.streaming

import java.io.{OutputStream, PrintStream}
import java.net.{ServerSocket, Socket}

object MakeData {


  def main(args: Array[String]): Unit = {


    val listen = new ServerSocket(8889)
    println("server start")
    while(true){
      val client: Socket = listen.accept()

      new Thread(){
        override def run(): Unit = {
          var num = 0
          if(client.isConnected){
            val out: OutputStream = client.getOutputStream
            val printer = new PrintStream(out)
            while(client.isConnected){
              num+=1
              printer.println(s"hello ${num}")
              Thread.sleep(1000)
            }
          }
        }
      }.start()
    }
  }

}
