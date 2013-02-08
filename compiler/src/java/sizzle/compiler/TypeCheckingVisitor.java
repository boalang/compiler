package sizzle.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sizzle.aggregators.AggregatorSpec;
import sizzle.parser.syntaxtree.*;
import sizzle.types.*;

/**
 * Prescan the Sizzle program and check that all variables are consistently
 * typed.
 * 
 * @author anthonyu
 * 
 */
public class TypeCheckingVisitor extends DefaultVisitor<SizzleType, SymbolTable> {
	private Map<Node, SizzleType> bindings = new HashMap<Node, SizzleType>();
	private Map<Node, SymbolTable> syms = new HashMap<Node, SymbolTable>();

	public SizzleType getBinding(Node n) {
		return bindings.get(n);
	}

	public SymbolTable getSyms(Node n) {
		return syms.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Program n, final SymbolTable argu) {
		syms.put(n, argu);

		for (final Node node : n.f0.nodes)
			bindings.put(node, node.accept(this, argu));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Declaration n, final SymbolTable argu) {
		syms.put(n, argu);

		switch (n.f0.which) {
		case 0: // type declaration
		case 1: // static variable declaration
		case 2: // variable declaration
			bindings.put(n, n.f0.choice.accept(this, argu));
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TypeDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f1.f0.tokenImage;

		if (argu.hasType(id))
			throw new TypeException(n, "'" + id + "' already defined as type '" + argu.getType(id) + "'");

		bindings.put(n, new SizzleName(n.f3.accept(this, argu)));
		bindings.put(n.f1, bindings.get(n));
		argu.setType(id, bindings.get(n));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StaticVarDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f1.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VarDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f0.f0.tokenImage;

		if (argu.contains(id))
			throw new TypeException(n, "variable '" + id + "' already declared as '" + argu.get(id) + "'");

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
				throw new TypeException(n, "incorrect type '" + rhs + "' for assignment to '" + id + ": " + lhs + "'");
		} else {
			if (rhs == null)
				throw new TypeException(n, "variable declaration requires an explicit type or an initializer");

			lhs = rhs;
		}

		argu.set(id, lhs);
		bindings.put(n.f0, lhs);
		bindings.put(n, lhs);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Type n, final SymbolTable argu) {
		syms.put(n, argu);

		switch (n.f0.which) {
		case 0: // identifier
		case 1: // array
		case 2: // map
		case 3: // tuple
		case 4: // table
		case 5: // function
		case 6: // visitor
		case 8: // stack
			bindings.put(n, n.f0.choice.accept(this, argu));
			break;
		case 7: // proto type
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Component n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f0.present()) {
			final Identifier id = (Identifier) ((NodeSequence) n.f0.node).elementAt(0);
			final SizzleType type = n.f1.accept(this, argu);
			bindings.put(n, new SizzleName(type, id.f0.tokenImage));
			bindings.put(id, type);
		} else {
			bindings.put(n, n.f1.accept(this, argu));
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ArrayType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleArray(n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleTupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present())
			bindings.put(n, new SizzleTuple(this.check((SimpleMemberList) n.f1.node, argu)));
		else
			bindings.put(n, new SizzleTuple(new ArrayList<SizzleType>()));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleMember n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ProtoTupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f2.present())
			bindings.put(n, new SizzleProtoTuple(this.check((SimpleMemberList) n.f2.node, argu)));
		else
			bindings.put(n, new SizzleProtoTuple(new ArrayList<SizzleType>()));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final MapType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleMap(n.f5.accept(this, argu), n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StackType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleStack(n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final OutputType n, final SymbolTable argu) {
		syms.put(n, argu);

		List<SizzleScalar> indexTypes = null;
		if (n.f3.present()) {
			indexTypes = new ArrayList<SizzleScalar>();

			for (final Node node : n.f3.nodes) {
				final SizzleType sizzleType = ((NodeSequence) node).elementAt(1).accept(this, argu);

				if (!(sizzleType instanceof SizzleScalar))
					throw new TypeException(n, "incorrect type '" + sizzleType + "' for index");

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
				throw new TypeException(n, "no arguments for table '" + n.f1.f0.tokenImage + "'");

		bindings.put(n, new SizzleTable(type, indexTypes, tweight));
		argu.set(n.f1.f0.tokenImage, bindings.get(n));
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExprList n, final SymbolTable argu) {
		syms.put(n, argu);

		final List<SizzleType> types = this.check(n);

//		final SizzleType t = types.get(0);

//		for (int i = 1; i < types.size(); i++)
//			if (!t.assigns(types.get(i)))
				bindings.put(n, new SizzleTuple(types));

//		bindings.put(n, new SizzleArray(t));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FunctionType n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType[] params;
		if (n.f2.present()) {
			final List<Identifier> idents = new ArrayList<Identifier>();
			final List<SizzleType> types = new ArrayList<SizzleType>();

			final NodeSequence nodes = (NodeSequence)n.f2.node;

			idents.add((Identifier)nodes.elementAt(0));
			types.add(nodes.elementAt(2).accept(this, argu));
			
			final NodeListOptional paramList = (NodeListOptional)nodes.elementAt(3);
			if (paramList.present())
				for (Node paramNodes : paramList.nodes) {
					idents.add((Identifier)((NodeSequence)paramNodes).elementAt(1));
					types.add(((NodeSequence)paramNodes).elementAt(3).accept(this, argu));
				}

			params = new SizzleType[idents.size()];

			for (int i = 0; i < params.length; i++) {
				params[i] = new SizzleName(types.get(i), idents.get(i).f0.tokenImage);
				argu.set(idents.get(i).f0.tokenImage, types.get(i));
				idents.get(i).accept(this, argu);
			}
		} else {
			params = new SizzleType[0];
		}

		final SizzleType ret;
		if (n.f4.present())
			ret = ((NodeSequence)n.f4.node).elementAt(1).accept(this, argu);
		else
			ret = new SizzleAny();

		bindings.put(n, new SizzleFunction(ret, params));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Statement n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Assignment n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType lhs = n.f0.accept(this, argu);
		final SizzleType rhs = n.f2.accept(this, argu);

		if (!(lhs instanceof SizzleArray && rhs instanceof SizzleTuple))
			if (!lhs.assigns(rhs))
				throw new TypeException(n, "invalid type '" + rhs + "' for assignment to '" + lhs + "'");

		bindings.put(n, lhs);
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

		syms.put(n, st);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				node.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final BreakStatement n, final SymbolTable argu) {
		syms.put(n, argu);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ContinueStatement n, final SymbolTable argu) {
		syms.put(n, argu);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final DoStatement n, final SymbolTable argu) {
		SymbolTable st;

		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		syms.put(n, st);

		n.f4.accept(this, st);
		n.f2.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final EmitStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f0.f0.tokenImage;
		final SizzleType type = n.f0.accept(this, argu);

		if (type == null)
			throw new TypeException(n, "emitting to undeclared output '" + id + "'");
		if (!(type instanceof SizzleTable))
			throw new TypeException(n, "emitting to non-output variable '" + id + "'");

		final SizzleTable t = (SizzleTable) type;

		if (n.f1.present()) {
			final List<SizzleType> indices = new ArrayList<SizzleType>();
			for (final Node node : n.f1.nodes)
				indices.add(((NodeSequence) node).nodes.get(1).accept(this, argu));

			if (indices.size() != t.countIndices())
				throw new TypeException(n, "incorrect number of indices for '" + id + "'");

			for (int i = 0; i < t.countIndices(); i++)
				if (!t.getIndex(i).assigns(indices.get(i)))
					throw new TypeException(n, "incorrect type '" + indices.get(i) + "' for index '" + i + "'");
		} else if (t.countIndices() > 0)
			throw new TypeException(n, "indices missing from emit");

		final SizzleType expression = n.f3.accept(this, argu);
		if (!t.accepts(expression))
			throw new TypeException(n, "incorrect type '" + expression + "' for '" + id + ": " + t + "'");

		if (n.f4.present()) {
			if (t.getWeightType() == null)
				throw new TypeException(n, "unexpected weight specified by emit");

			final SizzleType wtype = ((NodeSequence) n.f4.node).nodes.get(1).accept(this, argu);

			if (!t.acceptsWeight(wtype))
				throw new TypeException(n, "incorrect type '" + wtype + "' for weight of '" + id + ": " + t.getWeightType() + "'");
		} else if (t.getWeightType() != null)
			throw new TypeException(n, "no weight specified by emit");

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ExprStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType type = n.f0.accept(this, argu);
		bindings.put(n, type);

		if (n.f1.present() && !(type instanceof SizzleInt))
			throw new TypeException(n, "'" + type + "' not valid for operator '" + n.f1.toString() + "'");

		return bindings.get(n);
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

		syms.put(n, st);

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
		syms.put(n, argu);

		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		bindings.put(n, varDecl.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ForExprStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final ExprStatement exprStatement = new ExprStatement(n.f0, n.f1, new NodeToken(";"));

		bindings.put(n, exprStatement.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IfStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType test = n.f2.accept(this, argu);

		if (!(test instanceof SizzleBool) && !(test instanceof SizzleFunction && ((SizzleFunction) test).getType() instanceof SizzleBool))
			throw new TypeException(n, "invalid type '" + test + "' for if conditional");

		n.f4.accept(this, argu);

		if (n.f5.present())
			((Statement) ((NodeSequence) n.f5.node).elementAt(1)).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final ReturnStatement n, final SymbolTable argu) {
		if (argu.getIsBeforeVisitor())
			throw new TypeException(n, "return statement not allowed inside visitors");

		syms.put(n, argu);

		// FIXME rdyer need to check return type matches function declaration's return
		if (n.f1.present())
			bindings.put(n, n.f1.accept(this, argu));
		else
			bindings.put(n, new SizzleAny());

		return bindings.get(n);
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

		syms.put(n, st);

		st.set(n.f2.f0.tokenImage, n.f4.accept(this, argu));
		n.f2.accept(this, st);

		n.f6.accept(this, st);
		n.f8.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Expression n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType ltype = n.f0.accept(this, argu);
		bindings.put(n, ltype);

		if (n.f1.present()) {
			if (!(ltype instanceof SizzleBool))
				throw new TypeException(n, "invalid type '" + ltype + "' for disjunction");

			for (final Node node : n.f1.nodes) {
				final SizzleType rtype = ((NodeSequence)node).elementAt(1).accept(this, argu);
	
				if (!(rtype instanceof SizzleBool))
					throw new TypeException(n, "invalid type '" + rtype + "' for disjunction");
			}
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Conjunction n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType lhs = n.f0.accept(this, argu);
		bindings.put(n, lhs);

		if (n.f1.present()) {
			for (final Node node : n.f1.nodes) {
				final SizzleType rhs = ((NodeSequence)node).nodes.elementAt(1).accept(this, argu);

				if (!rhs.compares(lhs))
					throw new TypeException(n, "invalid type '" + rhs + "' for conjunction with '" + lhs + "'");
			}

			bindings.put(n, new SizzleBool());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Comparison n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType lhs = n.f0.accept(this, argu);
		bindings.put(n, lhs);

		if (n.f1.present()) {
			final SizzleType rhs = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);

			if (!rhs.compares(lhs))
				throw new TypeException(n, "invalid type '" + rhs + "' for comparison with '" + lhs + "'");

			bindings.put(n, new SizzleBool());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final SimpleExpr n, final SymbolTable argu) {
		syms.put(n, argu);

		SizzleType type = n.f0.accept(this, argu);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				type = type.arithmetics(((NodeSequence) node).nodes.get(1).accept(this, argu));

		bindings.put(n, type);
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Term n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType accepts = n.f0.accept(this, argu);
		bindings.put(n, accepts);

		if (n.f1.present()) {
			SizzleScalar type;

			if (accepts instanceof SizzleFunction)
				type = (SizzleScalar) ((SizzleFunction) accepts).getType();
			else
				type = (SizzleScalar) accepts;

			for (final Node node : n.f1.nodes)
				type = type.arithmetics(((NodeSequence) node).nodes.get(1).accept(this, argu));

			bindings.put(n, type);
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Factor n, final SymbolTable argu) {
		syms.put(n, argu);

		SizzleType type = null;

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				final NodeChoice nodeChoice = (NodeChoice) node;
				switch (nodeChoice.which) {
				case 0: // selector
					if (type == null)
						type = n.f0.accept(this, argu);

					if (type instanceof SizzleName)
						type = ((SizzleName) type).getType();

					argu.setOperandType(type);
					type = ((Selector)nodeChoice.choice).accept(this, argu);
					break;
				case 1: // index
					if (type == null)
						type = n.f0.accept(this, argu);

					final SizzleType index = ((Index) nodeChoice.choice).accept(this, argu);

					if (type instanceof SizzleArray) {
						if (!(index instanceof SizzleInt))
							throw new TypeException(n, "invalid operand type '" + index + "' for indexing into array");

						type = ((SizzleArray) type).getType();
					} else if (type instanceof SizzleProtoList) {
						if (!(index instanceof SizzleInt))
							throw new TypeException(n, "invalid operand type '" + index + "' for indexing into array");

						type = ((SizzleProtoList) type).getType();
					} else if (type instanceof SizzleMap) {
						if (!((SizzleMap) type).getIndexType().assigns(index))
							throw new TypeException(n, "invalid operand type '" + index + "' for indexing into '" + type + "'");

						type = ((SizzleMap) type).getType();
					} else {
						throw new TypeException(n, "invalid operand type '" + type + "' for indexing expression");
					}
					break;
				case 2: // call
					((Call) nodeChoice.choice).accept(this, argu);
					syms.put(((Operand)n.f0).f0.choice, argu);

					final List<SizzleType> formalParameters = this.check((Call) nodeChoice.choice, argu);

					bindings.put(node, new FunctionFindingVisitor(formalParameters).visit(n.f0, argu));
					bindings.put(n, bindings.get(node));
					return bindings.get(n);
				default:
					throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
				}
				bindings.put(node, type);
			}
		else
			type = n.f0.accept(this, argu);

		bindings.put(n, type);
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Selector n, final SymbolTable argu) {
		syms.put(n, argu);

		final String selector = n.f1.f0.tokenImage;
		SizzleType type = argu.getOperandType();

		if (type instanceof SizzleProtoMap) {
			// FIXME rdyer how do we verify the enum value exists?
//			if (!((SizzleTuple) type).hasMember(selector))
//				throw new TypeException(type + " has no member named '" + selector + "'");

			type = new SizzleInt();
		} else if (type instanceof SizzleTuple) {
			if (!((SizzleTuple) type).hasMember(selector))
				throw new TypeException(n, "'" + type + "' has no member named '" + selector + "'");

			type = ((SizzleTuple) type).getMember(selector);
		} else {
			throw new TypeException(n, "invalid operand type '" + type + "' for member selection");
		}

		bindings.put(n, type);
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Index n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleType index = n.f1.accept(this, argu);
		bindings.put(n, index);

		if (index == null)
			throw new RuntimeException();

		if (n.f2.present()) {
			if (!(index instanceof SizzleInt))
				throw new TypeException(n, "invalid type '" + index + "' for slice expression");

			final SizzleType slice = ((NodeSequence) n.f2.node).elementAt(1).accept(this, argu);

			if (!(slice instanceof SizzleInt))
				throw new TypeException(n, "invalid type '" + slice + "' for slice expression");
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Call n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present())
			bindings.put(n, n.f1.node.accept(this, argu));
		else
			bindings.put(n, new SizzleArray());

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Operand n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, super.visit(n, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visitOperandDollar(final Operand n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visitOperandFactor(final NodeToken op, final Factor n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visitOperandParen(final Expression n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Composite n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;

			switch (nodeChoice.which) {
			case 0: // pair list
			case 1: // expression list
				bindings.put(n, nodeChoice.choice.accept(this, argu));
				break;
			case 2: // empty map
				bindings.put(n, new SizzleMap());
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			bindings.put(n, new SizzleArray());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final PairList n, final SymbolTable argu) {
		syms.put(n, argu);

		final SizzleMap sizzleMap = (SizzleMap) n.f0.accept(this, argu);
		bindings.put(n, sizzleMap);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				if (!sizzleMap.assigns(((NodeSequence) node).elementAt(1).accept(this, argu)))
					throw new TypeException(n, "incorrect type '" + node + "' for " + sizzleMap);

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Pair n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleMap(n.f0.accept(this, argu), n.f2.accept(this, argu)));
		return bindings.get(n);
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

		syms.put(n, st);

		bindings.put(n, n.f0.accept(this, st));
		n.f1.accept(this, st);

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Proto n, final SymbolTable argu) {
		syms.put(n, argu);

		n.f1.accept(this, argu);
		final String tokenImage = ((NodeToken) n.f1.f0.choice).tokenImage;

		argu.importProto(tokenImage.substring(1, tokenImage.length() - 1));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final Identifier n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f0.tokenImage;

		if (argu.hasType(id)) {
			bindings.put(n, argu.getType(id));
			return bindings.get(n);
		}

		try {
			bindings.put(n, argu.get(id));
			return bindings.get(n);
		} catch (final RuntimeException e) {
			throw new TypeException(n, "invalid identifier '" + id + "'", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final IntegerLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FingerprintLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleFingerprint());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final FloatingPointLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleFloat());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final CharLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StringLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleString());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final BytesLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleBytes());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final TimeLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleTime());
		return bindings.get(n);
	}

	List<SizzleType> check(final Call c) {
		return check(c, getSyms(c));
	}

	List<SizzleType> check(final Call c, final SymbolTable argu) {
		if (c.f1.present())
			return this.check((ExprList) c.f1.node, argu);

		return new ArrayList<SizzleType>();
	}

	private List<SizzleType> check(final ExprList e) {
		return check(e, getSyms(e));
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

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final EmptyStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final StopStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		if (!argu.getIsBeforeVisitor())
			throw new TypeException(n, "Stop statement only allowed inside non-wildcard 'before' clauses");
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VisitorExpr n, final SymbolTable argu) {
		syms.put(n, argu);

		n.f1.accept(this, argu);
		bindings.put(n, n.f0.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VisitorType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new SizzleVisitor());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleType visit(final VisitStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		syms.put(n, st);
		
		st.setIsBeforeVisitor(n.f0.which == 0 && n.f1.which != 2);

		switch (n.f1.which) {
		case 0: // single type
			final Identifier typeId = (Identifier)((NodeSequence)n.f1.choice).nodes.get(2);
			final String typeName = typeId.f0.tokenImage;
			final SizzleType t = typeId.accept(this, st);
			if (t == null)
				throw new TypeException(n, "Invalid type '" + typeName + "'");
			final Identifier id = (Identifier)((NodeSequence)n.f1.choice).nodes.get(0);
			st.set(id.f0.tokenImage, t);
			id.accept(this, st);
			break;
		case 1: // list of types
			final IdentifierList idlist = (IdentifierList)n.f1.choice;
			for (final Node ns : idlist.f1.nodes)
				ns.accept(this, st);
			break;
		case 2: // wildcard
			break;
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}

		n.f3.accept(this, st);

		return null;
	}
}
