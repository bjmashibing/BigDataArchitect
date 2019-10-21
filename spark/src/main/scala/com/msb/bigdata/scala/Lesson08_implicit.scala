package com.msb.bigdata.scala

import java.util

object Lesson08_implicit {


  def main(args: Array[String]): Unit = {


    val listLinked = new util.LinkedList[Int]()
    listLinked.add(1)
    listLinked.add(2)
    listLinked.add(3)
    val listArray = new util.ArrayList[Int]()
    listArray.add(1)
    listArray.add(2)
    listArray.add(3)

//    list.foreach(println)   //3个东西：  list数据集  foreach 遍历行为  println 处理函数


//    def foreach[T](list:util.LinkedList[T], f:(T)=>Unit): Unit ={
//      val iter: util.Iterator[T] = list.iterator()
//      while(iter.hasNext) f(iter.next())
//    }

//    foreach(list,println)


//    val xx = new XXX(list)
//    xx.foreach(println)

    //隐式转换：  隐式转换方法
    implicit def sdfsdf[T](list:util.LinkedList[T]) ={
      val iter: util.Iterator[T] = list.iterator()
      new XXX(iter)
    }

    implicit def sldkfjskldfj[T](list:java.util.ArrayList[T]) ={
      val iter: util.Iterator[T] = list.iterator()
      new XXX(iter)
    }

    listLinked.foreach(println)
    listArray.foreach(println)

    //spark  RDD N方法 scala
    //隐式转换类
//    implicit  class KKK[T](list:util.LinkedList[T]){
//      def foreach( f:(T)=>Unit): Unit ={
//        val iter: util.Iterator[T] = list.iterator()
//        while(iter.hasNext) f(iter.next())
//      }
//    }
//    list.foreach(println) //必须先承认一件事情：  list有foreach方法吗？  肯定是没有的~！ 在java里这么写肯定报错。。

    //这些代码最终交给的是scala的编译器！
    /*
    1,scala编译器发现 list.foreach(println)  有bug
    2,去寻找有没有implicit  定义的方法，且方法的参数正好是list的类型！！！
    3,编译期：完成你曾经人类：
    //    val xx = new XXX(list)
//    xx.foreach(println)
  *，编译器帮你把代码改写了。。。！
     */


    implicit val sdfsdfsd:String = "werwe"
//    implicit val sdfsdfs:String = "wangwu"
    implicit val sssss:Int = 88
    def ooxx(age:Int)(implicit name:String ): Unit ={
      println(name+" "+age)
    }

//    def sdfsf(name:String = "wangwu")

//    ooxx("zhangsan")
    ooxx(66)("jkljkl")
    ooxx(66)







  }
}

class XXX[T](list:util.Iterator[T]){

  def foreach( f:(T)=>Unit): Unit ={

    while(list.hasNext) f(list.next())
  }

}

//class XXX[T](list:util.LinkedList[T]){
//
//  def foreach( f:(T)=>Unit): Unit ={
//    val iter: util.Iterator[T] = list.iterator()
//    while(iter.hasNext) f(iter.next())
//  }
//
//}

