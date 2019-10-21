package com.msb.bigdata.scala

trait  God{
  def say(): Unit ={
    println("god...say")
  }
}

trait Mg{
  def ku(): Unit ={
    println("mg...say")
  }
  def haiRen():Unit
}

class Person(name:String)  extends   God with Mg{

  def hello(): Unit ={
    println(s"$name say hello")
  }

  override def haiRen(): Unit = {
    println("ziji shixian ....")
  }
}


object Lesson04_trait {

  def main(args: Array[String]): Unit = {

    val p = new Person("zhangsan")
    p.hello()
    p.say()
    p.ku()
    p.haiRen()

  }
}
