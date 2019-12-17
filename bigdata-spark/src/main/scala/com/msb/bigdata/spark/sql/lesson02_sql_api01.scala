package com.msb.bigdata.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{DataType, DataTypes, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, Dataset, Encoders, RelationalGroupedDataset, Row, SaveMode, SparkSession}

import scala.beans.BeanProperty


class Person  extends  Serializable {
  @BeanProperty
  var name :String = ""
  @BeanProperty
  var age:Int  =  0
}


object lesson02_sql_api01 {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("test")
    val session: SparkSession = SparkSession
      .builder()
      .config(conf)
      //      .appName("test")
      //      .master("local")
      //      .enableHiveSupport()   //开启这个选项时  spark sql on  hive  才支持DDL，没开启，spark只有catalog
      .getOrCreate()

    val sc: SparkContext = session.sparkContext
    sc.setLogLevel("ERROR")


    import  session.implicits._


    val dataDF: DataFrame = List(
      "hello world",
      "hello world",
      "hello msb",
      "hello world",
      "hello world",
      "hello spark",
      "hello world",
      "hello spark"
    ).toDF("line")


    dataDF.createTempView("ooxx")

    val df: DataFrame = session.sql("select * from ooxx")
    df.show()
    df.printSchema()

    println("-------------------------------")
    session.sql(" select word, count(*) from   (select explode(split(line,' ')) as word from ooxx) as tt   group by tt.word  ").show()
    println("-------------------------------")

    //面向api的时候，df相当于 from tab
    val res: DataFrame = dataDF.selectExpr("explode(split(line,' ')) as word").groupBy("word").count()

    /*
    以上两种方式，哪个更快？
    为什么时第二种
         */
    println("-------------------------------")

    res.write.mode(SaveMode.Append).parquet("./bigdata-spark/data/out/ooxx")

    println("-------------------------------")


    val frame: DataFrame = session.read.parquet("./bigdata-spark/data/out/ooxx")

    frame.show()
    frame.printSchema()

    /*
    基于文件的行式：
    session.read.parquet()
    session.read.textFile()
    session.read.json()
    session.read.csv()
    读取任何格式的数据源都要转换成DF
    res.write.parquet()
    res.write.orc()
    res.write.text()
    */

    session.read.textFile("dfsdf")








    //数据+元数据  ==  df   就是一张表！！！！

    //1  数据：  RDD[Row]

    //Spark 的Dataset  既可以按collection，类似于rdd的方法操作，也可以按SQL领域语言定义的方式操作数据
//    import session.implicits._


    /**
     * 纯文本文件，不带自描述，string  不被待见的
     * 必须转结构化。。再参与计算
     * 转换的过程可以由spark完成
     * hive数仓
     * 接数：源数据   不删除，不破坏
     * ETL  中间态
     * 所有的计算发生再中间态
     * 中间态：--》一切以后续计算成本为考量
     * 文件格式类型？
     * 分区/分桶
    */
//    val ds01: Dataset[String] = session.read.textFile("./bigdata-spark/data/person.txt")
//    val person: Dataset[(String, Int)] = ds01.map(
//      line => {
//        val strs: Array[String] = line.split(" ")
//        (strs(0), strs(1).toInt)
//      }
//    )(Encoders.tuple(Encoders.STRING, Encoders.scalaInt))
//
//    val cperson: DataFrame = person.toDF("name","age")
//    cperson.show()
//    cperson.printSchema()




//    val rdd: RDD[String] = sc.textFile("./bigdata-spark/data/person.txt")

    //第二种方式： bean类型的rdd + javabean
    //数据+元数据  ==  df   就是一张表！！！！

//    val p = new Person
//    //1,mr,spark  pipeline  iter  一次内存飞过一条数据：：-> 这一条记录完成读取/计算/序列化
//    //2，分布式计算，计算逻辑由 Driver  序列化，发送给其他jvm的Executor中执行
//    val rddBean: RDD[Person] = rdd.map(_.split(" "))
//      .map(arr => {
////        val p = new Person
//        p.setName(arr(0))
//        p.setAge(arr(1).toInt)
//        p
//      })
//
//    val df: DataFrame = session.createDataFrame(rddBean,classOf[Person])
//    df.show()
//    df.printSchema()














    //第一点一版本：动态封装：

//    val userSchema = Array(
//        "name string",
//        "age int",
//        "sex int"
//    )
//
//    //1 row rdd
//
//    def toDataType(vv:(String,Int))={
//      userSchema(vv._2).split(" ")(1) match {
//        case "string" => vv._1.toString
//        case "int" => vv._1.toInt
//      }
//    }
//
//    val rowRdd: RDD[Row] = rdd.map(_.split(" "))
//      .map(x => x.zipWithIndex)
//      // x == [(zhangsan,0) (18,1)  (0,2)]
//      .map(x => x.map(toDataType(_)))
//      .map(x => Row.fromSeq(x))//Row  代表的很多的列，每个列要标识出准确的类型
//
//    //2 structtype
//
//    def getDataType(v:String)={
//      v match {
//        case "string" => DataTypes.StringType
//        case "int" => DataTypes.IntegerType
//
//      }
//    }
//    val fields: Array[StructField] = userSchema.map(_.split(" ")).map(x=> StructField.apply(x(0),getDataType(x(1))))
//
//    val schema: StructType = StructType.apply(fields)
//
//    val schema01: StructType = StructType.fromDDL("name string,age int,sex int")
//    val df: DataFrame = session.createDataFrame(rowRdd,schema01)
//    df.show()
//    df.printSchema()






    // 第一种方式： row类型的rdd + structType
    //数据+元数据  ==  df   就是一张表！！！！
//    val rddRow: RDD[Row] = rdd
//      .map(_.split(" ")).map(arr=> Row.apply(arr(0),arr(1).toInt))
////      .map(x => Row.apply(Array(x._1,x._2)))  //错误的。。。等于列里时一个arry
//
//
//    //2 元数据 ： StructType
//    val fields = Array(
//      StructField.apply("name", DataTypes.StringType, true),
//      StructField.apply("age", DataTypes.IntegerType, true)
//    )
//
//    val schema: StructType = StructType.apply(fields)
//
//    val dataFrame: DataFrame = session.createDataFrame(rddRow,schema)
//
////    dataFrame.show()
////    dataFrame.printSchema()
////    dataFrame.createTempView("person")
////    session.sql("select * from person").show()
//
//    //metastore   catalog
//    session.sql("create table ooxx (name string,age int)")
//    session.catalog.listTables().show()

  }




}
