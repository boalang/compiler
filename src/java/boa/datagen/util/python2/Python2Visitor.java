package boa.datagen.util.python2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import boa.datagen.util.python2.*;
import boa.datagen.util.python2.Python2Parser.And_exprContext;
import boa.datagen.util.python2.Python2Parser.And_testContext;
import boa.datagen.util.python2.Python2Parser.ArglistContext;
import boa.datagen.util.python2.Python2Parser.ArgumentContext;
import boa.datagen.util.python2.Python2Parser.Arith_exprContext;
import boa.datagen.util.python2.Python2Parser.Assert_stmtContext;
import boa.datagen.util.python2.Python2Parser.AtomContext;
import boa.datagen.util.python2.Python2Parser.AugassignContext;
import boa.datagen.util.python2.Python2Parser.Break_stmtContext;
import boa.datagen.util.python2.Python2Parser.ClassdefContext;
import boa.datagen.util.python2.Python2Parser.Comp_forContext;
import boa.datagen.util.python2.Python2Parser.Comp_ifContext;
import boa.datagen.util.python2.Python2Parser.Comp_iterContext;
import boa.datagen.util.python2.Python2Parser.Comp_opContext;
import boa.datagen.util.python2.Python2Parser.ComparisonContext;
import boa.datagen.util.python2.Python2Parser.Compound_stmtContext;
import boa.datagen.util.python2.Python2Parser.Continue_stmtContext;
import boa.datagen.util.python2.Python2Parser.DecoratedContext;
import boa.datagen.util.python2.Python2Parser.DecoratorContext;
import boa.datagen.util.python2.Python2Parser.DecoratorsContext;
import boa.datagen.util.python2.Python2Parser.Del_stmtContext;
import boa.datagen.util.python2.Python2Parser.DictorsetmakerContext;
import boa.datagen.util.python2.Python2Parser.Dotted_as_nameContext;
import boa.datagen.util.python2.Python2Parser.Dotted_as_namesContext;
import boa.datagen.util.python2.Python2Parser.Dotted_nameContext;
import boa.datagen.util.python2.Python2Parser.Encoding_declContext;
import boa.datagen.util.python2.Python2Parser.Eval_inputContext;
import boa.datagen.util.python2.Python2Parser.Except_clauseContext;
import boa.datagen.util.python2.Python2Parser.Exec_stmtContext;
import boa.datagen.util.python2.Python2Parser.ExprContext;
import boa.datagen.util.python2.Python2Parser.Expr_stmtContext;
import boa.datagen.util.python2.Python2Parser.ExprlistContext;
import boa.datagen.util.python2.Python2Parser.FactorContext;
import boa.datagen.util.python2.Python2Parser.File_inputContext;
import boa.datagen.util.python2.Python2Parser.Flow_stmtContext;
import boa.datagen.util.python2.Python2Parser.For_stmtContext;
import boa.datagen.util.python2.Python2Parser.FpdefContext;
import boa.datagen.util.python2.Python2Parser.FplistContext;
import boa.datagen.util.python2.Python2Parser.FuncdefContext;
import boa.datagen.util.python2.Python2Parser.Global_stmtContext;
import boa.datagen.util.python2.Python2Parser.If_stmtContext;
import boa.datagen.util.python2.Python2Parser.Import_as_nameContext;
import boa.datagen.util.python2.Python2Parser.Import_as_namesContext;
import boa.datagen.util.python2.Python2Parser.Import_fromContext;
import boa.datagen.util.python2.Python2Parser.Import_nameContext;
import boa.datagen.util.python2.Python2Parser.Import_stmtContext;
import boa.datagen.util.python2.Python2Parser.LambdefContext;
import boa.datagen.util.python2.Python2Parser.List_forContext;
import boa.datagen.util.python2.Python2Parser.List_ifContext;
import boa.datagen.util.python2.Python2Parser.List_iterContext;
import boa.datagen.util.python2.Python2Parser.ListmakerContext;
import boa.datagen.util.python2.Python2Parser.Not_testContext;
import boa.datagen.util.python2.Python2Parser.Old_lambdefContext;
import boa.datagen.util.python2.Python2Parser.Old_testContext;
import boa.datagen.util.python2.Python2Parser.Or_testContext;
import boa.datagen.util.python2.Python2Parser.ParametersContext;
import boa.datagen.util.python2.Python2Parser.Pass_stmtContext;
import boa.datagen.util.python2.Python2Parser.PowerContext;
import boa.datagen.util.python2.Python2Parser.Print_stmtContext;
import boa.datagen.util.python2.Python2Parser.Raise_stmtContext;
import boa.datagen.util.python2.Python2Parser.Return_stmtContext;
import boa.datagen.util.python2.Python2Parser.Shift_exprContext;
import boa.datagen.util.python2.Python2Parser.Simple_stmtContext;
import boa.datagen.util.python2.Python2Parser.Single_inputContext;
import boa.datagen.util.python2.Python2Parser.SliceopContext;
import boa.datagen.util.python2.Python2Parser.Small_stmtContext;
import boa.datagen.util.python2.Python2Parser.StmtContext;
import boa.datagen.util.python2.Python2Parser.SubscriptContext;
import boa.datagen.util.python2.Python2Parser.SubscriptlistContext;
import boa.datagen.util.python2.Python2Parser.SuiteContext;
import boa.datagen.util.python2.Python2Parser.TermContext;
import boa.datagen.util.python2.Python2Parser.TestContext;
import boa.datagen.util.python2.Python2Parser.Testlist1Context;
import boa.datagen.util.python2.Python2Parser.TestlistContext;
import boa.datagen.util.python2.Python2Parser.Testlist_compContext;
import boa.datagen.util.python2.Python2Parser.Testlist_safeContext;
import boa.datagen.util.python2.Python2Parser.TrailerContext;
import boa.datagen.util.python2.Python2Parser.Try_stmtContext;
import boa.datagen.util.python2.Python2Parser.VarargslistContext;
import boa.datagen.util.python2.Python2Parser.While_stmtContext;
import boa.datagen.util.python2.Python2Parser.With_itemContext;
import boa.datagen.util.python2.Python2Parser.With_stmtContext;
import boa.datagen.util.python2.Python2Parser.Xor_exprContext;
import boa.datagen.util.python2.Python2Parser.Yield_exprContext;
import boa.datagen.util.python2.Python2Parser.Yield_stmtContext;
import boa.datagen.util.python3.Python3Lexer;
import boa.datagen.util.python3.Python3Parser;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.PositionInfo;
import boa.types.Ast.Statement;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
import boa.types.Ast.Expression.ExpressionKind;

public class Python2Visitor implements Python2Listener{
	Python2Parser parser;
	Python2Lexer lexer;
	
	private String src = null;
	public static final int PY2 = 1, PY3 = 2;
	
	private PositionInfo.Builder pos = null;
	private Namespace.Builder b = Namespace.newBuilder();
	private List<boa.types.Ast.Comment> comments = new ArrayList<boa.types.Ast.Comment>();
	//private List<String> imports = new ArrayList<String>();
	//private Stack<boa.types.Ast.Expression> expressions = new Stack<boa.types.Ast.Expression>();
	protected Stack<List<boa.types.Ast.Variable>> fields = new Stack<List<boa.types.Ast.Variable>>();
	//private Stack<List<boa.types.Ast.Method>> methods = new Stack<List<boa.types.Ast.Method>>();
	//private Stack<List<boa.types.Ast.Statement>> statements = new Stack<List<boa.types.Ast.Statement>>();
	private Stack<Method.Builder> methods = new Stack<Method.Builder>();
	private Stack<Statement.Builder> statements = new Stack<Statement.Builder>();
	private Stack<Expression.Builder> expressions = new Stack<Expression.Builder>();
	private Stack<String> atoms = new Stack<String>();
	private Stack<String> imports = new Stack<String>();
	protected int astLevel = PY2;
	
	public int getAstLevel() {
		return astLevel;
	}

	public void setAstLevel(int astLevel) {
		this.astLevel = astLevel;
	}

	public Namespace getNamespaces() {
		return b.build();
	}
	
	public List<boa.types.Ast.Comment> getComments() {
		return comments;
	}

	public List<String> getImports() {
		return imports;
	}
	
	public Python2Visitor(String src) {
		this.src = src;
	}
	
	public Python2Visitor() {
	}
	
	private static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    public Python2Parser parsefile(File file) throws IOException {
        String code = readFile(file, Charset.forName("UTF-8"));
        return parse(code);
    }
    
    public Python2Parser parse(String code) {
    	lexer = new Python2Lexer(new ANTLRInputStream(code));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        parser = new Python2Parser(tokens);

        return parser;
    }
    
	public void visit(String source) {
        parser = parse(source);
        try {
			ParseTreeWalker.DEFAULT.walk(this, parser.file_input());
		}
		catch(Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}
	
	public void visit(File file) {
		try {
			parser = parsefile(file);
			ParseTreeWalker.DEFAULT.walk(this, parser.file_input());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void enterFile_input(File_inputContext ctx) {
		String pkg = "";
		b.setName(pkg);
	}

	@Override
	public void exitFile_input(File_inputContext ctx) {
		
	}
	
	Declaration.Builder db;
	@Override
	public void enterClassdef(ClassdefContext ctx) {
		db = Declaration.newBuilder();
		db.setName(ctx.NAME().getText());
		db.setKind(TypeKind.CLASS);
	}

	@Override
	public void exitClassdef(ClassdefContext ctx) {
		if(db != null)
			b.addDeclarations(db.build());
		db = null;
	}
	
	@Override
	public void enterFuncdef(FuncdefContext ctx) {		
		Method.Builder mb = Method.newBuilder();
		mb.setName(ctx.NAME().getText());
		methods.push(mb);
	}   
	
	@Override
	public void exitFuncdef(FuncdefContext ctx) {
		if(methods.isEmpty()) {
			return;
		}
		Method.Builder mbi = methods.pop();
		if(!statements.isEmpty()) {
			statements.peek().addMethods(mbi.build());
		}
		else {
//			if(!methods.isEmpty()) {
//				methods.peek().addMethods(mbi.build());
//			}
//			else {
				if(db != null) {
					db.addMethods(mbi.build());
				}
				else {
					b.addMethods(mbi.build());
				}
			//}
		}
	}	
	
	Variable.Builder vb;
	@Override
	public void enterParameters(ParametersContext ctx) {
		vb = Variable.newBuilder();	
	}

	@Override
	public void exitParameters(ParametersContext ctx) {
		vb = null;
	}
	
	@Override
	public void enterFpdef(FpdefContext ctx) {
		if(vb != null) {
			vb = Variable.newBuilder();
			vb.setName(ctx.NAME().getText());
			methods.peek().addArguments(vb.build());
		}
	}

	@Override
	public void exitFpdef(FpdefContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void enterVarargslist(VarargslistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVarargslist(VarargslistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFplist(FplistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFplist(FplistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterStmt(StmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitStmt(StmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSimple_stmt(Simple_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSimple_stmt(Simple_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSmall_stmt(Small_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSmall_stmt(Small_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExpr_stmt(Expr_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.EXPRESSION);
		sb.addNames(ctx.getText());
		statements.push(sb);
	}

	@Override
	public void exitExpr_stmt(Expr_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterAugassign(AugassignContext ctx) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN);
		eb.setVariable(ctx.getText());
		expressions.push(eb);
	}

	@Override
	public void exitAugassign(AugassignContext ctx) {
		exitExpression();
	}
	
	private void exitExpression() {
		Expression.Builder current = expressions.pop();
		if(!expressions.isEmpty()) {
			 expressions.peek().addExpressions(current.build());
		}
		else {
			if(!statements.isEmpty()) {
				Statement.Builder sb = statements.peek();
				sb.addExpressions(current.build());
			}
			else
				b.addExpressions(current.build());
		}
	}

	@Override
	public void enterPrint_stmt(Print_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.PRINT);
		sb.addNames(ctx.getText());
		statements.push(sb);
	}

	@Override
	public void exitPrint_stmt(Print_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterDel_stmt(Del_stmtContext ctx) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.DELETE);
		eb.setVariable(ctx.getText());
		expressions.push(eb);
	}

	@Override
	public void exitDel_stmt(Del_stmtContext ctx) {
		exitExpression();
	}

	@Override
	public void enterPass_stmt(Pass_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.PASS);
		statements.push(sb);
		
	}

	@Override
	public void exitPass_stmt(Pass_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterFlow_stmt(Flow_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFlow_stmt(Flow_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterBreak_stmt(Break_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.BREAK);
		statements.push(sb);
	}

	@Override
	public void exitBreak_stmt(Break_stmtContext ctx) {
		exitStatement();
	}
	
	private void exitStatement() {
		if(statements.empty()) {
			return;
		}
		Statement.Builder current = statements.pop();
		if(!statements.isEmpty()) {
			statements.peek().addStatements(current.build());
		}
		else {
			if (!methods.isEmpty())
				methods.peek().addStatements(current.build());
			else if (db != null) 
				db.addStatements(current.build());
			else 
				b.addStatements(current.build());
		}
	}

	@Override
	public void enterContinue_stmt(Continue_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.CONTINUE);
		statements.push(sb);
	}

	@Override
	public void exitContinue_stmt(Continue_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterReturn_stmt(Return_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.RETURN);
		statements.push(sb);
	}

	@Override
	public void exitReturn_stmt(Return_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterYield_stmt(Yield_stmtContext ctx) {
		
	}

	@Override
	public void exitYield_stmt(Yield_stmtContext ctx) {
		
	}

	@Override
	public void enterRaise_stmt(Raise_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.BREAK);
		statements.push(sb);
	}

	@Override
	public void exitRaise_stmt(Raise_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterImport_stmt(Import_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImport_stmt(Import_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImport_name(Import_nameContext ctx) {
		imports.push(ctx.stop.getText());
	}

	@Override
	public void exitImport_name(Import_nameContext ctx) {
		b.addImports(imports.pop());		
	}

	@Override
	public void enterImport_from(Import_fromContext ctx) {
		String mydata = ctx.getText();
		Pattern pattern = Pattern.compile("from(.*?)import.*");
		Matcher matcher = pattern.matcher(mydata);
		if (matcher.find()) {
			imports.push(ctx.stop.getText() + " From " + matcher.group(1));
		}
	}

	@Override
	public void exitImport_from(Import_fromContext ctx) {
		b.addImports(imports.pop());
		
	}

	@Override
	public void enterImport_as_name(Import_as_nameContext ctx) {
		
	}

	@Override
	public void exitImport_as_name(Import_as_nameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDotted_as_name(Dotted_as_nameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDotted_as_name(Dotted_as_nameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterImport_as_names(Import_as_namesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitImport_as_names(Import_as_namesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDotted_as_names(Dotted_as_namesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDotted_as_names(Dotted_as_namesContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDotted_name(Dotted_nameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDotted_name(Dotted_nameContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterGlobal_stmt(Global_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitGlobal_stmt(Global_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExec_stmt(Exec_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExec_stmt(Exec_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAssert_stmt(Assert_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.ASSERT);
		statements.push(sb);
	}

	@Override
	public void exitAssert_stmt(Assert_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterCompound_stmt(Compound_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCompound_stmt(Compound_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterIf_stmt(If_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.IF);
		statements.push(sb);
	}

	@Override
	public void exitIf_stmt(If_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterWhile_stmt(While_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.WHILE);
		statements.push(sb);
	}

	@Override
	public void exitWhile_stmt(While_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterFor_stmt(For_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.FOR);
		statements.push(sb);
	}

	@Override
	public void exitFor_stmt(For_stmtContext ctx) {
		exitStatement();		
	}

	@Override
	public void enterTry_stmt(Try_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.TRY);
		statements.push(sb);
	}

	@Override
	public void exitTry_stmt(Try_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterWith_stmt(With_stmtContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.WITH);
		statements.push(sb);
	}

	@Override
	public void exitWith_stmt(With_stmtContext ctx) {
		exitStatement();
	}

	@Override
	public void enterWith_item(With_itemContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitWith_item(With_itemContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExcept_clause(Except_clauseContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.CATCH);
		statements.push(sb);
		
	}

	@Override
	public void exitExcept_clause(Except_clauseContext ctx) {
		exitStatement();
	}

	@Override
	public void enterSuite(SuiteContext ctx) {
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.BLOCK);
		statements.push(sb);
	}

	@Override
	public void exitSuite(SuiteContext ctx) {
		//exitStatement();
		if(statements.empty()) {
			return;
		}
		Statement.Builder current = statements.pop();
		
		if(ctx.getParent().start.getText().equals("def")) {
			//System.out.println("Suite: " + ctx.getParent().start.getText());
			methods.peek().addStatements(current.build());
		}
		else if (!statements.isEmpty()) {
			statements.peek().addStatements(current.build());
		}
		else {
			if (db != null) 
				db.addStatements(current.build());
			else 
				b.addStatements(current.build());
		}
	}


	@Override
	public void enterTestlist_safe(Testlist_safeContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTestlist_safe(Testlist_safeContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterOld_test(Old_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitOld_test(Old_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterOld_lambdef(Old_lambdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitOld_lambdef(Old_lambdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTest(TestContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTest(TestContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterOr_test(Or_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitOr_test(Or_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAnd_test(And_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAnd_test(And_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterNot_test(Not_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNot_test(Not_testContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComparison(ComparisonContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComparison(ComparisonContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComp_op(Comp_opContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComp_op(Comp_opContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExpr(ExprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExpr(ExprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterXor_expr(Xor_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitXor_expr(Xor_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAnd_expr(And_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAnd_expr(And_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterShift_expr(Shift_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitShift_expr(Shift_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArith_expr(Arith_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitArith_expr(Arith_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTerm(TermContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTerm(TermContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFactor(FactorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFactor(FactorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPower(PowerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitPower(PowerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAtom(AtomContext ctx) {
		atoms.push(ctx.getText());
	}

	@Override
	public void exitAtom(AtomContext ctx) {
	
	}

	@Override
	public void enterListmaker(ListmakerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitListmaker(ListmakerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTestlist_comp(Testlist_compContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTestlist_comp(Testlist_compContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLambdef(LambdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLambdef(LambdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTrailer(TrailerContext ctx) {
		if(ctx.getText().startsWith(".")) {
			atoms.push(ctx.getText().substring(1));
		}
		else if(ctx.getText().equals("()")) {
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.METHODCALL);
			if(!atoms.isEmpty()) {
				eb.setMethod(atoms.pop());
			}
			else {
				eb.setVariable("Method name missing!");
			}
			expressions.push(eb);
			exitExpression();
		}
	}

	@Override
	public void exitTrailer(TrailerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSubscriptlist(SubscriptlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSubscriptlist(SubscriptlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSubscript(SubscriptContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSubscript(SubscriptContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSliceop(SliceopContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSliceop(SliceopContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterExprlist(ExprlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitExprlist(ExprlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTestlist(TestlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTestlist(TestlistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDictorsetmaker(DictorsetmakerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDictorsetmaker(DictorsetmakerContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterArglist(ArglistContext ctx) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.METHODCALL);
		if(!atoms.isEmpty()) {
			eb.setMethod(atoms.pop());
		}
		else {
			eb.setVariable("Method name missing!");
		}
		expressions.push(eb);
	}

	@Override
	public void exitArglist(ArglistContext ctx) {
		exitExpression();
	}
	
	public boolean isLiteral(String text) {
		boolean isLiteral = text.startsWith("\"")  ;
		if(!isLiteral) {
			try {
				Double.parseDouble(text);
				isLiteral =  true;
			}
			catch (Exception e){
				isLiteral = false;
			}
		}
		return isLiteral;
	}

	@Override
	public void enterArgument(ArgumentContext ctx) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.VARACCESS);
		eb.setVariable(ctx.getText());
		if(isLiteral(ctx.getText())) {
			eb.setKind(ExpressionKind.LITERAL);
		}
		if(!expressions.isEmpty()) {
			expressions.peek().addMethodArgs(eb.build());
		}	
	}
	
	@Override
	public void exitArgument(ArgumentContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterList_iter(List_iterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitList_iter(List_iterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterList_for(List_forContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitList_for(List_forContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterList_if(List_ifContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitList_if(List_ifContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComp_iter(Comp_iterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComp_iter(Comp_iterContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComp_for(Comp_forContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComp_for(Comp_forContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterComp_if(Comp_ifContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitComp_if(Comp_ifContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTestlist1(Testlist1Context ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTestlist1(Testlist1Context ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEncoding_decl(Encoding_declContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEncoding_decl(Encoding_declContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterYield_expr(Yield_exprContext ctx) {
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.YIELD);
		eb.setVariable(ctx.getText());
		expressions.push(eb);
		
	}

	@Override
	public void exitYield_expr(Yield_exprContext ctx) {
		exitExpression();
	}
	
	@Override
	public void enterEveryRule(ParserRuleContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitErrorNode(ErrorNode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitTerminal(TerminalNode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterSingle_input(Single_inputContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitSingle_input(Single_inputContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEval_input(Eval_inputContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEval_input(Eval_inputContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDecorator(DecoratorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDecorator(DecoratorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDecorators(DecoratorsContext ctx) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void exitDecorators(DecoratorsContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDecorated(DecoratedContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitDecorated(DecoratedContext ctx) {
		// TODO Auto-generated method stub
		
	}

}
