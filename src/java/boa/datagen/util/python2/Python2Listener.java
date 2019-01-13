// Generated from Python2.g4 by ANTLR 4.7.2

package boa.datagen.util.python2;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Python2Parser}.
 */
public interface Python2Listener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Python2Parser#single_input}.
	 * @param ctx the parse tree
	 */
	void enterSingle_input(Python2Parser.Single_inputContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#single_input}.
	 * @param ctx the parse tree
	 */
	void exitSingle_input(Python2Parser.Single_inputContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#file_input}.
	 * @param ctx the parse tree
	 */
	void enterFile_input(Python2Parser.File_inputContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#file_input}.
	 * @param ctx the parse tree
	 */
	void exitFile_input(Python2Parser.File_inputContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#eval_input}.
	 * @param ctx the parse tree
	 */
	void enterEval_input(Python2Parser.Eval_inputContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#eval_input}.
	 * @param ctx the parse tree
	 */
	void exitEval_input(Python2Parser.Eval_inputContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#decorator}.
	 * @param ctx the parse tree
	 */
	void enterDecorator(Python2Parser.DecoratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#decorator}.
	 * @param ctx the parse tree
	 */
	void exitDecorator(Python2Parser.DecoratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#decorators}.
	 * @param ctx the parse tree
	 */
	void enterDecorators(Python2Parser.DecoratorsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#decorators}.
	 * @param ctx the parse tree
	 */
	void exitDecorators(Python2Parser.DecoratorsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#decorated}.
	 * @param ctx the parse tree
	 */
	void enterDecorated(Python2Parser.DecoratedContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#decorated}.
	 * @param ctx the parse tree
	 */
	void exitDecorated(Python2Parser.DecoratedContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#funcdef}.
	 * @param ctx the parse tree
	 */
	void enterFuncdef(Python2Parser.FuncdefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#funcdef}.
	 * @param ctx the parse tree
	 */
	void exitFuncdef(Python2Parser.FuncdefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#parameters}.
	 * @param ctx the parse tree
	 */
	void enterParameters(Python2Parser.ParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#parameters}.
	 * @param ctx the parse tree
	 */
	void exitParameters(Python2Parser.ParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#varargslist}.
	 * @param ctx the parse tree
	 */
	void enterVarargslist(Python2Parser.VarargslistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#varargslist}.
	 * @param ctx the parse tree
	 */
	void exitVarargslist(Python2Parser.VarargslistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#fpdef}.
	 * @param ctx the parse tree
	 */
	void enterFpdef(Python2Parser.FpdefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#fpdef}.
	 * @param ctx the parse tree
	 */
	void exitFpdef(Python2Parser.FpdefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#fplist}.
	 * @param ctx the parse tree
	 */
	void enterFplist(Python2Parser.FplistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#fplist}.
	 * @param ctx the parse tree
	 */
	void exitFplist(Python2Parser.FplistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(Python2Parser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(Python2Parser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#simple_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSimple_stmt(Python2Parser.Simple_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#simple_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSimple_stmt(Python2Parser.Simple_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#small_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSmall_stmt(Python2Parser.Small_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#small_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSmall_stmt(Python2Parser.Small_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#expr_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExpr_stmt(Python2Parser.Expr_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#expr_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExpr_stmt(Python2Parser.Expr_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#augassign}.
	 * @param ctx the parse tree
	 */
	void enterAugassign(Python2Parser.AugassignContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#augassign}.
	 * @param ctx the parse tree
	 */
	void exitAugassign(Python2Parser.AugassignContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#print_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPrint_stmt(Python2Parser.Print_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#print_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPrint_stmt(Python2Parser.Print_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#del_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDel_stmt(Python2Parser.Del_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#del_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDel_stmt(Python2Parser.Del_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#pass_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPass_stmt(Python2Parser.Pass_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#pass_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPass_stmt(Python2Parser.Pass_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#flow_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFlow_stmt(Python2Parser.Flow_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#flow_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFlow_stmt(Python2Parser.Flow_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBreak_stmt(Python2Parser.Break_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#break_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBreak_stmt(Python2Parser.Break_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void enterContinue_stmt(Python2Parser.Continue_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#continue_stmt}.
	 * @param ctx the parse tree
	 */
	void exitContinue_stmt(Python2Parser.Continue_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn_stmt(Python2Parser.Return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn_stmt(Python2Parser.Return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#yield_stmt}.
	 * @param ctx the parse tree
	 */
	void enterYield_stmt(Python2Parser.Yield_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#yield_stmt}.
	 * @param ctx the parse tree
	 */
	void exitYield_stmt(Python2Parser.Yield_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#raise_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRaise_stmt(Python2Parser.Raise_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#raise_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRaise_stmt(Python2Parser.Raise_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#import_stmt}.
	 * @param ctx the parse tree
	 */
	void enterImport_stmt(Python2Parser.Import_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#import_stmt}.
	 * @param ctx the parse tree
	 */
	void exitImport_stmt(Python2Parser.Import_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#import_name}.
	 * @param ctx the parse tree
	 */
	void enterImport_name(Python2Parser.Import_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#import_name}.
	 * @param ctx the parse tree
	 */
	void exitImport_name(Python2Parser.Import_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#import_from}.
	 * @param ctx the parse tree
	 */
	void enterImport_from(Python2Parser.Import_fromContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#import_from}.
	 * @param ctx the parse tree
	 */
	void exitImport_from(Python2Parser.Import_fromContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#import_as_name}.
	 * @param ctx the parse tree
	 */
	void enterImport_as_name(Python2Parser.Import_as_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#import_as_name}.
	 * @param ctx the parse tree
	 */
	void exitImport_as_name(Python2Parser.Import_as_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#dotted_as_name}.
	 * @param ctx the parse tree
	 */
	void enterDotted_as_name(Python2Parser.Dotted_as_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#dotted_as_name}.
	 * @param ctx the parse tree
	 */
	void exitDotted_as_name(Python2Parser.Dotted_as_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#import_as_names}.
	 * @param ctx the parse tree
	 */
	void enterImport_as_names(Python2Parser.Import_as_namesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#import_as_names}.
	 * @param ctx the parse tree
	 */
	void exitImport_as_names(Python2Parser.Import_as_namesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#dotted_as_names}.
	 * @param ctx the parse tree
	 */
	void enterDotted_as_names(Python2Parser.Dotted_as_namesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#dotted_as_names}.
	 * @param ctx the parse tree
	 */
	void exitDotted_as_names(Python2Parser.Dotted_as_namesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#dotted_name}.
	 * @param ctx the parse tree
	 */
	void enterDotted_name(Python2Parser.Dotted_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#dotted_name}.
	 * @param ctx the parse tree
	 */
	void exitDotted_name(Python2Parser.Dotted_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#global_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_stmt(Python2Parser.Global_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#global_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_stmt(Python2Parser.Global_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#exec_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExec_stmt(Python2Parser.Exec_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#exec_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExec_stmt(Python2Parser.Exec_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#assert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAssert_stmt(Python2Parser.Assert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#assert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAssert_stmt(Python2Parser.Assert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#compound_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCompound_stmt(Python2Parser.Compound_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#compound_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCompound_stmt(Python2Parser.Compound_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void enterIf_stmt(Python2Parser.If_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#if_stmt}.
	 * @param ctx the parse tree
	 */
	void exitIf_stmt(Python2Parser.If_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile_stmt(Python2Parser.While_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#while_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile_stmt(Python2Parser.While_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFor_stmt(Python2Parser.For_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#for_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFor_stmt(Python2Parser.For_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#try_stmt}.
	 * @param ctx the parse tree
	 */
	void enterTry_stmt(Python2Parser.Try_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#try_stmt}.
	 * @param ctx the parse tree
	 */
	void exitTry_stmt(Python2Parser.Try_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#with_stmt}.
	 * @param ctx the parse tree
	 */
	void enterWith_stmt(Python2Parser.With_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#with_stmt}.
	 * @param ctx the parse tree
	 */
	void exitWith_stmt(Python2Parser.With_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#with_item}.
	 * @param ctx the parse tree
	 */
	void enterWith_item(Python2Parser.With_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#with_item}.
	 * @param ctx the parse tree
	 */
	void exitWith_item(Python2Parser.With_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#except_clause}.
	 * @param ctx the parse tree
	 */
	void enterExcept_clause(Python2Parser.Except_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#except_clause}.
	 * @param ctx the parse tree
	 */
	void exitExcept_clause(Python2Parser.Except_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#suite}.
	 * @param ctx the parse tree
	 */
	void enterSuite(Python2Parser.SuiteContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#suite}.
	 * @param ctx the parse tree
	 */
	void exitSuite(Python2Parser.SuiteContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#testlist_safe}.
	 * @param ctx the parse tree
	 */
	void enterTestlist_safe(Python2Parser.Testlist_safeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#testlist_safe}.
	 * @param ctx the parse tree
	 */
	void exitTestlist_safe(Python2Parser.Testlist_safeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#old_test}.
	 * @param ctx the parse tree
	 */
	void enterOld_test(Python2Parser.Old_testContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#old_test}.
	 * @param ctx the parse tree
	 */
	void exitOld_test(Python2Parser.Old_testContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#old_lambdef}.
	 * @param ctx the parse tree
	 */
	void enterOld_lambdef(Python2Parser.Old_lambdefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#old_lambdef}.
	 * @param ctx the parse tree
	 */
	void exitOld_lambdef(Python2Parser.Old_lambdefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#test}.
	 * @param ctx the parse tree
	 */
	void enterTest(Python2Parser.TestContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#test}.
	 * @param ctx the parse tree
	 */
	void exitTest(Python2Parser.TestContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#or_test}.
	 * @param ctx the parse tree
	 */
	void enterOr_test(Python2Parser.Or_testContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#or_test}.
	 * @param ctx the parse tree
	 */
	void exitOr_test(Python2Parser.Or_testContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#and_test}.
	 * @param ctx the parse tree
	 */
	void enterAnd_test(Python2Parser.And_testContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#and_test}.
	 * @param ctx the parse tree
	 */
	void exitAnd_test(Python2Parser.And_testContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#not_test}.
	 * @param ctx the parse tree
	 */
	void enterNot_test(Python2Parser.Not_testContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#not_test}.
	 * @param ctx the parse tree
	 */
	void exitNot_test(Python2Parser.Not_testContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(Python2Parser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(Python2Parser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#comp_op}.
	 * @param ctx the parse tree
	 */
	void enterComp_op(Python2Parser.Comp_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#comp_op}.
	 * @param ctx the parse tree
	 */
	void exitComp_op(Python2Parser.Comp_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(Python2Parser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(Python2Parser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#xor_expr}.
	 * @param ctx the parse tree
	 */
	void enterXor_expr(Python2Parser.Xor_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#xor_expr}.
	 * @param ctx the parse tree
	 */
	void exitXor_expr(Python2Parser.Xor_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#and_expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expr(Python2Parser.And_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#and_expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expr(Python2Parser.And_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#shift_expr}.
	 * @param ctx the parse tree
	 */
	void enterShift_expr(Python2Parser.Shift_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#shift_expr}.
	 * @param ctx the parse tree
	 */
	void exitShift_expr(Python2Parser.Shift_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#arith_expr}.
	 * @param ctx the parse tree
	 */
	void enterArith_expr(Python2Parser.Arith_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#arith_expr}.
	 * @param ctx the parse tree
	 */
	void exitArith_expr(Python2Parser.Arith_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(Python2Parser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(Python2Parser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#factor}.
	 * @param ctx the parse tree
	 */
	void enterFactor(Python2Parser.FactorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#factor}.
	 * @param ctx the parse tree
	 */
	void exitFactor(Python2Parser.FactorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#power}.
	 * @param ctx the parse tree
	 */
	void enterPower(Python2Parser.PowerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#power}.
	 * @param ctx the parse tree
	 */
	void exitPower(Python2Parser.PowerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(Python2Parser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(Python2Parser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#listmaker}.
	 * @param ctx the parse tree
	 */
	void enterListmaker(Python2Parser.ListmakerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#listmaker}.
	 * @param ctx the parse tree
	 */
	void exitListmaker(Python2Parser.ListmakerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#testlist_comp}.
	 * @param ctx the parse tree
	 */
	void enterTestlist_comp(Python2Parser.Testlist_compContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#testlist_comp}.
	 * @param ctx the parse tree
	 */
	void exitTestlist_comp(Python2Parser.Testlist_compContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#lambdef}.
	 * @param ctx the parse tree
	 */
	void enterLambdef(Python2Parser.LambdefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#lambdef}.
	 * @param ctx the parse tree
	 */
	void exitLambdef(Python2Parser.LambdefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#trailer}.
	 * @param ctx the parse tree
	 */
	void enterTrailer(Python2Parser.TrailerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#trailer}.
	 * @param ctx the parse tree
	 */
	void exitTrailer(Python2Parser.TrailerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#subscriptlist}.
	 * @param ctx the parse tree
	 */
	void enterSubscriptlist(Python2Parser.SubscriptlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#subscriptlist}.
	 * @param ctx the parse tree
	 */
	void exitSubscriptlist(Python2Parser.SubscriptlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#subscript}.
	 * @param ctx the parse tree
	 */
	void enterSubscript(Python2Parser.SubscriptContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#subscript}.
	 * @param ctx the parse tree
	 */
	void exitSubscript(Python2Parser.SubscriptContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#sliceop}.
	 * @param ctx the parse tree
	 */
	void enterSliceop(Python2Parser.SliceopContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#sliceop}.
	 * @param ctx the parse tree
	 */
	void exitSliceop(Python2Parser.SliceopContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#exprlist}.
	 * @param ctx the parse tree
	 */
	void enterExprlist(Python2Parser.ExprlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#exprlist}.
	 * @param ctx the parse tree
	 */
	void exitExprlist(Python2Parser.ExprlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#testlist}.
	 * @param ctx the parse tree
	 */
	void enterTestlist(Python2Parser.TestlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#testlist}.
	 * @param ctx the parse tree
	 */
	void exitTestlist(Python2Parser.TestlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#dictorsetmaker}.
	 * @param ctx the parse tree
	 */
	void enterDictorsetmaker(Python2Parser.DictorsetmakerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#dictorsetmaker}.
	 * @param ctx the parse tree
	 */
	void exitDictorsetmaker(Python2Parser.DictorsetmakerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#classdef}.
	 * @param ctx the parse tree
	 */
	void enterClassdef(Python2Parser.ClassdefContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#classdef}.
	 * @param ctx the parse tree
	 */
	void exitClassdef(Python2Parser.ClassdefContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#arglist}.
	 * @param ctx the parse tree
	 */
	void enterArglist(Python2Parser.ArglistContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#arglist}.
	 * @param ctx the parse tree
	 */
	void exitArglist(Python2Parser.ArglistContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(Python2Parser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(Python2Parser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#list_iter}.
	 * @param ctx the parse tree
	 */
	void enterList_iter(Python2Parser.List_iterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#list_iter}.
	 * @param ctx the parse tree
	 */
	void exitList_iter(Python2Parser.List_iterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#list_for}.
	 * @param ctx the parse tree
	 */
	void enterList_for(Python2Parser.List_forContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#list_for}.
	 * @param ctx the parse tree
	 */
	void exitList_for(Python2Parser.List_forContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#list_if}.
	 * @param ctx the parse tree
	 */
	void enterList_if(Python2Parser.List_ifContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#list_if}.
	 * @param ctx the parse tree
	 */
	void exitList_if(Python2Parser.List_ifContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#comp_iter}.
	 * @param ctx the parse tree
	 */
	void enterComp_iter(Python2Parser.Comp_iterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#comp_iter}.
	 * @param ctx the parse tree
	 */
	void exitComp_iter(Python2Parser.Comp_iterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#comp_for}.
	 * @param ctx the parse tree
	 */
	void enterComp_for(Python2Parser.Comp_forContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#comp_for}.
	 * @param ctx the parse tree
	 */
	void exitComp_for(Python2Parser.Comp_forContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#comp_if}.
	 * @param ctx the parse tree
	 */
	void enterComp_if(Python2Parser.Comp_ifContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#comp_if}.
	 * @param ctx the parse tree
	 */
	void exitComp_if(Python2Parser.Comp_ifContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#testlist1}.
	 * @param ctx the parse tree
	 */
	void enterTestlist1(Python2Parser.Testlist1Context ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#testlist1}.
	 * @param ctx the parse tree
	 */
	void exitTestlist1(Python2Parser.Testlist1Context ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#encoding_decl}.
	 * @param ctx the parse tree
	 */
	void enterEncoding_decl(Python2Parser.Encoding_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#encoding_decl}.
	 * @param ctx the parse tree
	 */
	void exitEncoding_decl(Python2Parser.Encoding_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link Python2Parser#yield_expr}.
	 * @param ctx the parse tree
	 */
	void enterYield_expr(Python2Parser.Yield_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link Python2Parser#yield_expr}.
	 * @param ctx the parse tree
	 */
	void exitYield_expr(Python2Parser.Yield_exprContext ctx);
}