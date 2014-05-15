package boa.compiler;

import boa.parser.syntaxtree.*;
import boa.parser.visitor.GJNoArguDepthFirst;

/**
 * An {@link Exception} thrown for type errors in Boa programs.
 * 
 * @author anthonyu
 * @author rdyer
 * 
 */
public class TypeException extends RuntimeException {
	private static final long serialVersionUID = -5838752670934187621L;

	/**
	 * Construct a TypeException.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 */
	public TypeException(final Node n, final String text) {
		super(getMessage(n, text));
	}

	/**
	 * Construct a TypeException caused by another exception.
	 * 
	 * @param n
	 *            The {@link Node} where the error occurred
	 * @param text
	 *            A {@link String} containing the description of the error
	 * @param e
	 *            A {@link Throwable} representing the cause of this type
	 *            exception
	 */
	public TypeException(final Node n, final String text, final Throwable e) {
		super(getMessage(n, text), e);
	}

	private static String getMessage(final Node n, final String text) {
		final NodeToken first = n.accept(new FirstNodeTokenVisitor());
		final NodeToken last = n.accept(new LastNodeTokenVisitor());

		return "Error at lines " + first.beginLine + "-" + last.endLine +
				", columns " + first.beginColumn + "-" + last.endColumn + ": " + text;
	}

	private static class FirstNodeTokenVisitor extends GJNoArguDepthFirst<NodeToken> {
		@Override
		public NodeToken visit(final Start n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Program n) {
			return n.f0.elementAt(0).accept(this);
		}

		@Override
		public NodeToken visit(final Declaration n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final TypeDecl n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final StaticVarDecl n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final VarDecl n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Type n) {
			if (n.f0.which == 7)
				return (NodeToken)n.f0.choice;
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final Component n) {
			if (n.f0.present())
				return ((NodeSequence)n.f0.node).elementAt(0).accept(this);				
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final ArrayType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final TupleType n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleTupleType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final SimpleMemberList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleMember n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoTupleType n) {
			if (n.f0.present())
				return n.f0.node.accept(this);				
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoMemberList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoMember n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoFieldDecl n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final MapType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final OutputType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final ExprList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final FunctionType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final Statement n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final Assignment n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Block n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final BreakStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final ContinueStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final DoStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final EmitStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final ExprStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final ForStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final ForVarDecl n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final ForExprStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final IfStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final ResultStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final ReturnStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final SwitchStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final WhenStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final WhenKind n) {
			return (NodeToken)n.f0.choice;
		}

		@Override
		public NodeToken visit(final IdentifierList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final WhileStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final Expression n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Conjunction n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Comparison n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleExpr n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Term n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Factor n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Selector n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final Index n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final Call n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final RegexpList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Regexp n) {
			if (n.f0.present())
				return n.f0.node.accept(this);
			if (n.f1.present())
				return n.f1.node.accept(this);
			return n.f2.accept(this);
		}

		@Override
		public NodeToken visit(final Operand n) {
			switch (n.f0.which) {
			case 7:
				return ((NodeChoice)((NodeSequence)n.f0.choice).elementAt(0)).choice.accept(this);
			case 8:
				return (NodeToken)n.f0.choice;
			case 10:
				return ((NodeSequence)n.f0.choice).elementAt(0).accept(this);
			default:
				return n.f0.choice.accept(this);
			}
		}

		@Override
		public NodeToken visit(final Composite n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final PairList n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Pair n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Function n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final StatementExpr n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final Identifier n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final IntegerLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final FingerprintLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final FloatingPointLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final CharLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final StringLiteral n) {
			return (NodeToken)n.f0.choice;
		}

		@Override
		public NodeToken visit(final BytesLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final TimeLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final EmptyStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final StopStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final VisitorExpr n) {
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final VisitorType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final VisitStatement n) {
			return (NodeToken)n.f0.choice;
		}
	}

	private static class LastNodeTokenVisitor extends GJNoArguDepthFirst<NodeToken> {
		@Override
		public NodeToken visit(final Start n) {
			return n.f1;
		}

		@Override
		public NodeToken visit(final Program n) {
			return n.f0.elementAt(n.f0.size() - 1).accept(this);
		}

		@Override
		public NodeToken visit(final Declaration n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final TypeDecl n) {
			return n.f4;
		}

		@Override
		public NodeToken visit(final StaticVarDecl n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final VarDecl n) {
			return n.f4;
		}

		@Override
		public NodeToken visit(final Type n) {
			if (n.f0.which == 7) {
				final NodeSequence l = (NodeSequence)n.f0.choice;
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final Component n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final ArrayType n) {
			return n.f2.accept(this);
		}

		@Override
		public NodeToken visit(final TupleType n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleTupleType n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final SimpleMemberList n) {
			if (n.f2.present())
				return (NodeToken)n.f2.node;
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleMember n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoTupleType n) {
			return n.f3;
		}

		@Override
		public NodeToken visit(final ProtoMemberList n) {
			if (n.f2.present())
				return (NodeToken)n.f2.node;
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoMember n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final ProtoFieldDecl n) {
			if (n.f4.present())
				return ((NodeSequence)n.f4.node).elementAt(1).accept(this);
			return n.f3.accept(this);
		}

		@Override
		public NodeToken visit(final MapType n) {
			return n.f5.accept(this);
		}

		@Override
		public NodeToken visit(final OutputType n) {
			if (n.f8.present())
				return ((NodeSequence)n.f6.node).elementAt(1).accept(this);
			if (n.f7.present())
				return ((NodeSequence)((NodeChoice)n.f6.node).choice).elementAt(3).accept(this);
			if (n.f6.present())
				return ((NodeSequence)n.f6.node).elementAt(3).accept(this);
			return n.f5.accept(this);
		}

		@Override
		public NodeToken visit(final ExprList n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final FunctionType n) {
			if (n.f4.present()) {
				final NodeSequence ns = (NodeSequence)n.f4.node;
				return ns.nodes.elementAt(ns.size() - 1).accept(this);
			}
			return n.f3;
		}

		@Override
		public NodeToken visit(final Statement n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public NodeToken visit(final Assignment n) {
			return n.f3;
		}

		@Override
		public NodeToken visit(final Block n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final BreakStatement n) {
			return n.f1;
		}

		@Override
		public NodeToken visit(final ContinueStatement n) {
			return n.f1;
		}

		@Override
		public NodeToken visit(final DoStatement n) {
			return n.f6;
		}

		@Override
		public NodeToken visit(final EmitStatement n) {
			return n.f5;
		}

		@Override
		public NodeToken visit(final ExprStatement n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final ForStatement n) {
			return n.f8.accept(this);
		}

		@Override
		public NodeToken visit(final ForVarDecl n) {
			if (n.f3.present())
				return ((NodeChoice)n.f3.node).accept(this);
			if (n.f2.present())
				return n.f2.accept(this);
			return n.f1;
		}

		@Override
		public NodeToken visit(final ForExprStatement n) {
			if (n.f1.present())
				return (NodeToken)n.f1.node;
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final IfStatement n) {
			if (n.f5.present())
				return ((NodeSequence)n.f5.node).elementAt(1).accept(this);
			return n.f4.accept(this);
		}

		@Override
		public NodeToken visit(final ResultStatement n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final ReturnStatement n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final SwitchStatement n) {
			return n.f10;
		}

		@Override
		public NodeToken visit(final WhenStatement n) {
			return n.f8.accept(this);
		}

		@Override
		public NodeToken visit(final WhenKind n) {
			return (NodeToken)n.f0.choice;
		}

		@Override
		public NodeToken visit(final IdentifierList n) {
			if (n.f1.present()) {
				final NodeSequence ns = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return ns.nodes.elementAt(ns.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final WhileStatement n) {
			return n.f4.accept(this);
		}

		@Override
		public NodeToken visit(final Expression n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Conjunction n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Comparison n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.node;
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final SimpleExpr n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Term n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Factor n) {
			if (n.f1.present())
				return ((NodeChoice)n.f1.nodes.elementAt(n.f1.size() - 1)).choice.accept(this);
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Selector n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final Index n) {
			return n.f3;
		}

		@Override
		public NodeToken visit(final Call n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final RegexpList n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Regexp n) {
			return n.f2.accept(this);
		}

		@Override
		public NodeToken visit(final Operand n) {
			switch (n.f0.which) {
			case 7:
				return ((NodeSequence)n.f0.choice).elementAt(1).accept(this);
			case 8:
				return (NodeToken)n.f0.choice;
			case 10:
				return ((NodeSequence)n.f0.choice).elementAt(2).accept(this);
			default:
				return n.f0.choice.accept(this);
			}
		}

		@Override
		public NodeToken visit(final Composite n) {
			return n.f2;
		}

		@Override
		public NodeToken visit(final PairList n) {
			if (n.f1.present()) {
				final NodeSequence l = (NodeSequence)n.f1.nodes.elementAt(n.f1.size() - 1);
				return l.nodes.elementAt(l.size() - 1).accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public NodeToken visit(final Pair n) {
			return n.f2.accept(this);
		}

		@Override
		public NodeToken visit(final Function n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final StatementExpr n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final Identifier n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final IntegerLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final FingerprintLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final FloatingPointLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final CharLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final StringLiteral n) {
			return (NodeToken)n.f0.choice;
		}

		@Override
		public NodeToken visit(final BytesLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final TimeLiteral n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final EmptyStatement n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final StopStatement n) {
			return n.f1;
		}

		@Override
		public NodeToken visit(final VisitorExpr n) {
			return n.f1.accept(this);
		}

		@Override
		public NodeToken visit(final VisitorType n) {
			return n.f0;
		}

		@Override
		public NodeToken visit(final VisitStatement n) {
			return n.f3.accept(this);
		}
	}
}
