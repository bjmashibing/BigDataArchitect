package com.msb.bigdata.scala

import java.util

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Lesson03_Collections {


  def main(args: Array[String]): Unit = {

    //你是一个javacoder
    val listJava = new util.LinkedList[String]()
    listJava.add("hello")

    //scala还有自己的collections
    //1.数组
    //Java中泛型是<>  scala中是[]，所以数组用（n）
    //  val 约等于 final  不可变描述的是val指定的引用的值（值：字面值，地址）
    val arr01 = Array[Int](1,2,3,4)
//    arr01=Array(1,2,3,3,3,3)
    arr01(1)=99
    println(   arr01(0)  )

    for (elem <- arr01) {
      println(elem)
    }
    //遍历元素，需要函数接收元素
    arr01.foreach(println)

    println("--------------list-------------")
    //2.链表
    //scala中collections中有个2个包：immutable，mutable  默认的是不可变的immutable
    val list01 = List(1,2,3,4,5,4,3,2,1)
    for (elem <- list01) {
      println(elem)
    }
    list01.foreach(println)
//    list01.+=(22)

    val list02 = new ListBuffer[Int]()
    list02.+=(33)
    list02.+=(34)
    list02.+=(35)

    //TODO:学习  scala数据集中的  ++ +=  ++：  ：++

    list02.foreach(println)



    println("--------------Set-------------")

    val set01: Set[Int] = Set(1,2,3,4,2,1)
    for (elem <- set01) {
      println(elem)
    }
    set01.foreach(println)

    import scala.collection.mutable.Set
    val set02: mutable.Set[Int] = Set(11,22,33,44,11)
    set02.add(88)

    set02.foreach(println)

    val set03: Predef.Set[Int] = scala.collection.immutable.Set(33,44,22,11)
//    set03.add

    println("--------------tuple-------------")

//    val t2 = new Tuple2(11,"sdfsdf")  //2元素的Tuple2  在scala描绘的是K,V
    val t2 = (11,"sdfsdf")  //2元素的Tuple2  在scala描绘的是K,V
    val t3 = Tuple3(22,"sdfsdf",'s')
    val t4: (Int, Int, Int, Int) = (1,2,3,4)
    val t22: ((Int, Int) => Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) = ( (a:Int,b:Int)=>a+b+8   ,2,3,4,5,6,7,8,9,1,2,3,4,5,6,7,8,9,1,2,3,4)

    println(t2._1)
    println(t4._3)
//    val i: Int = t22._1(8)
//    println(i)
    println(t22._1)

    val tIter: Iterator[Any] = t22.productIterator
    while(tIter.hasNext){
      println(tIter.next())
    }


    println("--------------map-------------")
    val map01: Map[String, Int] = Map( ("a",33) ,  "b"->22  ,("c",3434),("a",3333)  )
    val keys: Iterable[String] = map01.keys

    //option: none  some
    println(map01.get("a").get)
//    println(map01.get("w").get)


    println(map01.get("a").getOrElse("hello world"))
    println(map01.get("w").getOrElse("hello world"))
    for (elem <- keys) {
      println(s"key: $elem   value: ${map01.get(elem).get}")
    }

//    keys.foreach()

    val map02: mutable.Map[String, Int] = scala.collection.mutable.Map(("a",11),("b",22))
    map02.put("c",22)

    println("--------------艺术-------------")


    val list = List(1,2,3,4,5,6)

    list.foreach(println)

    val listMap: List[Int] = list.map( (x:Int) => x+10  )
    listMap.foreach(println)
    val listMap02: List[Int] = list.map(  _*10 )

    list.foreach(println)
    listMap02.foreach(println)

    println("--------------艺术-升华------------")

    val listStr = List(
      "hello world",
      "hello msb",
      "good idea"
    )
//        val listStr = Array(
//      "hello world",
//      "hello msb",
//      "good idea"
//    )
//        val listStr = Set(
//      "hello world",
//      "hello msb",
//      "good idea"
//    )


    val flatMap= listStr.flatMap(  (x:String)=> x.split(" ") )
    flatMap.foreach(println)
    val mapList = flatMap.map( (_,1) )
    mapList.foreach(println)

    //以上代码有什么问题吗？  内存扩大了N倍，每一步计算内存都留有对象数据；有没有什么现成的技术解决数据计算中间状态占用内存这一问题~？
    //iterator！！！！！

    println("--------------艺术-再-升华------------")



    //基于迭代器的原码分析

    val iter: Iterator[String] = listStr.iterator  //什么是迭代器，为什么会有迭代器模式？  迭代器里不存数据！

    val iterFlatMap= iter.flatMap(  (x:String)=> x.split(" ") )
//    iterFlatMap.foreach(println)

    val iterMapList = iterFlatMap.map( (_,1) )

    while(iterMapList.hasNext){
      val tuple: (String, Int) = iterMapList.next()
      println(tuple)
    }



//    iterMapList.foreach(println)

    //1.listStr真正的数据集，有数据的
    //2.iter.flatMap  没有发生计算，返回了一个新的迭代器










  }






}
