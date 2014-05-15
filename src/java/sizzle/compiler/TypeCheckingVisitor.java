package sizzle.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sizzle.aggregators.AggregatorSpec;
import sizzle.parser.syntaxtree.ArrayType;
import sizzle.parser.syntaxtree.Assignment;
import sizzle.parser.syntaxtree.Block;
import sizzle.parser.syntaxtree.BreakStatement;
import sizzle.parser.syntaxtree.BytesLiteral;
import sizzle.parser.syntaxtree.Call;
import sizzle.parser.syntaxtree.CharLiteral;
import sizzle.parser.syntaxtree.Comparison;
import sizzle.parser.syntaxtree.Component;
import sizzle.parser.syntaxtree.Composite;
import sizzle.parser.syntaxtree.Conjunction;
import sizzle.parser.syntaxtree.ContinueStatement;
import sizzle.parser.syntaxtree.Declaration;
import sizzle.parser.syntaxtree.DoStatement;
import sizzle.parser.syntaxtree.EmitStatement;
import sizzle.parser.syntaxtree.ExprList;
import sizzle.parser.syntaxtree.ExprStatement;
import sizzle.parser.syntaxtree.Expression;
import sizzle.parser.syntaxtree.Factor;
import sizzle.parser.syntaxtree.FingerprintLiteral;
import sizzle.parser.syntaxtree.FloatingPointLiteral;
import sizzle.parser.syntaxtree.ForExprStatement;
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.ForVarDecl;
import sizzle.parser.syntaxtree.Function;
import sizzle.parser.syntaxtree.FunctionType;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.MapType;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeChoice;
import sizzle.parser.syntaxtree.NodeListOptional;
import sizzle.parser.syntaxtree.NodeSequence;
import sizzle.parser.syntaxtree.NodeToken;
import sizzle.parser.syntaxtree.Operand;
import sizzle.parser.syntaxtree.OutputType;
import sizzle.parser.syntaxtree.Pair;
import sizzle.parser.syntaxtree.PairList;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.Proto;
import sizzle.parser.syntaxtree.ProtoFieldDecl;
import sizzle.parser.syntaxtree.ProtoMember;
import sizzle.parser.syntaxtree.ProtoMemberList;
import sizzle.parser.syntaxtree.ProtoTupleType;
import sizzle.parser.syntaxtree.Regexp;
import sizzle.parser.syntaxtree.RegexpList;
import sizzle.parser.syntaxtree.ResultStatement;
import sizzle.parser.syntaxtree.ReturnStatement;
import sizzle.parser.syntaxtree.Selector;
import sizzle.parser.syntaxtree.SimpleExpr;
import sizzle.parser.syntaxtree.SimpleMember;
import sizzle.parser.syntaxtree.SimpleMemberList;
import sizzle.parser.syntaxtree.SimpleTupleType;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.StatementExpr;
import sizzle.parser.syntaxtree.StaticVarDecl;
import sizzle.parser.syntaxtree.StringLiteral;
import sizzle.parser.syntaxtree.SwitchStatement;
import sizzle.parser.syntaxtree.Term;
import sizzle.parser.syntaxtree.TimeLiteral;
import sizzle.parser.syntaxtree.TupleType;
import sizzle.parser.syntaxtree.Type;
import sizzle.parser.syntaxtree.TypeDecl;
import sizzle.parser.syntaxtree.VarDecl;
import sizzle.parser.syntaxtree.WhenStatement;
import sizzle.parser.syntaxtree.WhileStatement;
import sizzle.parser.visitor.GJDepthFirst;
import sizzle.types.SizzleAny;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFingerprint;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleName;
import sizzle.types.SizzleProtoList;
import sizzle.types.SizzleProtoMap;
import sizzle.types.SizzleProtoTuple;
import sizzle.types.SizzleScalar;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleTime;
import sizzle.types.SizzleTuple;
import sizzle.types.SizzleType;

/**
 * Prescan the Sizzle program and check that all variables are consistently
 * typed.
 * 
 * @author anthonyu
 * 
 */
public class TypeCheckingVisitor extends GJDepthFirst<SizzleType, SymbolTable> {
	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Start n, final SymbolTable argu) {
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Program n, final SymbolTable argu) {
		for (final Node node : n.f0.nodes)
			node.accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Declaration n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // type declaration
		case 1: // static variable declaration
		case 2: // variable declaration
			return n.f0.choice.accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TypeDecl n, final SymbolTable argu) {
		final String id = n.f1.f0.tokenImage;

		if (argu.hasType(id))
			throw new TypeException(n, id + " already defined as " + argu.getType(id));

		argu.setType(id, new SizzleName(n.f3.accept(this, argu)));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StaticVarDecl n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VarDecl n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;

		if (argu.contains(id))
			throw new TypeException(n, "variable " + id + " already declared as " + argu.get(id));

		SizzleType rhs = null;
		if (n.f3.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f3.node;
			switch (nodeChoice.which) {
			case 0: // initializer
				Node elem = ((NodeSequence) nodeChoice.choice).elementAt(1);
				rhs = elem.accept(this, argu);
				if (rhs instanceof SizzleFunction && !elem.accept(new IsFunctionVisitor(), argu))
					rhs = ((SizzleFunction)rhs).getType();
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		SizzleType lhs;
		if (n.f2.present()) {
			lhs = n.f2.node.accept(this, argu);

			if (lhs instanceof SizzleArray && rhs instanceof SizzleTuple)
				rhs = new SizzleArray(((SizzleTuple)rhs).getMember(0));

			if (rhs != null && !lhs.assigns(rhs) && !argu.hasCast(rhs, lhs))
				throw new TypeException(n, "incorrect type " + rhs + " for assignment to " + id + ':' + lhs);
		} else {
			if (rhs == null)
				throw new TypeException(n, "variable declaration requires a type or an initializer");

			lhs = rhs;
		}

		argu.set(id, lhs);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Type n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // array
		case 2: // map
		case 3: // tuple
		case 4: // table
		case 5: // function
			return n.f0.choice.accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Component n, final SymbolTable argu) {
		if (n.f0.present())
			return new SizzleName(n.f1.accept(this, argu), ((Identifier) ((NodeSequence) n.f0.node).elementAt(0)).f0.tokenImage);
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ArrayType n, final SymbolTable argu) {
		return new SizzleArray(n.f2.accept(this, argu));
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TupleType n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleTupleType n, final SymbolTable argu) {
		if (n.f1.present())
			return new SizzleTuple(this.check((SimpleMemberList) n.f1.node, argu));
		else
			return new SizzleTuple(new ArrayList<SizzleType>());
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleMember n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ProtoTupleType n, final SymbolTable argu) {
		if (n.f2.present())
			return new SizzleProtoTuple(this.check((SimpleMemberList) n.f2.node, argu));
		else
			return new SizzleProtoTuple(new ArrayList<SizzleType>());
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ProtoMemberList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ProtoMember n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ProtoFieldDecl n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final MapType n, final SymbolTable argu) {
		return new SizzleMap(n.f5.accept(this, argu), n.f2.accept(this, argu));
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final OutputType n, final SymbolTable argu) {
		List<SizzleScalar> indexTypes = null;
		if (n.f3.present()) {
			indexTypes = new ArrayList<SizzleScalar>();

			for (final Node node : n.f3.nodes) {
				final SizzleType sizzleType = ((NodeSequence) node).elementAt(1).accept(this, argu);

				if (!(sizzleType instanceof SizzleScalar))
					throw new TypeException(n, "incorrect type " + sizzleType + " for index");

				indexTypes.add((SizzleScalar) sizzleType);
			}
		}

		final SizzleType type = n.f5.accept(this, argu);

		final AggregatorSpec annotation = argu.getAggregators(n.f1.f0.tokenImage, type).get(0).getAnnotation(AggregatorSpec.class);

		SizzleScalar tweight = null;
		if (n.f6.present()) {
			if (annotation.weightType().equals("none"))
				throw new TypeException(n, "unexpected weight for table declaration");

			final SizzleType aweight = argu.getType(annotation.weightType());
			tweight = (SizzleScalar) ((NodeSequence) n.f6.node).nodes.get(1).accept(this, argu);

			if (!aweight.assigns(tweight))
				throw new TypeException(n, "incorrect weight type for table declaration");
		} else if (!annotation.weightType().equals("none"))
			throw new TypeException(n, "missing weight for table declaration");

		if (n.f2.present())
			if (annotation.formalParameters().length == 0)
				throw new TypeException(n, "no arguments for table " + n.f1.f0.tokenImage);

		return new SizzleTable(type, indexTypes, tweight);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExprList n, final SymbolTable argu) {
		final List<SizzleType> types = this.check(n, argu);

//		final SizzleType t = types.get(0);

//		for (int i = 1; i < types.size(); i++)
//			if (!t.assigns(types.get(i)))
				return new SizzleTuple(types);

//		return new SizzleArray(t);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FunctionType n, final SymbolTable argu) {
		final SizzleType[] params;
		if (n.f2.present()) {
			final List<String> idents = new ArrayList<String>();
			final List<SizzleType> types = new ArrayList<SizzleType>();

			final NodeSequence nodes = (NodeSequence)n.f2.node;

			idents.add(((Identifier)nodes.elementAt(0)).f0.tokenImage);
			types.add(nodes.elementAt(2).accept(this, argu));
			
			final NodeListOptional paramList = (NodeListOptional)nodes.elementAt(3);
			if (paramList.present())
				for (Node paramNodes : paramList.nodes) {
					idents.add(((Identifier)((NodeSequence)paramNodes).elementAt(1)).f0.tokenImage);
					types.add(((NodeSequence)paramNodes).elementAt(3).accept(this, argu));
				}

			params = new SizzleType[idents.size()];

			for (int i = 0; i < params.length; i++) {
				params[i] = new SizzleName(types.get(i), idents.get(i));
				argu.set(idents.get(i), types.get(i));
			}
		} else {
			params = null;
		}

		final SizzleType ret;
		if (n.f4.present())
			ret = ((NodeSequence)n.f4.node).elementAt(1).accept(this, argu);
		else
			ret = new SizzleAny();

		return new SizzleFunction(ret, params);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Statement n, final SymbolTable argu) {
		return n.f0.choice.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Assignment n, final SymbolTable argu) {
		final SizzleType lhs = n.f0.accept(this, argu);
		final SizzleType rhs = n.f2.accept(this, argu);

		if (!(lhs instanceof SizzleArray && rhs instanceof SizzleTuple))
			if (!lhs.assigns(rhs))
				throw new TypeException(n, "invalid type " + rhs + " for assignment to " + lhs);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Block n, final SymbolTable argu) {
		SymbolTable st;

		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				node.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final BreakStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ContinueStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final DoStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final EmitStatement n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(id);

		if (t == null)
			throw new TypeException(n, "emitting to undeclared table " + id);

		if (n.f1.present()) {
			final List<SizzleType> indices = new ArrayList<SizzleType>();
			for (final Node node : n.f1.nodes)
				indices.add(((NodeSequence) node).nodes.get(1).accept(this, argu));

			if (indices.size() != t.countIndices())
				throw new TypeException(n, "incorrect number of indices for " + id);

			for (int i = 0; i < t.countIndices(); i++)
				if (!t.getIndex(i).assigns(indices.get(i)))
					throw new TypeException(n, "incorrect type " + indices.get(i) + " for index " + i);
		} else if (t.countIndices() > 0)
			throw new TypeException(n, "indices missing from emit");

		final SizzleType expression = n.f3.accept(this, argu);
		if (!t.accepts(expression))
			throw new TypeException(n, "incorrect type " + expression + " for " + id + ":" + t);

		if (n.f4.present()) {
			if (t.getWeightType() == null)
				throw new TypeException(n, "unexpected weight specified by emit");

			final SizzleType wtype = ((NodeSequence) n.f4.node).nodes.get(1).accept(this, argu);

			if (!t.acceptsWeight(wtype))
				throw new TypeException(n, "incorrect type " + wtype + " for weight of " + id + ":" + t.getWeightType());
		} else if (t.getWeightType() != null)
			throw new TypeException(n, "no weight specified by emit");

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExprStatement n, final SymbolTable argu) {
		final SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present() && !(type instanceof SizzleInt))
			throw new TypeException(n, type + " not valid for operator " + n.f1.toString());

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ForStatement n, final SymbolTable argu) {
		SymbolTable st;

		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		if (n.f2.present())
			((NodeChoice) n.f2.node).choice.accept(this, st);

		if (n.f4.present())
			n.f4.accept(this, st);

		if (n.f6.present())
			((NodeChoice) n.f6.node).choice.accept(this, st);

		n.f8.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ForVarDecl n, final SymbolTable argu) {
		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		return varDecl.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ForExprStatement n, final SymbolTable argu) {
		final ExprStatement exprStatement = new ExprStatement(n.f0, n.f1, new NodeToken(";"));

		return exprStatement.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IfStatement n, final SymbolTable argu) {
		final SizzleType test = n.f2.accept(this, argu);

		if (!(test instanceof SizzleBool) && !(test instanceof SizzleFunction && ((SizzleFunction) test).getType() instanceof SizzleBool))
			throw new TypeException(n, "invalid type " + test + " for if test");

		n.f4.accept(this, argu);

		if (n.f5.present())
			((Statement) ((NodeSequence) n.f5.node).elementAt(1)).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ResultStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ReturnStatement n, final SymbolTable argu) {
		// FIXME rdyer need to check return type matches function declaration's return
		if (n.f1.present())
			return n.f1.accept(this, argu);
		return new SizzleAny();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SwitchStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final WhenStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		st.set(n.f2.f0.tokenImage, n.f4.accept(this, argu));

		n.f6.accept(this, st);
		n.f8.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Expression n, final SymbolTable argu) {
		final SizzleType ltype = n.f0.accept(this, argu);

		if (n.f1.present()) {
			if (!(ltype instanceof SizzleBool))
				throw new TypeException(n, "invalid type " + ltype + " for disjunction");

			final SizzleType rtype = n.f0.accept(this, argu);

			if (!(rtype instanceof SizzleBool))
				throw new TypeException(n, "invalid type " + rtype + " for disjunction");
		}

		return ltype;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Conjunction n, final SymbolTable argu) {
		final SizzleType lhs = n.f0.accept(this, argu);

		if (n.f1.present()) {
			final SizzleType rhs = n.f0.accept(this, argu);

			if (!rhs.compares(lhs))
				throw new TypeException(n, "invalid type " + rhs + " for conjunction with " + lhs);

			return new SizzleBool();
		}

		return lhs;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Comparison n, final SymbolTable argu) {
		final SizzleType lhs = n.f0.accept(this, argu);

		if (n.f1.present()) {
			final SizzleType rhs = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);

			if (!rhs.compares(lhs))
				throw new TypeException(n, "invalid type " + rhs + " for comparison with " + lhs);

			return new SizzleBool();
		}

		return lhs;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleExpr n, final SymbolTable argu) {
		SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present()) {
			for (final Node node : n.f1.nodes) {
				final SizzleType accept = ((NodeSequence) node).nodes.get(1).accept(this, argu);
				type = type.arithmetics(accept);
			}
		}

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Term n, final SymbolTable argu) {
		final SizzleType accepts = n.f0.accept(this, argu);

		if (n.f1.present()) {
			SizzleScalar type;

			if (accepts instanceof SizzleFunction)
				type = (SizzleScalar) ((SizzleFunction) accepts).getType();
			else
				type = (SizzleScalar) accepts;

			for (final Node node : n.f1.nodes) {
				final SizzleType accept = ((NodeSequence) node).nodes.get(1).accept(this, argu);

				type = type.arithmetics(accept);
			}

			return type;
		}

		return accepts;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Factor n, final SymbolTable argu) {
		SizzleType type = null;

		if (n.f1.present()) {
			for (final Node node : n.f1.nodes) {
				final NodeChoice nodeChoice = (NodeChoice) node;
				switch (nodeChoice.which) {
				case 0: // selector
					if (type == null)
						type = n.f0.accept(this, argu);

					if (type instanceof SizzleName)
						type = ((SizzleName) type).getType();

					final String selector = ((Selector) nodeChoice.choice).f1.f0.tokenImage;

					if (type instanceof SizzleProtoMap) {
						// FIXME rdyer how do we verify the enum value exists?
//						if (!((SizzleTuple) type).hasMember(selector))
//							throw new TypeException(type + " has no member named '" + selector + "'");

						type = new SizzleInt();
						break;
					}

					if (type instanceof SizzleTuple) {
						if (!((SizzleTuple) type).hasMember(selector))
							throw new TypeException(n, type + " has no member named '" + selector + "'");

						type = ((SizzleTuple) type).getMember(selector);
						break;
					}

					throw new TypeException(n, "invalid operand type " + type + " for member selection");
				case 1: // index
					if (type == null)
						type = n.f0.accept(this, argu);
					final SizzleType index = ((Index) nodeChoice.choice).accept(this, argu);

					if (type instanceof SizzleArray) {
						if (!(index instanceof SizzleInt))
							throw new TypeException(n, "invalid operand type " + index + " for indexing into array");

						type = ((SizzleArray) type).getType();
					} else if (type instanceof SizzleProtoList) {
						if (!(index instanceof SizzleInt))
							throw new TypeException(n, "invalid operand type " + index + " for indexing into array");

						type = ((SizzleProtoList) type).getType();
					} else if (type instanceof SizzleMap) {
						if (!((SizzleMap) type).getIndexType().assigns(index))
							throw new TypeException(n, "invalid operand type " + index + " for indexing into " + type);

						type = ((SizzleMap) type).getType();
					} else {
						throw new TypeException(n, "invalid operand type " + type + " for indexing expression");
					}
					break;
				case 2: // call
					final List<SizzleType> formalParameters = this.check((Call) nodeChoice.choice, argu);

					return new FunctionFindingVisitor(formalParameters).visit(n.f0, argu);
				default:
					throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
				}
			}
		} else {
			type = n.f0.accept(this, argu);
		}

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Selector n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Index n, final SymbolTable argu) {
		final SizzleType index = n.f1.accept(this, argu);

		if (index == null)
			throw new RuntimeException();

		if (n.f2.present()) {
			if (!(index instanceof SizzleInt))
				throw new TypeException(n, "invalid type " + index + " for slice expression");

			final SizzleType slice = ((NodeSequence) n.f2.node).elementAt(1).accept(this, argu);

			if (!(slice instanceof SizzleInt))
				throw new TypeException(n, "invalid type " + slice + " for slice expression");
		}

		return index;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Call n, final SymbolTable argu) {
		if (n.f1.present())
			return n.f1.node.accept(this, argu);

		return new SizzleArray();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final RegexpList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Regexp n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Operand n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // identifier
		case 1: // string literal
		case 2: // integer literal
		case 3: // floating point literal
		case 4: // composite
		case 5: // function
		case 8: // statement expression
			return n.f0.choice.accept(this, argu);
		case 6: // unary operator
		case 9: // parenthetical
			return ((NodeSequence) n.f0.choice).nodes.elementAt(1).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Composite n, final SymbolTable argu) {
		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;

			switch (nodeChoice.which) {
			case 0: // pair list
			case 1: // expression list
				return nodeChoice.choice.accept(this, argu);
			case 2: // empty map
				return new SizzleMap();
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		return new SizzleArray();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final PairList n, final SymbolTable argu) {
		final SizzleMap sizzleMap = (SizzleMap) n.f0.accept(this, argu);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				if (!sizzleMap.assigns(((NodeSequence) node).elementAt(1).accept(this, argu)))
					throw new TypeException(n, "incorrect type " + node + " for " + sizzleMap);

		return sizzleMap;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Pair n, final SymbolTable argu) {
		return new SizzleMap(n.f0.accept(this, argu), n.f2.accept(this, argu));
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Function n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleType type = n.f0.accept(this, st);
		n.f1.accept(this, st);

		return type;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StatementExpr n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Proto n, final SymbolTable argu) {
		final String tokenImage = ((NodeToken) n.f1.f0.choice).tokenImage;

		argu.importProto(tokenImage.substring(1, tokenImage.length() - 1));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Identifier n, final SymbolTable argu) {
		final String id = n.f0.tokenImage;

		if (argu.hasType(id))
			return argu.getType(id);

		return argu.get(id);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IntegerLiteral n, final SymbolTable argu) {
		return new SizzleInt();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FingerprintLiteral n, final SymbolTable argu) {
		return new SizzleFingerprint();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FloatingPointLiteral n, final SymbolTable argu) {
		return new SizzleFloat();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final CharLiteral n, final SymbolTable argu) {
		return new SizzleInt();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StringLiteral n, final SymbolTable argu) {
		return new SizzleString();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final BytesLiteral n, final SymbolTable argu) {
		return new SizzleBytes();
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TimeLiteral n, final SymbolTable argu) {
		return new SizzleTime();
	}

	List<SizzleType> check(final Call c, final SymbolTable argu) {
		if (c.f1.present())
			return this.check((ExprList) c.f1.node, argu);

		return new ArrayList<SizzleType>();
	}

	private List<SizzleType> check(final ExprList e, final SymbolTable argu) {
		final List<SizzleType> types = new ArrayList<SizzleType>();

		types.add(assignableType(e.f0.accept(this, argu)));

		if (e.f1.present())
			for (final Node node : e.f1.nodes)
				types.add(assignableType(((NodeSequence) node).elementAt(1).accept(this, argu)));

		return types;
	}

	private SizzleType assignableType(SizzleType t) {
		if (t instanceof SizzleFunction)
			return ((SizzleFunction) t).getType();
		return t;
	}

	private List<SizzleType> check(final SimpleMemberList e, final SymbolTable argu) {
		final List<SizzleType> types = new ArrayList<SizzleType>();

		types.add(e.f0.accept(this, argu));

		if (e.f1.present())
			for (final Node node : e.f1.nodes)
				types.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		return types;
	}
}
