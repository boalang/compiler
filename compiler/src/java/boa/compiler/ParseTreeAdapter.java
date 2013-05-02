package boa.compiler;

import java.util.Vector;

import boa.compiler.ast.*;
import boa.compiler.ast.expressions.*;
import boa.compiler.ast.literals.*;
import boa.compiler.ast.statements.*;
import boa.compiler.ast.types.*;

import boa.parser.visitor.GJNoArguDepthFirst;

public class ParseTreeAdapter extends GJNoArguDepthFirst<Node> {
	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Program n) {
		final Program p = new Program();

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
		return new VarDeclStatement(true, var.getId(), var.getType(), var.getInitializer());
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VarDecl n) {
		Node t;
		if (n.f2.present())
			t = n.f2.accept(this);
		else
			t = null;

		Node initializer;
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

		return new VarDeclStatement((Identifier)n.f0.accept(this), (AbstractType)t, initializer);
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
			return new Component((Identifier)n.f0.accept(this), (AbstractType)n.f1.accept(this));
		return new Component((AbstractType)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ArrayType n) {
		return new ArrayType((Component) n.f2.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.TupleType n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SimpleTupleType n) {
		final TupleType t = new TupleType();

		if (n.f1.present()) {
			final boa.parser.syntaxtree.SimpleMemberList ml = (boa.parser.syntaxtree.SimpleMemberList)n.f1.node;

			t.addMember((Component) ml.f0.accept(this));

			if (ml.f1.present())
				for (final boa.parser.syntaxtree.Node c : ml.f1.nodes)
					t.addMember((Component) c.accept(this));
		}

		return t;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SimpleMember n) {
		return n.f0.accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.MapType n) {
		return new MapType((Component)n.f2.accept(this), (Component)n.f5.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StackType n) {
		return new StackType((Component) n.f2.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.OutputType n) {
		final OutputType t;
		if (n.f6.present())
			t = new OutputType((Identifier)n.f1.accept(this), (Component)n.f5.accept(this), (Component)((boa.parser.syntaxtree.NodeSequence)n.f6.node).elementAt(1).accept(this));
		else
			t = new OutputType((Identifier)n.f1.accept(this), (Component)n.f5.accept(this));

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

		if (n.f2.present()) {
			final boa.parser.syntaxtree.NodeSequence nodes = (boa.parser.syntaxtree.NodeSequence)n.f2.node;

			f.addArg(new Component((Identifier)nodes.elementAt(0).accept(this), (AbstractType)nodes.elementAt(2).accept(this)));
			
			final boa.parser.syntaxtree.NodeListOptional paramList = (boa.parser.syntaxtree.NodeListOptional)nodes.elementAt(3);
			if (paramList.present())
				for (final boa.parser.syntaxtree.Node paramNodes : paramList.nodes) {
					boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)paramNodes;
					f.addArg(new Component((Identifier)ns.elementAt(1).accept(this), (AbstractType)ns.elementAt(3).accept(this)));
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
		return new AssignmentStatement((Factor)n.f0.accept(this), (Expression)n.f2.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Block n) {
		Block b = new Block();
		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node s : n.f1.nodes)
				b.addStatement((Statement)s.accept(this));
		return b;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.BreakStatement n) {
		return new BreakStatement();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ContinueStatement n) {
		return new ContinueStatement();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.DoStatement n) {
		return new DoStatement((Expression)n.f4.accept(this), (Statement)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.EmitStatement n) {
		final EmitStatement es;

		if (n.f4.present())
			es = new EmitStatement((Identifier)n.f0.accept(this), (Expression)n.f3.accept(this), (Expression)((boa.parser.syntaxtree.NodeSequence)n.f4.node).elementAt(1).accept(this));
		else
			es = new EmitStatement((Identifier)n.f0.accept(this), (Expression)n.f3.accept(this));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node ns : n.f1.nodes)
				es.addIndice((Expression)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));

		return es;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ExprStatement n) {
		if (n.f1.present())
			return new PostfixStatement((Expression)n.f0.accept(this), ((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)n.f1.node).choice).tokenImage);
		return new ExprStatement((Expression)n.f0.accept(this));
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

		return new ForStatement(init, condition, update, (Statement)n.f8.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.IfStatement n) {
		if (n.f5.present())
			return new IfStatement((Expression)n.f2.accept(this), (Statement)n.f4.accept(this), (Statement)((boa.parser.syntaxtree.NodeSequence)n.f5.node).elementAt(1).accept(this));
		return new IfStatement((Expression)n.f2.accept(this), (Statement)n.f4.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ResultStatement n) {
		return new ResultStatement((Expression)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.ReturnStatement n) {
		if (n.f1.present())
			return new ReturnStatement((Expression) n.f1.accept(this));
		return new ReturnStatement();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SwitchStatement n) {
		final SwitchCase dfault = new SwitchCase(true);
		dfault.addStatement((Statement)n.f8.accept(this));
		if (n.f9.present())
			for (boa.parser.syntaxtree.Node st : n.f9.nodes)
				dfault.addStatement((Statement)st.accept(this));

		final SwitchStatement s = new SwitchStatement((Expression)n.f2.accept(this), dfault);

		if (n.f5.present())
			for (boa.parser.syntaxtree.Node st : n.f5.nodes) {
				boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)st;
				final SwitchCase casest = new SwitchCase(false);

				casest.addCase((Expression)ns.elementAt(1).accept(this));
				casest.addStatement((Statement) ns.elementAt(4).accept(this));

				boa.parser.syntaxtree.NodeListOptional opt1 = (boa.parser.syntaxtree.NodeListOptional)ns.elementAt(2);
				if (opt1.present())
					for (boa.parser.syntaxtree.Node ns2 : opt1.nodes)
						casest.addCase((Expression) ((boa.parser.syntaxtree.NodeSequence)ns2).elementAt(1).accept(this));

				boa.parser.syntaxtree.NodeListOptional opt2 = (boa.parser.syntaxtree.NodeListOptional)ns.elementAt(5);
				if (opt2.present())
					for (boa.parser.syntaxtree.Node stmt : opt2.nodes)
						casest.addStatement((Statement) stmt.accept(this));
			}

		return s;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.WhenStatement n) {
		final Component var = new Component((Identifier)n.f2.accept(this), (AbstractType)n.f4.accept(this));
		final Expression condition = (Expression)n.f6.accept(this);
		final Statement body = (Statement)n.f8.accept(this);

		switch (n.f0.f0.which) {
		case 0:
			return new ForeachStatement(var, condition, body);
		case 1:
			return new IfAllStatement(var, condition, body);
		case 2:
			return new ExistsStatement(var, condition, body);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.f0.which + " is " + n.f0.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.WhileStatement n) {
		return new WhileStatement((Expression)n.f2.accept(this), (Statement)n.f4.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Expression n) {
		final Expression e = new Expression((Conjunction)n.f0.accept(this));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node c : n.f1.nodes)
				e.addRhs((Conjunction)((boa.parser.syntaxtree.NodeSequence)c).elementAt(1).accept(this));

		return e;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Conjunction n) {
		final Conjunction c = new Conjunction((Comparison)n.f0.accept(this));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node c2 : n.f1.nodes)
				c.addRhs((Comparison)((boa.parser.syntaxtree.NodeSequence)c2).elementAt(1).accept(this));

		return c;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Comparison n) {
		if (n.f1.present())
			return new Comparison((SimpleExpr)n.f0.accept(this), ((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)n.f1.node).elementAt(0)).choice).tokenImage, (SimpleExpr)((boa.parser.syntaxtree.NodeSequence)n.f1.node).elementAt(1).accept(this));

		return new Comparison((SimpleExpr)n.f0.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.SimpleExpr n) {
		final SimpleExpr se = new SimpleExpr((Term)n.f0.accept(this));

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

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node ns : n.f1.nodes) {
				t.addOp(((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(0)).choice).tokenImage);
				t.addArg((Factor)((boa.parser.syntaxtree.NodeSequence)ns).elementAt(1).accept(this));
			}

		return t;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Factor n) {
		final Factor f = new Factor((Operand)n.f0.accept(this));

		if (n.f1.present())
			for (final boa.parser.syntaxtree.Node op : n.f1.nodes)
				f.addOp(op.accept(this));

		return f;
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Selector n) {
		return new Selector((Identifier)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Index n) {
		if (n.f2.present()) {
			final boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f2.node;
			return new Index((Expression) n.f1.accept(this), (Expression)ns.elementAt(1).accept(this));
		}

		return new Index((Expression) n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Call n) {
		final Call c = new Call();

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
		case 2: // integer literal
		case 3: // floating point literal
		case 4: // composite
		case 5: // visitor
		case 6: // function
		case 9: // statement expression
			return n.f0.choice.accept(this);
		case 7: // unary operator
			final Vector<boa.parser.syntaxtree.Node> nodes = ((boa.parser.syntaxtree.NodeSequence) n.f0.choice).nodes;
			return new UnaryFactor(((boa.parser.syntaxtree.NodeToken)((boa.parser.syntaxtree.NodeChoice)nodes.elementAt(0)).choice).tokenImage, (Factor)nodes.elementAt(1).accept(this));
		case 10: // paren
			return new ParenExpression((Expression)((boa.parser.syntaxtree.NodeSequence) n.f0.choice).nodes.elementAt(1).accept(this));
		case 8: // $
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Composite n) {
		final Composite c = new Composite(false);

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
				return new Composite(true);
			default:
				final boa.parser.syntaxtree.NodeChoice nc = (boa.parser.syntaxtree.NodeChoice)n.f1.node;
				throw new RuntimeException("unexpected choice " + nc.which + " is " + nc.choice.getClass());
			}

		return new Composite(true);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Pair n) {
		return new Pair((Expression)n.f0.accept(this), (Expression)n.f2.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Function n) {
		return new FunctionExpression((FunctionType)n.f0.accept(this), (Block)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StatementExpr n) {
		return new StatementExpr((Block)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.Identifier n) {
		return new Identifier(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.IntegerLiteral n) {
		return new IntegerLiteral(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.FloatingPointLiteral n) {
		return new FloatLiteral(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.CharLiteral n) {
		return new CharLiteral(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StringLiteral n) {
		switch (n.f0.which) {
		case 0: // STRING
			return new StringLiteral(((boa.parser.syntaxtree.NodeToken) n.f0.choice).tokenImage);
		case 1: // REGEX
			String s = ((boa.parser.syntaxtree.NodeToken) n.f0.choice).tokenImage;
			s = "\"" + s.substring(1, s.length() - 1).replace("\\", "\\\\") + "\"";
			return new StringLiteral(s);
		default:
			throw new RuntimeException("unimplemented");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.BytesLiteral n) {
		return new BytesLiteral(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.TimeLiteral n) {
		return new TimeLiteral(n.f0.tokenImage);
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.EmptyStatement n) {
		return new Block();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.StopStatement n) {
		return new StopStatement();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitorExpr n) {
		return new VisitorExpression((VisitorType)n.f0.accept(this), (Block)n.f1.accept(this));
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitorType n) {
		return new VisitorType();
	}

	/** {@inheritDoc} */
	@Override
	public Node visit(final boa.parser.syntaxtree.VisitStatement n) {
		final boolean before = n.f0.which == 0;
		final Statement body = (Statement)n.f3.accept(this);

		switch (n.f1.which) {
		case 0:
			boa.parser.syntaxtree.NodeSequence ns = (boa.parser.syntaxtree.NodeSequence)n.f1.choice;
			return new VisitStatement(before, new Component((Identifier)ns.elementAt(0).accept(this), (AbstractType)ns.elementAt(2).accept(this)), body);
		case 1:
			final VisitStatement vs = new VisitStatement(before, false, body);
			boa.parser.syntaxtree.IdentifierList idl = (boa.parser.syntaxtree.IdentifierList)n.f1.choice;

			vs.addId((Identifier)idl.f0.accept(this));

			if (idl.f1.present())
				for (boa.parser.syntaxtree.Node idseq : idl.f1.nodes)
					vs.addId((Identifier)((boa.parser.syntaxtree.NodeSequence)idseq).elementAt(1).accept(this));

			return vs;
		case 2:
			return new VisitStatement(before, true, body);
		default:
			throw new RuntimeException("unexpected choice " + n.f1.which + " is " + n.f1.choice.getClass());
		}
	}
}
