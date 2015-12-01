package boa.analysis.ds;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import boa.types.Ast.Expression;
import boa.types.Ast.Method;
import boa.types.Ast.Statement;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Control.Graph;

public class StatementIndexer {
	int size;
	Map<Integer, boa.types.Control.Node> indexToNode = new HashMap<Integer, boa.types.Control.Node>();
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();
	Method method;
	int numOfNodex = -1;

	public final Map<Integer, Node> processMethodStatements(Method method,
			Graph graph) {
		this.size = graph.getNodesCount();
		this.method = method;
		for (boa.types.Control.Node node : graph.getNodesList()) {
			indexToNode.put(node.getId(), node);
		}
		astToCFG();
		return nodes;
	}

	public final void astToCFG() {
		if (method.getStatementsCount() > 0) {
			createNode(); // start
			traverse(method.getStatementsList().get(0)); // body
			createNode(); // end
		}
	}

	private final void createNode() {
		Node node = new Node(indexToNode.get(++numOfNodex));
		nodes.put(node.node.getId(), node);
	}

	private final void createNode(Statement root) {
		Node stmtNode = new Node(indexToNode.get(++numOfNodex), root);
		nodes.put(stmtNode.node.getId(), stmtNode);
		return;
	}

	private final void createNode(Expression root) {
		Node exprNode = new Node(indexToNode.get(++numOfNodex), root);
		nodes.put(exprNode.node.getId(), exprNode);
		return;
	}

	private final void traverse(Statement root) {
		switch (root.getKind().getNumber()) {
		case StatementKind.BLOCK_VALUE:
			final List<Statement> statementsList = root.getStatementsList();
			final int statementsSize = statementsList.size();
			for (int i = 0; i < statementsSize; i++)
				traverse(statementsList.get(i));
			break;
		case StatementKind.EXPRESSION_VALUE:
			traverse(root.getExpression());
			return;
		case StatementKind.SYNCHRONIZED_VALUE:
			traverse_sync(root);
			return;
		case StatementKind.RETURN_VALUE:
			traverse_return(root);
			return;
		case StatementKind.FOR_VALUE:
			traverse_for(root);
			return;
		case StatementKind.DO_VALUE:
			traverse_do(root);
			return;
		case StatementKind.WHILE_VALUE:
			traverse_while(root);
			return;
		case StatementKind.IF_VALUE:
			traverse_if(root);
			return;
		case StatementKind.BREAK_VALUE:
			traverse_break(root);
			return;
		case StatementKind.CONTINUE_VALUE:
			traverse_continue(root);
			return;
		case StatementKind.LABEL_VALUE:
			traverse_labeled(root);
			return;
		case StatementKind.SWITCH_VALUE:
			traverse_switch(root);
			return;
			/*
			 * case StatementKind.CASE_VALUE: return traverse_;
			 */
		case StatementKind.TRY_VALUE:
			traverse_try(root);
			return;
		case StatementKind.THROW_VALUE:
			traverse_throw(root);
			return;
		case StatementKind.CATCH_VALUE:
			traverse_catch(root);
			return;
			/*
			 * case StatementKind.EMPTY_VALUE: break;
			 */
		case StatementKind.ASSERT_VALUE:
		case StatementKind.TYPEDECL_VALUE:
		case StatementKind.OTHER_VALUE:
			// CFGNode aNode = new CFGNode(root.getKind().name(),
			// CFGNode.TYPE_OTHER, "", root.getKind().name());
			createNode(root);
			return;
		default:
			System.out.println("[Error] " + root.getKind().name());
			break;
		}
	}

	private final void traverse(Expression root) {
		switch (root.getKind().getNumber()) {
		case ExpressionKind.CONDITIONAL_VALUE:
			traverse_conditional(root);
			return;
		case ExpressionKind.METHODCALL_VALUE:
			if (root.getMethod().equals("<init>")) {
				traverse_init(root);
				return;
			} else if (root.getMethod().equals("super")) {
				traverse_clinit(root);
				return;
			} else if (root.getMethod().contains("super.")) {
				traverse_super(root);
				return;
			} else {
				traverse_call(root);
				return;
			}
		case ExpressionKind.NEW_VALUE:
			traverse_instance(root);
			return;
		case ExpressionKind.NEQ_VALUE:
		case ExpressionKind.EQ_VALUE:
		case ExpressionKind.GT_VALUE:
		case ExpressionKind.GTEQ_VALUE:
		case ExpressionKind.LITERAL_VALUE:
		case ExpressionKind.LOGICAL_AND_VALUE:
		case ExpressionKind.LOGICAL_NOT_VALUE:
		case ExpressionKind.LOGICAL_OR_VALUE:
		case ExpressionKind.LT_VALUE:
		case ExpressionKind.LTEQ_VALUE:
		case ExpressionKind.ANNOTATION_VALUE:
		case ExpressionKind.ARRAYINDEX_VALUE:
		case ExpressionKind.ARRAYINIT_VALUE:
		case ExpressionKind.ASSIGN_ADD_VALUE:
		case ExpressionKind.ASSIGN_BITAND_VALUE:
		case ExpressionKind.ASSIGN_BITOR_VALUE:
		case ExpressionKind.ASSIGN_BITXOR_VALUE:
		case ExpressionKind.ASSIGN_DIV_VALUE:
		case ExpressionKind.ASSIGN_LSHIFT_VALUE:
		case ExpressionKind.ASSIGN_MOD_VALUE:
		case ExpressionKind.ASSIGN_MULT_VALUE:
		case ExpressionKind.ASSIGN_RSHIFT_VALUE:
		case ExpressionKind.ASSIGN_SUB_VALUE:
		case ExpressionKind.ASSIGN_UNSIGNEDRSHIFT_VALUE:
		case ExpressionKind.ASSIGN_VALUE:
		case ExpressionKind.BIT_AND_VALUE:
		case ExpressionKind.BIT_LSHIFT_VALUE:
		case ExpressionKind.BIT_NOT_VALUE:
		case ExpressionKind.BIT_OR_VALUE:
		case ExpressionKind.BIT_RSHIFT_VALUE:
		case ExpressionKind.BIT_UNSIGNEDRSHIFT_VALUE:
		case ExpressionKind.BIT_XOR_VALUE:
		case ExpressionKind.CAST_VALUE:
		case ExpressionKind.NEWARRAY_VALUE:
		case ExpressionKind.NULLCOALESCE_VALUE:
		case ExpressionKind.OP_ADD_VALUE:
		case ExpressionKind.OP_DEC_VALUE:
		case ExpressionKind.OP_DIV_VALUE:
		case ExpressionKind.OP_INC_VALUE:
		case ExpressionKind.OP_MOD_VALUE:
		case ExpressionKind.OP_MULT_VALUE:
		case ExpressionKind.OP_SUB_VALUE:
		case ExpressionKind.OTHER_VALUE:
		case ExpressionKind.TYPECOMPARE_VALUE:
		case ExpressionKind.VARACCESS_VALUE:
		case ExpressionKind.VARDECL_VALUE:
			if (root.getKind() == ExpressionKind.ASSIGN) {
				Expression right = root.getExpressions(1); // left is 0, right
															// is 1
				if (right.getKind() == ExpressionKind.METHODCALL) {
					String mname = right.getMethod();
					if (mname.contains("nextInt")) {
						System.out.println();
					}
				}
			}
			createNode(root);
			return;
		}
	}

	private final void traverse_instance(Expression root) {
		createNode(root);
	}

	private final void traverse_clinit(Expression root) {
		createNode(root);
	}

	private final void traverse_init(Expression root) {
		createNode(root);
	}

	private final void traverse_call(Expression root) {
		createNode(root);
	}

	private final void traverse_super(Expression root) {
		createNode(root);
	}

	private final void traverse_if(Statement root) {
		if (root.getExpression() != null) {
			if (root.getExpression().getKind() == ExpressionKind.ASSIGN) {
				System.out.println();
			}
			traverse(root.getExpression());
		}
		createNode(root);
		if (root.getStatementsCount() > 0) { // Then
			traverse(root.getStatements(0));
		}
		if (root.getStatementsCount() > 1) { // Else
			traverse(root.getStatements(1));
		}
	}

	private final void traverse_conditional(Expression root) {
		if (root.getExpressionsCount() == 3) {
			traverse(root.getExpressions(0));
		}
		createNode(root);
		if (root.getExpressionsCount() > 0) { // Then
			traverse(root.getExpressions(0));
		}
		if (root.getExpressionsCount() > 1) { // Else
			traverse(root.getExpressions(1));
		}
	}

	private final void traverse_switch(Statement root) {
		traverse(root.getExpression());
		createNode(root.getExpression());
		for (int i = 0; i < root.getStatementsCount(); i++) {
			Statement s = root.getStatements(i);
			if (s.getKind() == StatementKind.CASE) {
				traverse(s.getExpression());
			} else {
				traverse(s);
			}
		}
	}

	private final void traverse_for(Statement root) {
		for (Iterator it = root.getInitializationsList().iterator(); it
				.hasNext();) {
			Expression e = (Expression) it.next();
			traverse(e);
		}
		if (root.getInitializationsCount() == 0) { // enhanced for
			Expression e = root.getVariableDeclaration().getInitializer();
			if (e != null)
				traverse(e);
		}
		if (root.getExpression() != null)
			traverse(root.getExpression());
		createNode(root.getExpression());
		traverse(root.getStatements(0));
	}

	private final void traverse_while(Statement root) {
		if (root.getExpression() != null)
			traverse(root.getExpression());
		createNode(root.getExpression());
		traverse(root.getStatements(0));
	}

	private final void traverse_do(Statement root) {
		if (root.getExpression() != null)
			traverse(root.getExpression());
		createNode(root.getExpression());
		traverse(root.getStatements(0));
	}

	private final void traverse_labeled(Statement root) {
		traverse(root.getStatements(0));
	}

	private final void traverse_break(Statement root) {
		createNode(root);
	}

	private final void traverse_continue(Statement root) {
		createNode(root);
	}

	private final void traverse_throw(Statement root) {
		traverse(root.getExpression());
		createNode(root);
	}

	private final void traverse_return(Statement root) {
		traverse(root.getExpression());
		createNode(root);
	}

	private final void traverse_try(Statement root) {
		createNode(root);
		for (int i = 0; i < root.getStatementsCount(); i++)
			traverse(root.getStatements(i));
	}

	private final void traverse_catch(Statement root) {
		createNode(root);
		if (root.getStatementsCount() > 0)
			traverse(root.getStatements(0));
	}

	private final void traverse_sync(Statement root) {
		createNode(root.getExpression());
		traverse(root.getExpression());
		for (int i = 0; i < root.getStatementsCount(); i++)
			traverse(root.getStatements(i));
	}
}
