package boa.compiler;

import java.util.Vector;

import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;

import boa.parser.visitor.GJNoArguDepthFirst;

public class ParseTreeAdapter extends GJNoArguDepthFirst<Node> {
	private static class FirstNodeTokenVisitor extends GJNoArguDepthFirst<boa.parser.syntaxtree.NodeToken> {
		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Start n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Program n) {
			return n.f0.elementAt(0).accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Declaration n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TypeDecl n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StaticVarDecl n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VarDecl n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Type n) {
			if (n.f0.which == 7)
				return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Component n) {
			if (n.f0.present())
				return ((boa.parser.syntaxtree.NodeSequence)n.f0.node).elementAt(0).accept(this);				
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ArrayType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TupleType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.MemberList n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Member n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.MapType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.OutputType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ExprList n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FunctionType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Statement n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Assignment n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Block n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.BreakStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ContinueStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.DoStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.EmitStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ExprStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForVarDecl n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForExprStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IfStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ReturnStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.SwitchStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhenStatement n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhenKind n) {
			return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IdentifierList n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhileStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Expression n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Conjunction n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Comparison n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.SimpleExpr n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Term n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Factor n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Selector n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Index n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Call n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Operand n) {
			switch (n.f0.which) {
			case 9:
				return (boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)n.f0.choice).elementAt(0)).choice;
			case 10:
				return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
			case 12:
				return (boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeSequence)n.f0.choice).elementAt(0);
			default:
				return n.f0.choice.accept(this);
			}
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Composite n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.PairList n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Pair n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Function n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Identifier n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IntegerLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FingerprintLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FloatingPointLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.CharLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StringLiteral n) {
			return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TimeLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.EmptyStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StopStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitorExpr n) {
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitorType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitStatement n) {
			return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
		}
	}

	private static class LastNodeTokenVisitor extends GJNoArguDepthFirst<boa.parser.syntaxtree.NodeToken> {
		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Start n) {
			return n.f1;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Program n) {
			return n.f0.nodes.lastElement().accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Declaration n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TypeDecl n) {
			return n.f4;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StaticVarDecl n) {
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VarDecl n) {
			return n.f4;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Type n) {
			if (n.f0.which == 7) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f0.choice;
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Component n) {
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ArrayType n) {
			return n.f2.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TupleType n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.MemberList n) {
			if (n.f2.present())
				return (boa.parser.syntaxtree.NodeToken)n.f2.node;
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Member n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.MapType n) {
			return n.f5.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.OutputType n) {
			if (n.f7.present())
				return (boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeSequence)n.f7.node).elementAt(3);
			if (n.f6.present())
				return ((boa.parser.syntaxtree.NodeSequence)n.f6.node).elementAt(1).accept(this);
			return n.f5.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ExprList n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FunctionType n) {
			if (n.f4.present()) {
				final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f4.node;
				return ns.nodes.lastElement().accept(this);
			}
			return n.f3;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Statement n) {
			return n.f0.choice.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Assignment n) {
			return n.f3;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Block n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.BreakStatement n) {
			return n.f1;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ContinueStatement n) {
			return n.f1;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.DoStatement n) {
			return n.f6;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.EmitStatement n) {
			return n.f5;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ExprStatement n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForStatement n) {
			return n.f8.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForVarDecl n) {
			if (n.f3.present()) {
				switch (((boa.parser.syntaxtree.NodeChoice)n.f3.node).which) {
				case 0:
					return ((boa.parser.syntaxtree.NodeSequence)((boa.parser.syntaxtree.NodeChoice)n.f3.node).choice).elementAt(1).accept(this);
				default:
					return ((boa.parser.syntaxtree.NodeChoice)n.f3.node).choice.accept(this);
				}
			}
			if (n.f2.present())
				return n.f2.accept(this);
			return n.f1;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ForExprStatement n) {
			if (n.f1.present())
				return (boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice;
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IfStatement n) {
			if (n.f5.present())
				return ((boa.parser.syntaxtree.NodeSequence)n.f5.node).elementAt(1).accept(this);
			return n.f4.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.ReturnStatement n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.SwitchStatement n) {
			return n.f10;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhenStatement n) {
			return n.f8.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhenKind n) {
			return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IdentifierList n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return ns.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.WhileStatement n) {
			return n.f4.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Expression n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Conjunction n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Comparison n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.node;
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.SimpleExpr n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Term n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Factor n) {
			if (n.f1.present())
				return ((boa.parser.syntaxtree.NodeChoice)n.f1.nodes.lastElement()).choice.accept(this);
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Selector n) {
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Index n) {
			return n.f3;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Call n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Operand n) {
			switch (n.f0.which) {
			case 9:
				return ((boa.parser.syntaxtree.NodeSequence)n.f0.choice).elementAt(1).accept(this);
			case 10:
				return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
			case 12:
				return (boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeSequence)n.f0.choice).elementAt(2);
			default:
				return n.f0.choice.accept(this);
			}
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Composite n) {
			return n.f2;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.PairList n) {
			if (n.f1.present()) {
				final boa.parser.syntaxtree.NodeSequence l = (boa.parser.syntaxtree.NodeSequence)n.f1.nodes.lastElement();
				return l.nodes.lastElement().accept(this);
			}
			return n.f0.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Pair n) {
			return n.f2.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Function n) {
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.Identifier n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.IntegerLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FingerprintLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.FloatingPointLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.CharLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StringLiteral n) {
			return (boa.parser.syntaxtree.NodeToken)n.f0.choice;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.TimeLiteral n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.EmptyStatement n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.StopStatement n) {
			return n.f1;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitorExpr n) {
			return n.f1.accept(this);
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitorType n) {
			return n.f0;
		}

		@Override
		public boa.parser.syntaxtree.NodeToken visit(final boa.parser.syntaxtree.VisitStatement n) {
			return n.f3.accept(this);
		}
	}

	private static FirstNodeTokenVisitor firstVisitor = new FirstNodeTokenVisitor();
	private static LastNodeTokenVisitor lastVisitor = new LastNodeTokenVisitor();

	/** {@inheritDoc} */
	@Override
	public Node visit(boa.parser.syntaxtree.Start n) {
		return new Start((Program)n.f0.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Program n) {
		final Program p = new Program();
		p.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		for (final boa.parser.syntaxtree.Node nl : n.f0.nodes)
			p.addStatement((Statement)nl.accept(this));

		return p;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Declaration n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StaticVarDecl n) {
		final VarDeclStatement var = (VarDeclStatement)n.f1.accept(this);
		return new VarDeclStatement(true, var.getId(), var.getType(), var.getInitializer()).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VarDecl n) {
		final Node t;
		if (n.f2.present())
			t = n.f2.accept(this);
		else
			t = null;

		final Node initializer;
		if (n.f3.present())
			initializer = ((boa.parser.syntaxtree.NodeSequence)n.f3.node).elementAt(1).accept(this);
		else
			initializer = null;

		return new VarDeclStatement((Identifier)n.f0.accept(this), (AbstractType)t, (Expression)initializer).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Type n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Component n) {
		if (n.f0.present())
			return new Component((Identifier)n.f0.accept(this), (AbstractType)n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		return new Component((AbstractType)n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ArrayType n) {
		return new ArrayType((Component) n.f2.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.TupleType n) {
		final TupleType t = new TupleType();
		t.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present()) {
			final boa.parser.syntaxtree.MemberList ml = (boa.parser.syntaxtree.MemberList)n.f1.node;

			t.addMember((Component) ml.f0.accept(this));

			if (ml.f1.present())
				for (final boa.parser.syntaxtree.Node c : ml.f1.nodes)
					t.addMember((Component) c.accept(this));
		}

		return t;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Member n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.MapType n) {
		return new MapType((Component)n.f2.accept(this), (Component)n.f5.accept(this)).setPositions(n.f0, lastVisitor.visit(n.f5));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StackType n) {
		return new StackType((Component) n.f2.accept(this)).setPositions(n.f0, lastVisitor.visit(n.f2));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SetType n) {
		return new SetType((Component) n.f2.accept(this)).setPositions(n.f0, lastVisitor.visit(n.f2));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.OutputType n) {
		final OutputType t;
		if (n.f6.present())
			t = new OutputType((Identifier)n.f1.accept(this), (Component)n.f5.accept(this), (Component)((boa.parser.syntaxtree.NodeSequence)n.f6.node).elementAt(1).accept(this));
		else
			t = new OutputType((Identifier)n.f1.accept(this), (Component)n.f5.accept(this));
		t.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f2.present()) {
			final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f2.node;
			boa.parser.syntaxtree.ExprList el = (boa.parser.syntaxtree.ExprList)ns.elementAt(1);

			t.addArg((Expression)el.f0.accept(this));

			if (el.f1.present())
				for (final boa.parser.syntaxtree.Node ns2 : el.f1.nodes)
					t.addArg((Expression)((boa.parser.syntaxtree.NodeSequence)ns2).elementAt(1).accept(this));
		}

		if (n.f3.present())
			for (final boa.parser.syntaxtree.Node ns : n.f3.nodes)
				t.addIndice((Component)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));

		return t;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.FunctionType n) {
		final FunctionType f;
		if (n.f4.present())
			f = new FunctionType((AbstractType)((boa.parser.syntaxtree.NodeSequence)n.f4.node).elementAt(1).accept(this));
		else
			f = new FunctionType();
		f.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f2.present()) {
			final boa.parser.syntaxtree.NodeSequence nodes = (boa.parser.syntaxtree.NodeSequence)n.f2.node;

			final Component c = new Component((Identifier)nodes.elementAt(0).accept(this), (AbstractType)nodes.elementAt(2).accept(this));
			c.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Identifier)nodes.elementAt(0)), lastVisitor.visit((boa.parser.syntaxtree.Type)nodes.elementAt(2)));
			f.addArg(c);

			final boa.parser.syntaxtree.NodeListOptional paramList = (boa.parser.syntaxtree.NodeListOptional)nodes.elementAt(3);
			if (paramList.present())
				for (final boa.parser.syntaxtree.Node paramNodes : paramList.nodes) {
					boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)paramNodes;
					final Component c2 = new Component((Identifier)ns.elementAt(1).accept(this), (AbstractType)ns.elementAt(3).accept(this));
					c2.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Identifier)ns.elementAt(1)), lastVisitor.visit((boa.parser.syntaxtree.Type)ns.elementAt(3)));
					f.addArg(c2);
				}
		}

		return f;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Statement n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Assignment n) {
		return new AssignmentStatement((Factor)n.f0.accept(this), (Expression)n.f2.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Block n) {
		Block b = new Block();
		b.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node s : n.f1.nodes)
				b.addStatement((Statement)s.accept(this));
		return b;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.BreakStatement n) {
		return new BreakStatement().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ContinueStatement n) {
		return new ContinueStatement().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.DoStatement n) {
		return new DoStatement((Expression)n.f4.accept(this), ensureBlock((Statement)n.f1.accept(this))).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.EmitStatement n) {
		final EmitStatement es;

		if (n.f4.present())
			es = new EmitStatement((Identifier)n.f0.accept(this), (Expression)n.f3.accept(this), (Expression)((boa.parser.syntaxtree.NodeSequence)n.f4.node).elementAt(1).accept(this));
		else
			es = new EmitStatement((Identifier)n.f0.accept(this), (Expression)n.f3.accept(this));
		es.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node ns : n.f1.nodes)
				es.addIndice((Expression)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));

		return es;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ExprStatement n) {
		if (n.f1.present())
			return new PostfixStatement((Expression)n.f0.accept(this), ((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice).tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		return new ExprStatement((Expression)n.f0.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ForVarDecl n) {
		final Node t;
		if (n.f2.present())
			t = n.f2.accept(this);
		else
			t = null;

		final Node initializer;
		if (n.f3.present()) {
			final boa.parser.syntaxtree.NodeChoice nc = (boa.parser.syntaxtree.NodeChoice)n.f3.node;
			switch (nc.which) {
			case 0:
				initializer = ((boa.parser.syntaxtree.NodeSequence)nc.choice).elementAt(1).accept(this);
				break;
			case 1:
				initializer = nc.choice.accept(this);
				break;
			default:
				throw new RuntimeException("unexpected choice " + nc.which + " is " + nc.choice.getClass());
			}
		} else {
			initializer = null;
		}

		return new VarDeclStatement((Identifier)n.f0.accept(this), (AbstractType)t, (Expression)initializer).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ForExprStatement n) {
		if (n.f1.present())
			return new PostfixStatement((Expression)n.f0.accept(this), ((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice).tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		return new ExprStatement((Expression)n.f0.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ForStatement n) {
		final Statement init;
		if (n.f2.present())
			init = (Statement)((boa.parser.syntaxtree.NodeChoice)n.f2.node).choice.accept(this);
		else
			init = null;

		final Expression condition;
		if (n.f4.present())
			condition = (Expression)n.f4.node.accept(this);
		else
			condition = null;

		final Statement update;
		if (n.f6.present())
			update = (Statement)((boa.parser.syntaxtree.NodeChoice)n.f6.node).choice.accept(this);
		else
			update = null;

		return new ForStatement(init, condition, update, ensureBlock((Statement)n.f8.accept(this))).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.IfStatement n) {
		if (n.f5.present())
			return new IfStatement((Expression)n.f2.accept(this), ensureBlock((Statement)n.f4.accept(this)), ensureBlock((Statement)((boa.parser.syntaxtree.NodeSequence)n.f5.node).elementAt(1).accept(this))).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		return new IfStatement((Expression)n.f2.accept(this), ensureBlock((Statement)n.f4.accept(this))).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ReturnStatement n) {
		if (n.f1.present())
			return new ReturnStatement((Expression) n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		return new ReturnStatement().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SwitchStatement n) {
		final Block defaultBody = new Block();
		final SwitchCase dfault = new SwitchCase(true, defaultBody);
		defaultBody.addStatement((Statement)n.f8.accept(this));
		if (n.f9.present()) {
			for (boa.parser.syntaxtree.Node st : n.f9.nodes)
				defaultBody.addStatement((Statement)st.accept(this));
			defaultBody.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Statement)n.f8), lastVisitor.visit((boa.parser.syntaxtree.Statement)n.f9.nodes.lastElement()));
		} else {
			defaultBody.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Statement)n.f8), lastVisitor.visit((boa.parser.syntaxtree.Statement)n.f8));
		}

		final SwitchStatement s = new SwitchStatement((Expression)n.f2.accept(this), dfault);
		s.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f5.present())
			for (boa.parser.syntaxtree.Node st : n.f5.nodes) {
				boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)st;
				final Block body = new Block();
				final SwitchCase casest = new SwitchCase(false, body);
				s.addCase(casest);

				casest.addCase((Expression)ns.elementAt(1).accept(this));
				body.addStatement((Statement) ns.elementAt(4).accept(this));

				boa.parser.syntaxtree.NodeListOptional opt1 = (boa.parser.syntaxtree.NodeListOptional)ns.elementAt(2);
				if (opt1.present()) {
					for (boa.parser.syntaxtree.Node ns2 : opt1.nodes)
						casest.addCase((Expression) ((boa.parser.syntaxtree.NodeSequence)ns2).elementAt(1).accept(this));
					casest.setPositions((boa.parser.syntaxtree.NodeToken)ns.elementAt(0), lastVisitor.visit((boa.parser.syntaxtree.Expression)((boa.parser.syntaxtree.NodeSequence)opt1.nodes.lastElement()).elementAt(1)));
				} else {
					casest.setPositions((boa.parser.syntaxtree.NodeToken)ns.elementAt(0), lastVisitor.visit((boa.parser.syntaxtree.Expression)ns.elementAt(1)));
				}

				boa.parser.syntaxtree.NodeListOptional opt2 = (boa.parser.syntaxtree.NodeListOptional)ns.elementAt(5);
				if (opt2.present()) {
					for (boa.parser.syntaxtree.Node stmt : opt2.nodes)
						body.addStatement((Statement) stmt.accept(this));
					body.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Statement)ns.elementAt(4)), lastVisitor.visit((boa.parser.syntaxtree.Statement)opt2.nodes.lastElement()));
				} else {
					body.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Statement)ns.elementAt(4)), lastVisitor.visit((boa.parser.syntaxtree.Statement)ns.elementAt(4)));
				}
			}

		return s;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.WhenStatement n) {
		final Component var = new Component((Identifier)n.f2.accept(this), (AbstractType)n.f4.accept(this));
		var.setPositions(firstVisitor.visit(n.f2), lastVisitor.visit(n.f4));
		final Expression condition = (Expression)n.f6.accept(this);
		condition.setPositions(firstVisitor.visit(n.f6), lastVisitor.visit(n.f6));
		final Block body = ensureBlock((Statement)n.f8.accept(this));
		body.setPositions(firstVisitor.visit(n.f8), lastVisitor.visit(n.f8));

		switch (n.f0.f0.which) {
		case 0:
			return new ForeachStatement(var, condition, body).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		case 1:
			return new IfAllStatement(var, condition, body).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		case 2:
			return new ExistsStatement(var, condition, body).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		default:
			throw new RuntimeException("unexpected choice " + n.f0.f0.which + " is " + n.f0.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.WhileStatement n) {
		return new WhileStatement((Expression)n.f2.accept(this), ensureBlock((Statement)n.f4.accept(this))).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Expression n) {
		final Expression e = new Expression((Conjunction)n.f0.accept(this));
		e.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node c : n.f1.nodes)
				e.addRhs((Conjunction)((boa.parser.syntaxtree.NodeSequence)c).elementAt(1).accept(this));

		return e;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Conjunction n) {
		final Conjunction c = new Conjunction((Comparison)n.f0.accept(this));
		c.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node c2 : n.f1.nodes)
				c.addRhs((Comparison)((boa.parser.syntaxtree.NodeSequence)c2).elementAt(1).accept(this));

		return c;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Comparison n) {
		if (n.f1.present())
			return new Comparison((SimpleExpr)n.f0.accept(this), ((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)n.f1.node).elementAt(0)).choice).tokenImage, (SimpleExpr)((boa.parser.syntaxtree.NodeSequence)n.f1.node).elementAt(1).accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		return new Comparison((SimpleExpr)n.f0.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SimpleExpr n) {
		final SimpleExpr se = new SimpleExpr((Term)n.f0.accept(this));
		se.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node ns : n.f1.nodes) {
				se.addOp(((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(0)).choice).tokenImage);
				se.addRhs((Term)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));
			}

		return se;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Term n) {
		final Term t = new Term((Factor)n.f0.accept(this));
		t.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node ns : n.f1.nodes) {
				t.addOp(((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(0)).choice).tokenImage);
				t.addRhs((Factor)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));
			}

		return t;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Factor n) {
		final Factor f = new Factor((Operand)n.f0.accept(this));
		f.setPositions(firstVisitor.visit(n.f0), lastVisitor.visit(n));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node op : n.f1.nodes)
				f.addOp(op.accept(this));

		return f;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Selector n) {
		return new Selector((Identifier)n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Index n) {
		if (n.f2.present()) {
			final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f2.node;
			return new Index((Expression) n.f1.accept(this), (Expression)ns.elementAt(1).accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		}

		return new Index((Expression) n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Call n) {
		final Call c = new Call();
		c.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present()) {
			final boa.parser.syntaxtree.ExprList el = (boa.parser.syntaxtree.ExprList)n.f1.node;

			c.addArg((Expression)el.f0.accept(this));

			if (el.f1.present())
				for (boa.parser.syntaxtree.Node ns : el.f1.nodes)
					c.addArg((Expression)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));
		}

		return c;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Operand n) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // string literal
		case 2: // char literal
		case 3: // time literal
		case 4: // integer literal
		case 5: // floating point literal
		case 6: // composite
		case 7: // visitor
		case 8: // function
		case 11: // statement expression
			return n.f0.choice.accept(this);
		case 9: // unary operator
			final Vector<boa.parser.syntaxtree.Node> nodes = ((boa.parser.syntaxtree.NodeSequence) n.f0.choice).nodes;
			return new UnaryFactor(((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)nodes.elementAt(0)).choice).tokenImage, (Factor)nodes.elementAt(1).accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		case 12: // paren
			final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence) n.f0.choice;
			return new ParenExpression((Expression)ns.nodes.elementAt(1).accept(this)).setPositions((boa.parser.syntaxtree.NodeToken)ns.nodes.elementAt(0), (boa.parser.syntaxtree.NodeToken)ns.nodes.elementAt(2));
		case 10: // $
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Composite n) {
		final Composite c = new Composite(false);
		c.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));

		if (n.f1.present())
			switch (((boa.parser.syntaxtree.NodeChoice)n.f1.node).which) {
			case 0:
				final boa.parser.syntaxtree.PairList pl = (boa.parser.syntaxtree.PairList)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice;

				c.addPair((Pair)pl.f0.accept(this));

				if (pl.f1.present())
					for (boa.parser.syntaxtree.Node ns : pl.f1.nodes)
						c.addPair((Pair)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));

				return c;
			case 1:
				final boa.parser.syntaxtree.ExprList el = (boa.parser.syntaxtree.ExprList)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice;

				c.addExpr((Expression)el.f0.accept(this));

				if (el.f1.present())
					for (boa.parser.syntaxtree.Node ns : el.f1.nodes)
						c.addExpr((Expression)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));

				return c;
			case 2:
				return new Composite(true).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
			default:
				final boa.parser.syntaxtree.NodeChoice nc = (boa.parser.syntaxtree.NodeChoice)n.f1.node;
				throw new RuntimeException("unexpected choice " + nc.which + " is " + nc.choice.getClass());
			}

		return new Composite(true).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Pair n) {
		return new Pair((Expression)n.f0.accept(this), (Expression)n.f2.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Function n) {
		return new FunctionExpression((FunctionType)n.f0.accept(this), (Block)n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Identifier n) {
		return new Identifier(n.f0.tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.IntegerLiteral n) {
		return new IntegerLiteral(n.f0.tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.FloatingPointLiteral n) {
		return new FloatLiteral(n.f0.tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.CharLiteral n) {
		return new CharLiteral(n.f0.tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StringLiteral n) {
		switch (n.f0.which) {
		case 0: // STRING
			return new StringLiteral(((boa.parser.syntaxtree.NodeToken) n.f0.choice).tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		case 1: // REGEX
			final String s = ((boa.parser.syntaxtree.NodeToken) n.f0.choice).tokenImage;
			return new StringLiteral("\"" + s.substring(1, s.length() - 1).replace("\\", "\\\\") + "\"").setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		default:
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.TimeLiteral n) {
		return new TimeLiteral(n.f0.tokenImage).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.EmptyStatement n) {
		return new Block().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StopStatement n) {
		return new StopStatement().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitorExpr n) {
		return new VisitorExpression((VisitorType)n.f0.accept(this), (Block)n.f1.accept(this)).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitorType n) {
		return new VisitorType().setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitStatement n) {
		final boolean before = n.f0.which == 0;
		final Block body = ensureBlock((Statement)n.f3.accept(this));

		switch (n.f1.which) {
		case 0:
			boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f1.choice;
			final Component c = new Component((Identifier)ns.elementAt(0).accept(this), (AbstractType)ns.elementAt(2).accept(this));
			c.setPositions(firstVisitor.visit((boa.parser.syntaxtree.Identifier)ns.elementAt(0)), lastVisitor.visit((boa.parser.syntaxtree.Identifier)ns.elementAt(2)));
			return new VisitStatement(before, c, body).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		case 1:
			final VisitStatement vs = new VisitStatement(before, false, body);
			vs.setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
			boa.parser.syntaxtree.IdentifierList idl = (boa.parser.syntaxtree.IdentifierList)n.f1.choice;

			vs.addId((Identifier)idl.f0.accept(this));

			if (idl.f1.present())
				for (boa.parser.syntaxtree.Node idseq : idl.f1.nodes)
					vs.addId((Identifier)((boa.parser.syntaxtree.NodeSequence)idseq).elementAt(1).accept(this));

			return vs;
		case 2:
			return new VisitStatement(before, true, body).setPositions(firstVisitor.visit(n), lastVisitor.visit(n));
		default:
			throw new RuntimeException("unexpected choice " + n.f1.which + " is " + n.f1.choice.getClass());
		}
	}

	// ensures we always have a block statement by turning
	// single statements into blocks containing it
	private Block ensureBlock(final Statement stmt) {
		if (stmt instanceof Block)
			return (Block)stmt;

		final Block body = new Block();
		body.addStatement(stmt);
		body.setPositions(stmt.getBeginLine(), stmt.getBeginColumn(), stmt.getEndLine(), stmt.getEndColumn());
		return body;
	}
}
