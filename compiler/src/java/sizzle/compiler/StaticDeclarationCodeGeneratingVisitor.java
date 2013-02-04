package sizzle.compiler;

import java.io.IOException;

import org.antlr.stringtemplate.StringTemplate;

import sizzle.parser.syntaxtree.*;
import sizzle.parser.visitor.GJDepthFirst;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleType;

/**
 * Prescan the Sizzle program and generate initializer code for any static
 * variables.
 * 
 * @author anthonyu
 * 
 */
public class StaticDeclarationCodeGeneratingVisitor extends GJDepthFirst<String, SymbolTable> {
	private final CodeGeneratingVisitor codegenerator;

	public StaticDeclarationCodeGeneratingVisitor(final CodeGeneratingVisitor codegenerator) throws IOException {
		this.codegenerator = codegenerator;
	}

	@Override
	public String visit(final Start n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Program n, final SymbolTable argu) {
		final StringBuilder sb = new StringBuilder();

		for (final Node node : n.f0.nodes) {
			final NodeChoice nodeChoice = (NodeChoice) node;
			switch (nodeChoice.which) {
			case 0: // declaration
				final String accept = nodeChoice.choice.accept(this, argu);

				if (accept != null)
					sb.append(accept);
				break;
			case 1: // statement
				break;
			case 2: // proto
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return sb.toString();
	}

	@Override
	public String visit(final Declaration n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // type declaration
			return null;
		case 1: // static var declaration
		case 2: // variable declaration
			return n.f0.choice.accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	@Override
	public String visit(final TypeDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StaticVarDecl n, final SymbolTable argu) {
		return "private static " + this.codegenerator.visit(n.f1, argu);
	}

	@Override
	public String visit(final VarDecl n, final SymbolTable argu) {
		final SizzleType type = argu.get(n.f0.f0.tokenImage);

		if (type instanceof SizzleTable)
			return null;

		final StringTemplate st = this.codegenerator.stg.getInstanceOf("VarDecl");

		st.setAttribute("id", n.f0.f0.tokenImage);
		st.setAttribute("type", type.toJavaType());

		return st.toString();
	}

	@Override
	public String visit(final Type n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Component n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ArrayType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final TupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoTupleType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ProtoFieldDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final MapType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final OutputType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ExprList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final FunctionType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Statement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Assignment n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Block n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final BreakStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ContinueStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final DoStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final EmitStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ExprStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ForStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ForVarDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ForExprStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final IfStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ResultStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final ReturnStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SwitchStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhenStatement n, final SymbolTable argu) {
		return n.f8.accept(this, argu);
	}

	@Override
	public String visit(final WhenKind n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Expression n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Conjunction n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Comparison n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final SimpleExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Term n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Factor n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Selector n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Index n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Call n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final RegexpList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Regexp n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Operand n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Composite n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final PairList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Pair n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Function n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StatementExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Proto n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final Identifier n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final IntegerLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final FingerprintLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final FloatingPointLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final CharLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StringLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final BytesLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final TimeLiteral n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final EmptyStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final StopStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final VisitorExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final VisitorType n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String visit(final VisitStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}
}
