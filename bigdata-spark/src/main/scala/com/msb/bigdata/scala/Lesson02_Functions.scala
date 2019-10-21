package com.msb.bigdata.scala

import java.util
import java.util.Date

object Lesson02_Functions {

  //成员方法
  def ooxx(): Unit ={
    println("hello object")
  }

  def main(args: Array[String]): Unit = {


    //  方法  函数


    println("-------1.basic----------")

    //返回值，参数，函数体
    def fun01() {
      println("hello world")
    }

    fun01()
    var x = 3
    var y = fun01()
    println(y)

    //想有返回
    //    public void sdfsd(){}
    //    public String sdfsdf(){}
    //有return必须给出返回类型
    def fun02() = {

      new util.LinkedList[String]()
    }

    //参数：必须给出类型，是val
    //class 构造，是var，val
    def fun03(a: Int): Unit = {
      println(a)
    }

    fun03(33)

    println("-------2.递归函数----------")

    //递归先写触底！  触发什么报错呀
    def fun04(num: Int): Int = {
      if (num == 1) {
        num
      } else {
        num * fun04(num - 1)
      }
    }

    val i: Int = fun04(4)
    println(i)

    println("-------3.默认值函数----------")

    def fun05(a: Int = 8, b: String = "abc"): Unit = {
      println(s"$a\t$b")
    }

    //    fun05(9,"def")
    fun05(22)
    fun05(b = "ooxx")

    println("-------4.匿名函数----------")
    //函数是第一类值
    //函数：
    //1，签名 ：(Int,Int)=>Int ：  （参数类型列表）=> 返回值类型
    //2，匿名函数： (a:Int,b:Int) => { a+b }  ：（参数实现列表）=> 函数体
    var xx: Int = 3

    var yy: (Int, Int) => Int = (a: Int, b: Int) => {
      a + b
    }

    val w: Int = yy(3, 4)
    println(w)


    println("--------5.嵌套函数---------")

    def fun06(a: String): Unit = {

      def fun05(): Unit = {
        println(a)
      }

      fun05()
    }

    fun06("hello")


    println("--------6.偏应用函数---------")

    def fun07(date: Date, tp: String, msg: String): Unit = {

      println(s"$date\t$tp\t$msg")
    }

    fun07(new Date(), "info", "ok")

    var info = fun07(_: Date, "info", _: String)
    var error = fun07(_: Date, "error", _: String)
    info(new Date, "ok")
    error(new Date, "error...")

    println("--------7.可变参数---------")

    def fun08(a: Int*): Unit = {
      for (e <- a) {
        println(e)
      }
      //      def foreach[U](f: A => U): Unit
      //      a.foreach(   (x:Int)=>{println(x)}   )
      //      a.foreach(   println(_)   )
      a.foreach(println)
    }

    fun08(2)
    fun08(1, 2, 3, 4, 5, 6)

    println("--------8.高阶函数---------")
    //函数作为参数，函数作为返回值
    //函数作为参数
    def computer(a: Int, b: Int, f: (Int, Int) => Int): Unit = {
      val res: Int = f(a, b)
      println(res)
    }
    computer(3, 8, (x: Int, y: Int) => {
      x + y
    })
    computer(3, 8, (x: Int, y: Int) => {
      x * y
    })
    computer(3, 8, _ * _)
    //函数作为返回值：
    def factory(i: String): (Int, Int) => Int = {
      def plus(x: Int, y: Int): Int = {
        x + y
      }
      if (i.equals("+")) {
        plus
      } else {
        (x: Int, y: Int) => {
          x * y
        }
      }
    }


    computer(3, 8, factory("-"))


    println("--------9.柯里化---------")

    def fun09(a: Int)(b: Int)(c: String): Unit = {
      println(s"$a\t$b\t$c")
    }

    fun09(3)(8)("sdfsdf")

    def fun10(a: Int*)(b: String*): Unit = {
      a.foreach(println)
      b.foreach(println)
    }

    fun10(1, 2, 3)("sdfs", "sss")

    println("--------*.方法---------")

    //方法不想执行，赋值给一个引用  方法名+空格+下划线
    val funa = ooxx
    println(funa)
    val func = ooxx _
    func()

    //语法 ->  编译器  ->  字节码   <-  jvm规则
    //编译器，衔接 人  机器
    //java 中 +： 关键字
    //scala中+： 方法/函数
    //scala语法中，没有基本类型，所以你写一个数字  3  编辑器/语法，其实是把 3 看待成Int这个对象
//    3 + 2
//    3.+(2)
//    3:Int


  }

  /*
  学习scala就是为了多学一门语言吧？
  感觉不如python，不仅学了语言，也学了工具。
  理解有哪些偏差？ 老师？？

  编译型  C   《   贼快
  解释型  python   《   慢  贼慢

  JAVA：其实不值钱，最值钱的是JVM

  JAVA：  解释型，编译过程，类型   比 python 快
  JVM：为什么值钱  是C写的， 【字节码（二进制） >JVM（堆/堆外（二进制））<  kernel（mmap，sendfile） 】  更快！！

   */



}
