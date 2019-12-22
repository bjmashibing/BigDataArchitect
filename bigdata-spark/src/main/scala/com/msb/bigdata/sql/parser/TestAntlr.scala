package com.msb.bigdata.sql.parser

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}

import scala.tools.nsc.ast.parser.CommonTokens


object TestAntlr {

  def main(args: Array[String]): Unit = {


    val lexer = new OoxxLexer(new ANTLRInputStream("{1,{2,3},4}"))
    val tokens = new CommonTokenStream(lexer)
    val parser = new OoxxParser(tokens)

    val tree: ParseTree = parser.oxinit()


    println(tree.toStringTree(parser))














  }



}
