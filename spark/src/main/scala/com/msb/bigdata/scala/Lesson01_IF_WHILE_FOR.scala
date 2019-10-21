package com.msb.bigdata.scala

import scala.collection.immutable

object Lesson01_IF_WHILE_FOR {


  //自己特征：class  object
  //流控
  def main(args: Array[String]): Unit = {

    /*
    if
    while
    for
     */
    var a=0
    if(a <= 0){
      println(s"$a<0")
    }else{
      println(s"$a>=0")
    }

    var  aa=0
    while(aa <10){
      println(aa)
      aa=aa+1
    }

    println("-----------------------------")
    //for

//    for(i=0;i<10;i++)
//    for( P x : xs)

    val seqs = 1 until  10
    println(seqs)

    //循环逻辑，业务逻辑
    for( i <-  seqs if(i%2==0)){

      println(i)
    }

    println("----------------------")
//    for(i <- 1 to 9){
//      for (j <- 1 to 9){
//        if(j<=i) print(s"$i * $j = ${i*j}\t")
//        if(j == i ) println()
//      }
//    }
    var num = 0
    for(i <- 1 to 9;j <- 1 to 9 if(j<=i)){
      num+=1
        if(j<=i) print(s"$i * $j = ${i*j}\t")
        if(j == i ) println()
    }
    println(num)

    val seqss: immutable.IndexedSeq[Int] = for ( i <- 1 to 10) yield {
      var x = 8
      i + x
    }
//    println(seqss)
    for(i <-seqss){
      println(i)
    }










  }

}
