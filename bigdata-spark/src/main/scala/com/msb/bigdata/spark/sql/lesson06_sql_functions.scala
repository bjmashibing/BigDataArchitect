package com.msb.bigdata.spark.sql

import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, IntegerType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}



//class MyAggFun extends   UserDefinedAggregateFunction {
//  override def inputSchema: StructType = {
//    // ooxx(score)
//    StructType.apply(Array(StructField.apply("score",IntegerType,false)))
//  }
//
//  override def bufferSchema: StructType = {
//    //avg  sum / count = avg
//    StructType.apply(Array(
//      StructField.apply("sum",IntegerType,false),
//      StructField.apply("count",IntegerType,false)
//    ))
//  }
//
//  override def dataType: DataType = DoubleType
//
//  override def deterministic: Boolean = true
//
//  override def initialize(buffer: MutableAggregationBuffer): Unit = {
//    buffer(0) = 0
//    buffer(1) = 0
//  }
//
//  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
//    //组内，一条记录调用一次
//    buffer(0) = buffer.getInt(0) +  input.getInt(0)  //sum
//    buffer(1) = buffer.getInt(1) + 1  //count
//
//  }
//
//  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
//    buffer1(0) = buffer1.getInt(0) + buffer2.getInt(0)
//    buffer1(1) = buffer1.getInt(1) + buffer2.getInt(1)
//  }
//
//  override def evaluate(buffer: Row): Double = {  buffer.getInt(0) / buffer.getInt(1)  }
//}





object lesson06_sql_functions {


  def main(args: Array[String]): Unit = {
    val ss: SparkSession = SparkSession
      .builder()
      .appName("functions")
      .master("local")
      .getOrCreate()
    ss.sparkContext.setLogLevel("ERROR")

    import ss.implicits._

    val dataDF: DataFrame = List(
      ("A", 1, 90),
      ("B", 1, 90),
      ("A", 2, 50),
      ("C", 1, 80),
      ("B", 2, 60)
    ).toDF("name", "class", "score")

    dataDF.createTempView("users")

    //OLAP
    //开窗函数 依赖   fun()   over（partition  order ）
    //group by 是纯聚合函数： 一组最后就一条

//    ss.sql("select *, " +
//      "count(score) over (partition by class) as num " +
//      "from users").show()

//    ss.sql("select  class , count(score) as num   from users   group by class ").show()


//    ss.sql("select * ," +
//      " rank()  over(partition by class order by score desc  )  as rank ,   " +
//      " row_number()  over(partition by class order by score desc  )  as number    " +
//      "from users ").show()


    // 架构师 webserver  data  （update）  【ods  dw(仓库  真实/历史  拉链)  dm  bi 】


    //行列转换


//    ss.sql("select name , " +
//      "explode( " +
//      "split( " +
//      "     concat(case when class = 1 then 'AA' else 'BB' end ,' ',score) , ' ' " +
//      " ))  " +
//      "as ox  " +
//      "from users").show()

    //case when


//    ss.sql("select  " +
//      "  case  "  +
//      "  when score <= 100 and score >=90  then 'good'  " +
//      "  when score < 90  and score >= 80 then 'you' " +
//      "  else 'cha' " +
//      "end   as ox ,"  +
//      "   count(*)  " +
//      "from users   " +
//      "group by " +
//      "  case  "  +
//    "  when score <= 100 and score >=90  then 'good'  " +
//      "  when score < 90  and score >= 80 then 'you' " +
//      "  else 'cha' " +
//      "end  "
//
//    ).show()

//    ss.sql("select * ," +
//      "case " +
//      "  when score <= 100 and score >=90  then 'good'  " +
//      "  when score < 90  and score >= 80 then 'you' " +
//      "  else 'cha' " +
//      "end as ox " +
//      "from users").show()

    //udf

//    ss.udf.register("ooxx",new MyAggFun)  // avg
//    ss.sql("select name,    " +
//      " ooxx(score)    " +
//      "from users  " +
//      "group by name  ")
//      .show()

//    ss.udf.register("ooxx",(x:Int)=>{x*10})
//    ss.sql("select * ,ooxx(score) as ox from users").show()


    //分组，排序统计
//    ss.sql("select * from users  order by name  desc ,score  asc").show()


//    ss.sql("select  name,   " +
//      " sum(score) " +
//      "from users " +
//      "group by name " +
//      "order by name desc")
//      .show()

  }








}
