package com.msb.bigdata.scala


//约等于  static  单例对象
//static
//单例  new    scala的编译器很人性化   让你人少写了很多代码
object ooxx {

  //  private val xo:xxoo = new xxoo()
  private val xo = new ooxx(11)
  //  Integer num = 0;
  //  var/val   var:变量  val常量 取代了final
  /* var a=3
   a=4
   val b=4
   b=5*/

  private val name = "object:zhangsan"


  println("ooxx....up")

  def main(args: Array[String]): Unit = {
    println("hello from ooxx")
    xo.printMsg()
  }

  println("ooxx....down")

}

//类里，裸露的代码是默认构造中的。有默认构造
//个性化构造！！
//类名构造器中的参数就是类的成员属性，且默认是val类型，且默认是private
//只有在类名构造其中的参数可以设置成var，其他方法函数中的参数都是val类型的，且不允许设置成var类型
class ooxx(sex: String) {


  var name = "class:zhangsan"

  def this(xname: Int) {
    //必须调用默认构造
    this("abc")


  }

  var a: Int = 3

  //  private val value = new ooxx()

  println(s"ooxx....up$a....")

  def printMsg(): Unit = {
    println(s"sex: ${ooxx.name}")
  }

  println(s"ooxx....up${a + 4}")
}




