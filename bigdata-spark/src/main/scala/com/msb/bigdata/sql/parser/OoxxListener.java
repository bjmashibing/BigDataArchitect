// Generated from D:/Github/bigdata/bigdata-spark/src/main/scala/com/msb/bigdata/sql/g4\Ooxx.g4 by ANTLR 4.7.2
package com.msb.bigdata.sql.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link OoxxParser}.
 */
public interface OoxxListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link OoxxParser#oxinit}.
	 * @param ctx the parse tree
	 */
	void enterOxinit(OoxxParser.OxinitContext ctx);
	/**
	 * Exit a parse tree produced by {@link OoxxParser#oxinit}.
	 * @param ctx the parse tree
	 */
	void exitOxinit(OoxxParser.OxinitContext ctx);
	/**
	 * Enter a parse tree produced by {@link OoxxParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(OoxxParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link OoxxParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(OoxxParser.ValueContext ctx);
}