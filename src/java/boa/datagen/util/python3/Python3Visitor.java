package boa.datagen.util.python3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;


import boa.datagen.util.python3.Python3Parser.*;
import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.TypeKind;
import boa.types.Ast.Variable;
public class Python3Visitor implements Python3Listener{
	Python3Parser parser;
	Python3Lexer lexer;
	
	private String src = null;
	private String pkg = "";
	public static final int PY2 = 1, PY3 = 2;
	
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
	private Stack<String> atomEx = new Stack<String>();
	private Stack<String> expStatements = new Stack<String>();
	private Stack<String> imports = new Stack<String>();
	protected int astLevel = PY3;
	
	public boolean isPython3 = true;
	
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
		
	public Python3Visitor(String src) {
		this.src = src;
	}
	
	public Python3Visitor() {
	}
	
	private static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    public Python3Parser parsefile(File file) throws IOException {
        String code = readFile(file, Charset.forName("UTF-8"));
        return parse(code);
    }
    
    public Python3Parser parse(String code) {
    	lexer = new Python3Lexer(new ANTLRInputStream(code));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        parser = new Python3Parser(tokens);

        return parser;
    }
    
	public void visit(String source) {
		//System.out.println("-------\n" +source + "\n-------");
        parser = parse(source);
		try {
			ParseTreeWalker.DEFAULT.walk(this, parser.file_input());
		}
		catch(Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
	}
	
	public void visit(String path, String source) {
		pkg = path;
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
		b.setName(pkg);
	}

	@Override
	public void exitFile_input(File_inputContext ctx) {
		
	}
	
	Declaration.Builder db;
	@Override
	public void enterClassdef(ClassdefContext ctx) {
		if(ctx.NAME() == null) 
			return;
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
		if(ctx.NAME() == null) 
			return;
		Method.Builder mb = Method.newBuilder();
		mb.setName(ctx.NAME().getText());
		methods.push(mb);
	}  
	
	@Override
	public void exitFuncdef(FuncdefContext ctx) {
		if(methods.isEmpty())
			return;
		
		Method.Builder mbi = methods.pop();
		if(!statements.isEmpty()) {
			statements.peek().addMethods(mbi.build());
		}
		else {
			if(db != null)
				db.addMethods(mbi.build());
			else
				b.addMethods(mbi.build());
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
	public void enterTypedargslist(TypedargslistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTypedargslist(TypedargslistContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTfpdef(TfpdefContext ctx) {
		//System.out.println("Tfdef" + ctx.getText());
		if(vb != null) {
			vb = Variable.newBuilder();
			vb.setName(ctx.NAME().getText());
			if(!methods.isEmpty())
				methods.peek().addArguments(vb.build());
		}
	}

	@Override
	public void exitTfpdef(TfpdefContext ctx) {
		//System.out.println("END");
		
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
	public void enterVfpdef(VfpdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitVfpdef(VfpdefContext ctx) {
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
		
	}

	@Override
	public void exitSimple_stmt(Simple_stmtContext ctx) {
		
	}

	@Override
	public void enterSmall_stmt(Small_stmtContext ctx) {

		
	}

	@Override
	public void exitSmall_stmt(Small_stmtContext ctx) {
		
	}

	@Override
	public void enterExpr_stmt(Expr_stmtContext ctx) {
		System.out.println("Enter expr statement: "+ctx.getText());
		Statement.Builder sb = Statement.newBuilder();
		sb.setKind(Statement.StatementKind.EXPRESSION);
		//sb.addNames(ctx.getText()); // For testing purpose
		expStatements.push(ctx.getText());
		//System.out.println("@@ " + ctx.getText());
		statements.push(sb);
	}

	@Override
	public void exitExpr_stmt(Expr_stmtContext ctx) {
		
		System.out.println("Exit expr statement: "+ctx.getText());
		for(int i = 0; i < exitEx; i++) 
			exitExpression();
		exitEx = 0;
		exitStatement();
		
		e = null;
		
		if(python2Print)
			exitStatement();
	}
	
	boolean isAssign = false;
	@Override
	public void enterAssign(AssignContext ctx) {	
		System.out.println("Enter Asign: " + ctx.getText());
		if(expressions.isEmpty())
			return;
		isAssign = true;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitAssign(AssignContext ctx) {
		System.out.println("Exit assign: " + ctx.getText());
	}

	@Override
	public void enterAnnassign(AnnassignContext ctx) {

	}
	
	@Override
	public void exitAnnassign(AnnassignContext ctx) {
		
	}
	
	@Override
	public void enterAugassign(AugassignContext ctx) {
		
	}

	@Override
	public void exitAugassign(AugassignContext ctx) {
		
	}

	private void exitExpression() {
		if(expressions.isEmpty())
			return;
		System.out.println("Exit Expr: "+expressions.size());

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
		if(statements.empty())
			return;

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
		
	}

	@Override
	public void exitImport_stmt(Import_stmtContext ctx) {
		
	}

	@Override
	public void enterImport_name(Import_nameContext ctx) {
		//imports.push(ctx.stop.getText());
	}

	@Override
	public void exitImport_name(Import_nameContext ctx) {
		if(!imports.isEmpty()) {
			String i = imports.pop();
			b.addImports(i);
		}
	}

	@Override
	public void enterImport_from(Import_fromContext ctx) {
		String[] parts = new String[2];
		try {
			parts = ctx.getText().substring(4).split("import", 2);
		} catch (Exception e) {
			System.out.println("Problem Parsing Import-From Statment");
			return;
		}
		
		if(!(parts.length > 1)) 
			return;
		if(parts[1].endsWith("as" + ctx.getStop().getText())) {
			String i = null;
			try {
				i = parts[1].split("as" + ctx.getStop().getText())[0] + " AS " + ctx.getStop().getText() + " FROM " + parts[0];
			} catch (Exception e) {
				System.out.println("Continuing Import-From Statment with AS.");
			}
			if(i != null)
				imports.push(i);
			else
				imports.push(parts[1] + " FROM " + parts[0]);
		}
		else
			imports.push(parts[1] + " FROM " + parts[0]);
		
	}

	@Override
	public void exitImport_from(Import_fromContext ctx) {
		if(!imports.isEmpty()) {
			String i = imports.pop();
			b.addImports(i);
		}
	}

	@Override
	public void enterImport_as_name(Import_as_nameContext ctx) {
		if(ctx.getText().equals(ctx.getStart().getText() + "as" + ctx.getStop().getText())) {
			if(!imports.isEmpty() && imports.peek().contains(ctx.getText()))
				imports.push(imports.pop().replace(ctx.getText(), ctx.getStart().getText() + " AS " + ctx.getStop().getText()));
		}	
	}

	@Override
	public void exitImport_as_name(Import_as_nameContext ctx) {
		
	}

	@Override
	public void enterDotted_as_name(Dotted_as_nameContext ctx) {
		if(ctx.getText().endsWith("as" + ctx.getStop().getText())) { 
			if(!imports.isEmpty() && imports.peek().equals(ctx.getStop().getText()))
				imports.pop();
			String i = null;
			try {
				i = ctx.getText().split("as" + ctx.getStop().getText())[0] + " AS " + ctx.getStop().getText();
			} catch (Exception e) {
				System.out.println("Continuing Import-AS");
			}
			if(i != null)
				imports.push(i);
			else
				imports.push(ctx.getText());
		}
		else {
			imports.push(ctx.getText());
		}
	}

	@Override
	public void exitDotted_as_name(Dotted_as_nameContext ctx) {
		if(!imports.isEmpty()) {
			String i = imports.pop();
			b.addImports(i);
		}
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
	public void enterNonlocal_stmt(Nonlocal_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitNonlocal_stmt(Nonlocal_stmtContext ctx) {
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
	public void enterAsync_stmt(Async_stmtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAsync_stmt(Async_stmtContext ctx) {
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
	
//	@Override
//	public void enterElse_block(Else_blockContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void exitElse_block(Else_blockContext ctx) {
//		// TODO Auto-generated method stub
//		
//	}

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
		if(statements.empty()) 
			return;
		
		Statement.Builder current = statements.pop();
		
		if(ctx.getParent().start.getText().equals("def") && !methods.isEmpty()) {
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
	public void enterTest(TestContext ctx) {

	}
	
	@Override
	public void exitTest(TestContext ctx) {

	}

	@Override
	public void enterTest_nocond(Test_nocondContext ctx) {
		
	}

	@Override
	public void exitTest_nocond(Test_nocondContext ctx) {
				
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
	public void enterLambdef_nocond(Lambdef_nocondContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitLambdef_nocond(Lambdef_nocondContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterOr_test(Or_testContext ctx) {
	}

	@Override
	public void exitOr_test(Or_testContext ctx) {

	}

	@Override
	public void enterAnd_test(And_testContext ctx) {
		//System.out.println("AND TEST "+ ctx.getText());
	}

	@Override
	public void exitAnd_test(And_testContext ctx) {
		
	}

	@Override
	public void enterNot_test(Not_testContext ctx) {
		
	}

	@Override
	public void exitNot_test(Not_testContext ctx) {
		

	}

	@Override
	public void enterComparison(ComparisonContext ctx) {
		//System.out.println("CMP " + ctx.getText());

		
		
	}
	
	@Override
	public void exitComparison(ComparisonContext ctx) {
		
	}
	
	boolean isCondition = false;
	@Override
	public void enterComp_op(Comp_opContext ctx) {
		if(expressions.isEmpty())
			return;

		String op = ctx.getText();
		Expression.Builder eb = Expression.newBuilder();
		if(op.equals("=="))
			eb.setKind(ExpressionKind.EQ);
		else if(op.equals(">="))
			eb.setKind(ExpressionKind.GTEQ);
		else if(op.equals("<="))
			eb.setKind(ExpressionKind.LTEQ);
		else if(op.equals(">"))
			eb.setKind(ExpressionKind.GT);
		else if(op.equals("<"))
			eb.setKind(ExpressionKind.LT);
		else if(op.equals("!=") || op.equals("<>"))
			eb.setKind(ExpressionKind.NEQ);
		else
			return;
		
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		isCondition = true;
	}

	@Override
	public void exitComp_op(Comp_opContext ctx) {
		//exitExpression();
	}

	@Override
	public void enterStar_expr(Star_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitStar_expr(Star_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void enterExpr(ExprContext ctx) {
		//System.out.println("** " + ctx.getText());
		
	}

	@Override
	public void exitExpr(ExprContext ctx) {

		
	}
	
	boolean isOr = false;
	@Override
	public void enterLor(LorContext ctx) {
		isOr = true;
		
	}

	@Override
	public void exitLor(LorContext ctx) {
		// TODO Auto-generated method stub
		
	}

	boolean isAnd = false; 
	@Override
	public void enterLand(LandContext ctx) {		
		isAnd = true;
	}

	@Override
	public void exitLand(LandContext ctx) {
		
	}

	boolean isNot = false;
	@Override
	public void enterLnot(LnotContext ctx) {
		isNot = true;
	}
 
	@Override
	public void exitLnot(LnotContext ctx) {
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
		//System.out.println("AND EXPR " + ctx.getText());
	
		
	}

	@Override
	public void exitAnd_expr(And_exprContext ctx) {
		
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
		isArith = false;
			
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
		//System.out.println("factorstart");
	}

	@Override
	public void exitFactor(FactorContext ctx) {
		//System.out.println("factorend");
		
	}  
	
	boolean isArith = false; 
	@Override
	public void enterPlus(PlusContext ctx) {
		
		if (expressions.isEmpty()) 
			return;
		
		System.out.println("Enter plus: "+ctx.getText());
		if(!isArith) {
			Expression.Builder parentex = expressions.pop();
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OP_ADD);
			eb.addExpressions(parentex.build());
			expressions.push(eb);
			
			isArith = true;
		}
		
		else {
			Expression.Builder parentex = expressions.pop();
			if(!expressions.isEmpty() && expressions.peek().getKind() == ExpressionKind.OP_ADD) {
				expressions.peek().addExpressions(parentex.build());
				exitEx--;
			}
			else {
				Expression.Builder eb = Expression.newBuilder();
				eb.setKind(ExpressionKind.OP_ADD);
				eb.addExpressions(parentex.build());
				expressions.push(eb);
			}
		}
	}

	@Override
	public void exitPlus(PlusContext ctx) {
		// TODO Auto-generated method stub
		System.out.println("Exit plus: " + ctx.getText());
	}

	@Override
	public void enterMinus(MinusContext ctx) {
		if (expressions.isEmpty()) 
			return;
		
		
		if(!isArith) {
			Expression.Builder parentex = expressions.pop();
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OP_SUB);
			eb.addExpressions(parentex.build());
			expressions.push(eb);
			
			isArith = true;
		}
		
		else {
			Expression.Builder parentex = expressions.pop();
			if(!expressions.isEmpty() && expressions.peek().getKind() == ExpressionKind.OP_SUB) {
				expressions.peek().addExpressions(parentex.build());
				exitEx--;
			}
			else {
				Expression.Builder eb = Expression.newBuilder();
				eb.setKind(ExpressionKind.OP_SUB);
				eb.addExpressions(parentex.build());
				expressions.push(eb);
			}
		}
		
	}

	@Override
	public void exitMinus(MinusContext ctx) {
		
	}
	
	@Override
	public void enterPow(PowContext ctx) {
		if (expressions.isEmpty()) 
			return;
		
		
		if(!isArith) {
			Expression.Builder parentex = expressions.pop();
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OP_POW);
			eb.addExpressions(parentex.build());
			expressions.push(eb);
			
			isArith = true;
		}
		
		else {
			Expression.Builder parentex = expressions.pop();
			if(!expressions.isEmpty() && expressions.peek().getKind() == ExpressionKind.OP_POW) {
				expressions.peek().addExpressions(parentex.build());
				exitEx--;
			}
			else {
				Expression.Builder eb = Expression.newBuilder();
				eb.setKind(ExpressionKind.OP_POW);
				eb.addExpressions(parentex.build());
				expressions.push(eb);
			}
		}
	}

	@Override
	public void exitPow(PowContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPluseq(PluseqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_ADD);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitPluseq(PluseqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPower(PowerContext ctx) {
		
		
	}

	@Override
	public void exitPower(PowerContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void enterCompl(ComplContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCompl(ComplContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMult(MultContext ctx) {
		if (expressions.isEmpty()) 
			return;
		
		
		if(!isArith) {
			Expression.Builder parentex = expressions.pop();
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OP_MULT);
			eb.addExpressions(parentex.build());
			expressions.push(eb);
			
			isArith = true;
		}
		
		else {
			Expression.Builder parentex = expressions.pop();
			if(!expressions.isEmpty() && expressions.peek().getKind() == ExpressionKind.OP_MULT) {
				expressions.peek().addExpressions(parentex.build());
				exitEx--;
			}
			else {
				Expression.Builder eb = Expression.newBuilder();
				eb.setKind(ExpressionKind.OP_MULT);
				eb.addExpressions(parentex.build());
				expressions.push(eb);
			}
		}
		
	}

	@Override
	public void exitMult(MultContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAt(AtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAt(AtContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDiv(DivContext ctx) {
		if (expressions.isEmpty()) 
			return;
		
		
		if(!isArith) {
			Expression.Builder parentex = expressions.pop();
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.OP_DIV);
			eb.addExpressions(parentex.build());
			expressions.push(eb);
			
			isArith = true;
		}
		
		else {
			Expression.Builder parentex = expressions.pop();
			if(!expressions.isEmpty() && expressions.peek().getKind() == ExpressionKind.OP_DIV) {
				expressions.peek().addExpressions(parentex.build());
				exitEx--;
			}
			else {
				Expression.Builder eb = Expression.newBuilder();
				eb.setKind(ExpressionKind.OP_DIV);
				eb.addExpressions(parentex.build());
				expressions.push(eb);
			}
		}
		
	}

	@Override
	public void exitDiv(DivContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterRem(RemContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitRem(RemContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFdiv(FdivContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFdiv(FdivContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMinuseq(MinuseqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_SUB);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitMinuseq(MinuseqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterMulteq(MulteqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_MULT);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		
	}

	@Override
	public void exitMulteq(MulteqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDiveq(DiveqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_DIV);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitDiveq(DiveqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterRemeq(RemeqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_MOD);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitRemeq(RemeqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterAndeq(AndeqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_BITAND);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		
	}

	@Override
	public void exitAndeq(AndeqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterOreq(OreqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_BITOR);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		
	}

	@Override
	public void exitOreq(OreqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterXoreq(XoreqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_BITXOR);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		
	}

	@Override
	public void exitXoreq(XoreqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterLshifteq(LshifteqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_LSHIFT);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitLshifteq(LshifteqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterRshifteq(RshifteqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_RSHIFT);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
	}

	@Override
	public void exitRshifteq(RshifteqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterPowereq(PowereqContext ctx) {
		if(expressions.isEmpty())
			return;
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.ASSIGN_POW);
		eb.addExpressions(expressions.pop());
		expressions.push(eb);
		
	}

	@Override
	public void exitPowereq(PowereqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterFdiveq(FdiveqContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitFdiveq(FdiveqContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	int exitEx = 0;
	int exitArg = 0;
	
	@Override
	public void enterAtom_expr(Atom_exprContext ctx) {

		System.out.println("Enter Atom Expr: " + ctx.getText());
		atomEx.push(ctx.getText());
		
		if(isTuple) {
			isTuple=false;
			Expression.Builder ebr = Expression.newBuilder();
			ebr.setKind(ExpressionKind.TUPLE);
			expressions.push(ebr);
			exitEx++;
		}
		
		if(!isMethodArg) {	// add condition here to avoid any expression
			
			if(ctx.getText().startsWith("[")) {
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.NEWARRAY);
				expressions.push(ebr);
				exitEx++;
			}
			else if(!atomEx.isEmpty() && isLiteral(atomEx.peek())) {
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.LITERAL);
				ebr.setLiteral(atomEx.pop());
				expressions.push(ebr);
				exitEx++;	
			}		
			else if(!atomEx.isEmpty() && isVar(atomEx.peek())) {	
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.VARACCESS);
				ebr.setVariable(atomEx.pop());
				expressions.push(ebr);
				exitEx++; 
			}

		}
		
		else {

			
			if(ctx.getText().startsWith("[")) {
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.NEWARRAY);
				expressions.push(ebr);
				exitArg++;
			}
			else if(!atomEx.isEmpty() && isLiteral(atomEx.peek())) {
				//System.out.println("REL " + ctx.getText());
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.LITERAL);
				ebr.setLiteral(atomEx.pop());
				expressions.push(ebr);
				exitArg++;	
			}		
			else if(!atomEx.isEmpty() && isVar(atomEx.peek())) {	
				Expression.Builder ebr = Expression.newBuilder();
				ebr.setKind(ExpressionKind.VARACCESS);
				ebr.setVariable(atomEx.pop());
				expressions.push(ebr);
				exitArg++;
			}
		}
		
		
			
	
	}
	
	private Stack<Expression.Builder> cons = new Stack<Expression.Builder>();
	Expression.Builder e = null;
	
	@Override
	public void exitAtom_expr(Atom_exprContext ctx) {
//		if(isMethodArg)
//			return;	
		
		System.out.println("Exit Atom Expr: "+ctx.getText());

		if(isNot && !expressions.isEmpty()) {
			Expression.Builder ebr = Expression.newBuilder();
			ebr.setKind(ExpressionKind.LOGICAL_NOT);
			ebr.addExpressions(expressions.pop());
			expressions.push(ebr);
			isNot = false;
		}

		if(isCondition) { //&& !isMethodArg
			exitExpression();
			exitEx--;

			
			if(!statements.isEmpty() && !expressions.isEmpty()) { 
				if(statements.peek().getKind() == StatementKind.IF || statements.peek().getKind() == StatementKind.WHILE) {
					cons.push(expressions.pop());
					exitEx--;
					
					if(isAnd && e != null) {
						Expression.Builder fst = e;
						Expression.Builder snd = cons.pop();
						
						Expression.Builder eb = Expression.newBuilder();
						eb.setKind(ExpressionKind.LOGICAL_AND);
						eb.addExpressions(fst);
						eb.addExpressions(snd);
						cons.push(eb);
						statements.peek().clearConditions();
						isAnd = false;
						e = null;
					}
					
					else if(isOr && e != null) {
						Expression.Builder fst = e;
						Expression.Builder snd = cons.pop();
						
						Expression.Builder eb = Expression.newBuilder();
						eb.setKind(ExpressionKind.LOGICAL_OR);
						eb.addExpressions(fst);
						eb.addExpressions(snd);
						cons.push(eb);
						statements.peek().clearConditions();
						isOr = false;
						e = null;
					}
					
					while(!cons.isEmpty()) {
						e = cons.pop();
						statements.peek().addConditions(e);
					}
					
				}
				
				else {
					cons.push(expressions.pop());
					exitEx--;
					
					if(isAnd && e != null) {
						Expression.Builder fst = e;
						Expression.Builder snd = cons.pop();
						
						Expression.Builder eb = Expression.newBuilder();
						eb.setKind(ExpressionKind.LOGICAL_AND);
						eb.addExpressions(fst);
						eb.addExpressions(snd);
						expressions.push(eb);
						exitEx++;
						isAnd = false;
						e = null;
					}
					
					else if(isOr && e != null) {
						Expression.Builder fst = e;
						Expression.Builder snd = cons.pop();
						
						Expression.Builder eb = Expression.newBuilder();
						eb.setKind(ExpressionKind.LOGICAL_OR);
						eb.addExpressions(fst);
						eb.addExpressions(snd);
						expressions.push(eb);
						exitEx++;
						isOr = false;
						e = null;
					}
				}
				isCondition = false;
			}
			
			
		}	
			
	}
	 
	@Override
	public void enterAtom(AtomContext ctx) {
		atoms.push(ctx.getText());	
	}

	@Override
	public void exitAtom(AtomContext ctx) {

	}

	@Override
	public void enterTestlist_comp(Testlist_compContext ctx) {
		// TODO Auto-generated method stub
	}

	@Override
	public void exitTestlist_comp(Testlist_compContext ctx) {
		// TODO Auto-generated method stub
		
	}

	boolean trailerMethodCall = false;
	boolean isMethodCall = false;
	@Override
	public void enterTrailer(TrailerContext ctx) {
		
		if(ctx.getText().startsWith(".")) {
			trailerMethodCall = true;
			atoms.push(ctx.getText().substring(1));
		}
		
		else if(ctx.getText().equals("()")) {		
			trailerMethodCall = false;
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.METHODCALL);
			
			
			if(!atoms.isEmpty())
				eb.setMethod(atoms.pop());
			else
				eb.setVariable("Method name missing!");
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
		//System.out.println("%% " + ctx.getText());
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
		System.out.println("Enter Arglist " + ctx.getText());
		Expression.Builder eb = Expression.newBuilder();
		eb.setKind(ExpressionKind.METHODCALL);
		
		if(!atoms.isEmpty()) {
			eb.setMethod(atoms.pop());
		}
		else {
			eb.setVariable("Method name missing!");
		}
		
		if(trailerMethodCall && !atomEx.isEmpty()) {
			String fullEx = atomEx.pop();
			//System.out.println("@@ " + fullEx);
			String trailer = "";
			//System.out.println(eb.getMethod() +" " + trailer);
			if(fullEx.indexOf(eb.getMethod()) > 0) {
				trailer = fullEx.substring(0, fullEx.indexOf(eb.getMethod()));
				if(trailer.endsWith(".")) {
					//String[] parts = trailer.split(".");
					trailer = trailer.substring(0, trailer.length() - 1);
				}
				Expression.Builder eb2 = Expression.newBuilder();
				eb2.setKind(ExpressionKind.VARACCESS);
				eb2.setVariable(trailer);
				eb.addExpressions(eb2);
			}
		}
		expressions.push(eb);
		
	}

	@Override
	public void exitArglist(ArglistContext ctx) {
		exitEx ++;
		trailerMethodCall = false;
	}
	
	boolean isMethodArg = false;
	@Override
	public void enterArgument(ArgumentContext ctx) {
		System.out.println("Enter Argument " + ctx.getText());
		isMethodArg = true;
	}

	@Override
	public void exitArgument(ArgumentContext ctx) {
		
		for(int i = 0; i < exitArg-1; i++) 
			exitExpression();
		exitArg = 0;
		
		isMethodArg = false;
		System.out.println("Exit argument: "+ctx.getText());
		
		if(expressions.isEmpty()) 
			return;
		
		Expression.Builder e = expressions.pop();
		
		
		
		if(!expressions.isEmpty()) //i == exitArg - 1 && //  && expressions.peek().getKind() == ExpressionKind.METHODCALL
			expressions.peek().addMethodArgs(e);
		
//		for(int i = 0; i < exitArg; i++) {
//			if(expressions.isEmpty()) 
//				continue;
//			Expression.Builder e = expressions.pop();
//				
//			
//			
//			if(!expressions.isEmpty()) //i == exitArg - 1 && //  && expressions.peek().getKind() == ExpressionKind.METHODCALL
//				expressions.peek().addMethodArgs(e);
//			else
//				exitExpression();
//				
//		}
//		
//		
//		exitArg = 0;
//		System.out.println("AA END");
	}
	
	public boolean isLiteral(String text) {
		if (text == null || text.length() == 0)
		    return false;
		boolean isLiteral = text.startsWith("\"") || text.startsWith("\'");
		if(!isLiteral) {
			try {
				Double.parseDouble(text);
				isLiteral =  true;
			}
			catch (Exception e) {
				isLiteral = false;
			}
		}
		return isLiteral;
	}
	
	public boolean isVar(String s) { 
		if (s == null || s.length() == 0)
		    return false;
		
		if(isLiteral(s) || !s.matches("^[^\\d\\W]\\w*\\Z"))
			return false;
		
		return true;		
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
		
	}

	@Override
	public void exitComp_if(Comp_ifContext ctx) {
		
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
	public void enterYield_arg(Yield_argContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitYield_arg(Yield_argContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterCalldef(CalldefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitCalldef(CalldefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterEveryRule(ParserRuleContext arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext arg0) {
		// TODO Auto-generated method stub
		
	}
	
	boolean python2Print = false;
	@Override
	public void visitErrorNode(ErrorNode arg0) {
		//System.out.println(arg0.getText());
		String var = arg0.getText().toString();
				
		if(var.equals("print")) {
			Statement.Builder sb = Statement.newBuilder();
			sb.setKind(Statement.StatementKind.PRINT);
			statements.push(sb);
			python2Print = true;
			
		}
		
		isPython3 = false;
		return;
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

	@Override
	public void enterAsync_funcdef(Async_funcdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitAsync_funcdef(Async_funcdefContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTestlist_star_expr(Testlist_star_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitTestlist_star_expr(Testlist_star_exprContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	Boolean isArgAssign = false;
	@Override
	public void enterArgeq(ArgeqContext ctx) {
		
		System.out.println("Enter Arg Equal: "+ expressions.peek().getVariable());
		
		if(!expressions.isEmpty()) {
			//System.out.println("CONT " + expressions.peek().getVariable());
			
			Expression.Builder eb = Expression.newBuilder();
			eb.setKind(ExpressionKind.ASSIGN);
			eb.addExpressions(expressions.pop());
			expressions.push(eb);
			isArgAssign = true;
		}				
		

	}

	@Override
	public void exitArgeq(ArgeqContext ctx) {
		// TODO Auto-generated method stub
	}

	public boolean isTuple=false;
	@Override
	public void enterTuple_start(Tuple_startContext ctx) {
		// TODO Auto-generated method stub
		System.out.println("Enter tuple start "+ctx.getText());
		isTuple=true;
		
//		Expression.Builder eb = Expression.newBuilder();
//		eb.setKind(ExpressionKind.TUPLE);
//		//eb.addExpressions(expressions.pop());
//		expressions.push(eb);
		
	}

	@Override
	public void exitTuple_start(Tuple_startContext ctx) {
		// TODO Auto-generated method stub
		System.out.println("Exit tuple start "+ctx.getText());
		
	}

	@Override
	public void enterTuple_end(Tuple_endContext ctx) {
		// TODO Auto-generated method stub
		System.out.println("Enter tuple end "+ctx.getText());
		
	}

	@Override
	public void exitTuple_end(Tuple_endContext ctx) {
		// TODO Auto-generated method stub
		System.out.println("Exit tuple start "+ctx.getText());
		
		isTuple=false;
//		
//		for(int i = 0; i < exitEx; i++) 
//			exitExpression();
//		exitArg = 0;
//		
//		isMethodArg = false;
//		System.out.println("Exit argument: "+ctx.getText());
//		
//		if(expressions.isEmpty()) 
//			return;
//		
//		Expression.Builder e = expressions.pop();
//		
//		
//		
//		if(!expressions.isEmpty()) //i == exitArg - 1 && //  && expressions.peek().getKind() == ExpressionKind.METHODCALL
//			expressions.peek().addMethodArgs(e);
		
	}

}
