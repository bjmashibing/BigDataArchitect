语言分类：编译型，解释型！

1.  编译型：c。。。
2.  解释型：python。。。
3.  java是啥？
	1.  需要编译   字节码
	2.  解释执行/直接执行 
C：可移植性  代码 对应不同平台，各自编译
JAVA: 可移动性：一次编译，到处运行。。。JVM才是核心！

编译器！：编译型，解释型根本区别在哪？
	是否是强类型，什么是类型：宽度
	
SCALA  on  JVM  一定有一个编译

JAVA  /   SCALA
编译器  / 编译器   =>思考一个问题：scala代码和java不一样（编译器做了很多事情）
	JVM


再聊语言：模型：

	1.  面向过程的   第一类值：基本类型    +指针
	2.  面向对象的   第一类值：基本类型   +  对象类型
	3.  函数式的	 第一类值：基本类型 + 对象类型  + 函数

SCALA  面向对象的函数式编程语言

————————————————————————————————————————————
```
int a ;
a=33;
1,明文给出  推断
2,传参  很难发生推断

var a = 1
a="sdfsdf"
var b = "1"
var c = '1'
xx(a) {} //推断不出来
推断不代表糊弄,防止运行期报错
```


_____
使用scala：
1.  开发     jdk      sdk（编译器）
2.  运行     jdk jre

V： spark 2.3.x > scala 2.11  > jre/jdk 1.8.uxxx
	https://www.oracle.com/technetwork/java/javase/archive-139210.html

--------------coder-------------

主流：使用  集成工具  ：  IDE

	1.  IDEA  +plugin  +编译器   》创建scala项目了！
	2.  启动屏幕：configure-》；plugins  
	
____
#学习scala
https://docs.scala-lang.org/zh-cn/tour/tour-of-scala.html

1.  main,class,object
```text
主方法只能写在 object定义的文件中
object和class啥区别：
回顾一个问题：java中有一个知识点 静态
分号可有可无
scala是包级别区分，类名可以和文件名不一致

```

_____
TODO:2019-09-29:
MarkDown

IDEA中 plugins 中搜索  mind 安装 idea mind map






 





























