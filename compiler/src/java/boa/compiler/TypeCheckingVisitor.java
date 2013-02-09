package boa.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.aggregators.AggregatorSpec;
import boa.types.*;

import boa.parser.syntaxtree.*;

/**
 * Prescan the Boa program and check that all variables are consistently
 * typed.
 * 
 * @author anthonyu
 * 
 */
public class TypeCheckingVisitor extends DefaultVisitor<BoaType, SymbolTable> {
	private Map<Node, BoaType> bindings = new HashMap<Node, BoaType>();
	private Map<Node, SymbolTable> syms = new HashMap<Node, SymbolTable>();

	public BoaType getBinding(Node n) {
		return bindings.get(n);
	}

	public SymbolTable getSyms(Node n) {
		return syms.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Program n, final SymbolTable argu) {
		syms.put(n, argu);

		for (final Node node : n.f0.nodes)
			bindings.put(node, node.accept(this, argu));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Declaration n, final SymbolTable argu) {
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
	public BoaType visit(final TypeDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f1.f0.tokenImage;

		if (argu.hasType(id))
			throw new TypeException(n.f1, "'" + id + "' already defined as type '" + argu.getType(id) + "'");

		bindings.put(n, new BoaName(n.f3.accept(this, argu)));
		bindings.put(n.f1, bindings.get(n));
		argu.setType(id, bindings.get(n));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final StaticVarDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f1.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final VarDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f0.f0.tokenImage;

		if (argu.contains(id))
			throw new TypeException(n.f0, "variable '" + id + "' already declared as '" + argu.get(id) + "'");

		BoaType rhs = null;
		if (n.f3.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f3.node;
			switch (nodeChoice.which) {
			case 0: // initializer
				Node elem = ((NodeSequence) nodeChoice.choice).elementAt(1);
				rhs = elem.accept(this, argu);
				if (rhs instanceof BoaFunction && !elem.accept(new IsFunctionVisitor(), argu))
					rhs = ((BoaFunction)rhs).getType();
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		}

		BoaType lhs;
		if (n.f2.present()) {
			lhs = n.f2.node.accept(this, argu);

			if (lhs instanceof BoaArray && rhs instanceof BoaTuple)
				rhs = new BoaArray(((BoaTuple)rhs).getMember(0));

			if (rhs != null && !lhs.assigns(rhs) && !argu.hasCast(rhs, lhs))
				throw new TypeException(n.f3, "incorrect type '" + rhs + "' for assignment to '" + id + ": " + lhs + "'");
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
	public BoaType visit(final Type n, final SymbolTable argu) {
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
	public BoaType visit(final Component n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f0.present()) {
			final Identifier id = (Identifier) ((NodeSequence) n.f0.node).elementAt(0);
			final BoaType type = n.f1.accept(this, argu);
			bindings.put(n, new BoaName(type, id.f0.tokenImage));
			bindings.put(id, type);
		} else {
			bindings.put(n, n.f1.accept(this, argu));
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ArrayType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaArray(n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final TupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final SimpleTupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present())
			bindings.put(n, new BoaTuple(this.check((SimpleMemberList) n.f1.node, argu)));
		else
			bindings.put(n, new BoaTuple(new ArrayList<BoaType>()));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final SimpleMember n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ProtoTupleType n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f2.present())
			bindings.put(n, new BoaProtoTuple(this.check((SimpleMemberList) n.f2.node, argu)));
		else
			bindings.put(n, new BoaProtoTuple(new ArrayList<BoaType>()));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final MapType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaMap(n.f5.accept(this, argu), n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final StackType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaStack(n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final OutputType n, final SymbolTable argu) {
		syms.put(n, argu);

		List<BoaScalar> indexTypes = null;
		if (n.f3.present()) {
			indexTypes = new ArrayList<BoaScalar>();

			for (final Node node : n.f3.nodes) {
				final Node curNode = ((NodeSequence) node).elementAt(1);
				final BoaType boaType = curNode.accept(this, argu);

				if (!(boaType instanceof BoaScalar))
					throw new TypeException(curNode, "incorrect type '" + boaType + "' for index");

				indexTypes.add((BoaScalar) boaType);
			}
		}

		final BoaType type = n.f5.accept(this, argu);

		final AggregatorSpec annotation = argu.getAggregators(n.f1.f0.tokenImage, type).get(0).getAnnotation(AggregatorSpec.class);

		BoaScalar tweight = null;
		if (n.f6.present()) {
			if (annotation.weightType().equals("none"))
				throw new TypeException(n.f6, "unexpected weight for table declaration");

			final BoaType aweight = argu.getType(annotation.weightType());
			tweight = (BoaScalar) ((NodeSequence) n.f6.node).nodes.get(1).accept(this, argu);

			if (!aweight.assigns(tweight))
				throw new TypeException(n.f6, "incorrect weight type for table declaration");
		} else if (!annotation.weightType().equals("none"))
			throw new TypeException(n, "missing weight for table declaration");

		if (n.f2.present())
			if (annotation.formalParameters().length == 0)
				throw new TypeException(n.f2, "table '" + n.f1.f0.tokenImage + "' takes no arguments");

		bindings.put(n, new BoaTable(type, indexTypes, tweight));
		argu.set(n.f1.f0.tokenImage, bindings.get(n));
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ExprList n, final SymbolTable argu) {
		syms.put(n, argu);

		final List<BoaType> types = this.check(n);

//		final BoaType t = types.get(0);

//		for (int i = 1; i < types.size(); i++)
//			if (!t.assigns(types.get(i)))
				bindings.put(n, new BoaTuple(types));

//		bindings.put(n, new BoaArray(t));

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final FunctionType n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType[] params;
		if (n.f2.present()) {
			final List<Identifier> idents = new ArrayList<Identifier>();
			final List<BoaType> types = new ArrayList<BoaType>();

			final NodeSequence nodes = (NodeSequence)n.f2.node;

			idents.add((Identifier)nodes.elementAt(0));
			types.add(nodes.elementAt(2).accept(this, argu));
			
			final NodeListOptional paramList = (NodeListOptional)nodes.elementAt(3);
			if (paramList.present())
				for (Node paramNodes : paramList.nodes) {
					idents.add((Identifier)((NodeSequence)paramNodes).elementAt(1));
					types.add(((NodeSequence)paramNodes).elementAt(3).accept(this, argu));
				}

			params = new BoaType[idents.size()];

			for (int i = 0; i < params.length; i++) {
				params[i] = new BoaName(types.get(i), idents.get(i).f0.tokenImage);
				argu.set(idents.get(i).f0.tokenImage, types.get(i));
				idents.get(i).accept(this, argu);
			}
		} else {
			params = new BoaType[0];
		}

		final BoaType ret;
		if (n.f4.present())
			ret = ((NodeSequence)n.f4.node).elementAt(1).accept(this, argu);
		else
			ret = new BoaAny();

		bindings.put(n, new BoaFunction(ret, params));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Statement n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.f0.choice.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Assignment n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType lhs = n.f0.accept(this, argu);
		final BoaType rhs = n.f2.accept(this, argu);

		if (!(lhs instanceof BoaArray && rhs instanceof BoaTuple))
			if (!lhs.assigns(rhs))
				throw new TypeException(n.f2, "invalid type '" + rhs + "' for assignment to '" + lhs + "'");

		bindings.put(n, lhs);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Block n, final SymbolTable argu) {
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
	public BoaType visit(final BreakStatement n, final SymbolTable argu) {
		syms.put(n, argu);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ContinueStatement n, final SymbolTable argu) {
		syms.put(n, argu);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final DoStatement n, final SymbolTable argu) {
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
	public BoaType visit(final EmitStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final String id = n.f0.f0.tokenImage;
		final BoaType type = n.f0.accept(this, argu);

		if (type == null)
			throw new TypeException(n.f0, "emitting to undeclared output '" + id + "'");
		if (!(type instanceof BoaTable))
			throw new TypeException(n.f0, "emitting to non-output variable '" + id + "'");

		final BoaTable t = (BoaTable) type;

		if (n.f1.present()) {
			if (n.f1.nodes.size() != t.countIndices())
				throw new TypeException(n.f0, "incorrect number of indices for '" + id + "'");

			final List<BoaType> indices = new ArrayList<BoaType>();
			for (int i = 0; i < n.f1.nodes.size() && i < t.countIndices(); i++) {
				final Node node = n.f1.nodes.get(i);
				indices.add(((NodeSequence) node).nodes.get(1).accept(this, argu));
				if (!t.getIndex(i).assigns(indices.get(i)))
					throw new TypeException(node, "incorrect type '" + indices.get(i) + "' for index '" + i + "'");
			}
		} else if (t.countIndices() > 0)
			throw new TypeException(n, "indices missing from emit");

		final BoaType expression = n.f3.accept(this, argu);
		if (!t.accepts(expression))
			throw new TypeException(n.f3, "incorrect type '" + expression + "' for '" + id + ": " + t + "'");

		if (n.f4.present()) {
			if (t.getWeightType() == null)
				throw new TypeException(n.f4, "weight found but output table not declared with a weight");

			final BoaType wtype = ((NodeSequence) n.f4.node).nodes.get(1).accept(this, argu);

			if (!t.acceptsWeight(wtype))
				throw new TypeException(n.f4, "incorrect type '" + wtype + "' for weight of '" + id + ": " + t.getWeightType() + "'");
		} else if (t.getWeightType() != null)
			throw new TypeException(n, "must specify a weight");

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ExprStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType type = n.f0.accept(this, argu);
		bindings.put(n, type);

		if (n.f1.present() && !(type instanceof BoaInt))
			throw new TypeException(n.f0, "'" + type + "' not valid for operator '" + n.f1.toString() + "'");

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ForStatement n, final SymbolTable argu) {
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
	public BoaType visit(final ForVarDecl n, final SymbolTable argu) {
		syms.put(n, argu);

		final VarDecl varDecl = new VarDecl(n.f0, n.f1, n.f2, n.f3, new NodeToken(";"));

		bindings.put(n, varDecl.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ForExprStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final ExprStatement exprStatement = new ExprStatement(n.f0, n.f1, new NodeToken(";"));

		bindings.put(n, exprStatement.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final IfStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType test = n.f2.accept(this, argu);

		if (!(test instanceof BoaBool) && !(test instanceof BoaFunction && ((BoaFunction) test).getType() instanceof BoaBool))
			throw new TypeException(n.f2, "invalid type '" + test + "' for if condition");

		n.f4.accept(this, argu);

		if (n.f5.present())
			((Statement) ((NodeSequence) n.f5.node).elementAt(1)).accept(this, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final ReturnStatement n, final SymbolTable argu) {
		if (argu.getIsBeforeVisitor())
			throw new TypeException(n, "return statement not allowed inside visitors");

		syms.put(n, argu);

		// FIXME rdyer need to check return type matches function declaration's return
		if (n.f1.present())
			bindings.put(n, n.f1.accept(this, argu));
		else
			bindings.put(n, new BoaAny());

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final WhenStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		syms.put(n, st);

		st.set(n.f2.f0.tokenImage, n.f4.accept(this, argu));
		n.f2.accept(this, st);

		BoaType cond = n.f6.accept(this, st);
		if (!(cond instanceof BoaBool))
			throw new TypeException(n.f6, "Quantifier condition must be boolean");

		n.f8.accept(this, st);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Expression n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType ltype = n.f0.accept(this, argu);
		bindings.put(n, ltype);

		if (n.f1.present()) {
			if (!(ltype instanceof BoaBool))
				throw new TypeException(n.f0, "invalid type '" + ltype + "' for disjunction");

			for (final Node node : n.f1.nodes) {
				final BoaType rtype = ((NodeSequence)node).elementAt(1).accept(this, argu);
	
				if (!(rtype instanceof BoaBool))
					throw new TypeException(node, "invalid type '" + rtype + "' for disjunction");
			}
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Conjunction n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType lhs = n.f0.accept(this, argu);
		bindings.put(n, lhs);

		if (n.f1.present()) {
			if (!(lhs instanceof BoaBool))
				throw new TypeException(n.f0, "invalid type '" + lhs + "' for conjunction");

			for (final Node node : n.f1.nodes) {
				final BoaType rhs = ((NodeSequence)node).nodes.elementAt(1).accept(this, argu);

				if (!(rhs instanceof BoaBool))
					throw new TypeException(node, "invalid type '" + rhs + "' for conjunction");
			}

			bindings.put(n, new BoaBool());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Comparison n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType lhs = n.f0.accept(this, argu);
		bindings.put(n, lhs);

		if (n.f1.present()) {
			final BoaType rhs = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);

			if (!rhs.compares(lhs))
				throw new TypeException(n.f1, "invalid type '" + rhs + "' for comparison with '" + lhs + "'");

			bindings.put(n, new BoaBool());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final SimpleExpr n, final SymbolTable argu) {
		syms.put(n, argu);

		BoaType type = n.f0.accept(this, argu);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				type = type.arithmetics(((NodeSequence) node).nodes.get(1).accept(this, argu));

		bindings.put(n, type);
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Term n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType accepts = n.f0.accept(this, argu);
		bindings.put(n, accepts);

		if (n.f1.present()) {
			BoaScalar type;

			if (accepts instanceof BoaFunction)
				type = (BoaScalar) ((BoaFunction) accepts).getType();
			else
				type = (BoaScalar) accepts;

			for (final Node node : n.f1.nodes)
				type = type.arithmetics(((NodeSequence) node).nodes.get(1).accept(this, argu));

			bindings.put(n, type);
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Factor n, final SymbolTable argu) {
		syms.put(n, argu);

		BoaType type = null;

		if (n.f1.present())
			for (final Node node : n.f1.nodes) {
				final NodeChoice nodeChoice = (NodeChoice) node;
				switch (nodeChoice.which) {
				case 0: // selector
					if (type == null)
						type = n.f0.accept(this, argu);

					if (type instanceof BoaName)
						type = ((BoaName) type).getType();

					argu.setOperandType(type);
					type = ((Selector)nodeChoice.choice).accept(this, argu);
					break;
				case 1: // index
					if (type == null)
						type = n.f0.accept(this, argu);

					final BoaType index = ((Index) nodeChoice.choice).accept(this, argu);

					if (type instanceof BoaArray) {
						if (!(index instanceof BoaInt))
							throw new TypeException(nodeChoice.choice, "invalid operand type '" + index + "' for indexing into array");

						type = ((BoaArray) type).getType();
					} else if (type instanceof BoaProtoList) {
						if (!(index instanceof BoaInt))
							throw new TypeException(nodeChoice.choice, "invalid operand type '" + index + "' for indexing into array");

						type = ((BoaProtoList) type).getType();
					} else if (type instanceof BoaMap) {
						if (!((BoaMap) type).getIndexType().assigns(index))
							throw new TypeException(nodeChoice.choice, "invalid operand type '" + index + "' for indexing into '" + type + "'");

						type = ((BoaMap) type).getType();
					} else {
						throw new TypeException(nodeChoice.choice, "invalid operand type '" + type + "' for indexing expression");
					}
					break;
				case 2: // call
					((Call) nodeChoice.choice).accept(this, argu);
					syms.put(((Operand)n.f0).f0.choice, argu);

					final List<BoaType> formalParameters = this.check((Call) nodeChoice.choice, argu);

					bindings.put(node, new FunctionFindingVisitor(formalParameters).visit(n.f0, argu).erase(formalParameters));
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
	public BoaType visit(final Selector n, final SymbolTable argu) {
		syms.put(n, argu);

		final String selector = n.f1.f0.tokenImage;
		BoaType type = argu.getOperandType();

		if (type instanceof BoaProtoMap) {
			if (!((BoaProtoMap) type).hasAttribute(selector))
				throw new TypeException(n.f1, type + " has no member named '" + selector + "'");

			type = new BoaInt();
		} else if (type instanceof BoaTuple) {
			if (!((BoaTuple) type).hasMember(selector))
				throw new TypeException(n.f1, "'" + type + "' has no member named '" + selector + "'");

			type = ((BoaTuple) type).getMember(selector);
		} else {
			throw new TypeException(n, "invalid operand type '" + type + "' for member selection");
		}

		bindings.put(n, type);
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Index n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaType index = n.f1.accept(this, argu);
		bindings.put(n, index);

		if (index == null)
			throw new RuntimeException();

		if (n.f2.present()) {
			if (!(index instanceof BoaInt))
				throw new TypeException(n.f1, "invalid type '" + index + "' for slice expression");

			final BoaType slice = ((NodeSequence) n.f2.node).elementAt(1).accept(this, argu);

			if (!(slice instanceof BoaInt))
				throw new TypeException(n.f2, "invalid type '" + slice + "' for slice expression");
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Call n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present())
			bindings.put(n, n.f1.node.accept(this, argu));
		else
			bindings.put(n, new BoaArray());

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Operand n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, super.visit(n, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visitOperandDollar(final Operand n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visitOperandFactor(final NodeToken op, final Factor n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visitOperandParen(final Expression n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, n.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Composite n, final SymbolTable argu) {
		syms.put(n, argu);

		if (n.f1.present()) {
			final NodeChoice nodeChoice = (NodeChoice) n.f1.node;

			switch (nodeChoice.which) {
			case 0: // pair list
			case 1: // expression list
				bindings.put(n, nodeChoice.choice.accept(this, argu));
				break;
			case 2: // empty map
				bindings.put(n, new BoaMap());
				break;
			default:
				throw new RuntimeException("unexpected choice " + nodeChoice.which + " is " + nodeChoice.choice.getClass());
			}
		} else {
			bindings.put(n, new BoaArray());
		}

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final PairList n, final SymbolTable argu) {
		syms.put(n, argu);

		final BoaMap boaMap = (BoaMap) n.f0.accept(this, argu);
		bindings.put(n, boaMap);

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				if (!boaMap.assigns(((NodeSequence) node).elementAt(1).accept(this, argu)))
					throw new TypeException(node, "incorrect type '" + node + "' for " + boaMap);

		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Pair n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaMap(n.f0.accept(this, argu), n.f2.accept(this, argu)));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Function n, final SymbolTable argu) {
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
	public BoaType visit(final Proto n, final SymbolTable argu) {
		syms.put(n, argu);

		n.f1.accept(this, argu);
		final String tokenImage = ((NodeToken) n.f1.f0.choice).tokenImage;

		argu.importProto(tokenImage.substring(1, tokenImage.length() - 1));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final Identifier n, final SymbolTable argu) {
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
	public BoaType visit(final IntegerLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final FingerprintLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaFingerprint());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final FloatingPointLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaFloat());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final CharLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaInt());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final StringLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaString());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final BytesLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaBytes());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final TimeLiteral n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaTime());
		return bindings.get(n);
	}

	List<BoaType> check(final Call c) {
		return check(c, getSyms(c));
	}

	List<BoaType> check(final Call c, final SymbolTable argu) {
		if (c.f1.present())
			return this.check((ExprList) c.f1.node, argu);

		return new ArrayList<BoaType>();
	}

	private List<BoaType> check(final ExprList e) {
		return check(e, getSyms(e));
	}

	private List<BoaType> check(final ExprList e, final SymbolTable argu) {
		final List<BoaType> types = new ArrayList<BoaType>();

		types.add(assignableType(e.f0.accept(this, argu)));

		if (e.f1.present())
			for (final Node node : e.f1.nodes)
				types.add(assignableType(((NodeSequence) node).elementAt(1).accept(this, argu)));

		return types;
	}

	private BoaType assignableType(BoaType t) {
		if (t instanceof BoaFunction)
			return ((BoaFunction) t).getType();
		return t;
	}

	private List<BoaType> check(final SimpleMemberList e, final SymbolTable argu) {
		final List<BoaType> types = new ArrayList<BoaType>();

		types.add(e.f0.accept(this, argu));

		if (e.f1.present())
			for (final Node node : e.f1.nodes)
				types.add(((NodeSequence) node).elementAt(1).accept(this, argu));

		return types;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final EmptyStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final StopStatement n, final SymbolTable argu) {
		syms.put(n, argu);

		if (!argu.getIsBeforeVisitor())
			throw new TypeException(n, "Stop statement only allowed inside non-wildcard 'before' clauses");
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final VisitorExpr n, final SymbolTable argu) {
		syms.put(n, argu);

		n.f1.accept(this, argu);
		bindings.put(n, n.f0.accept(this, argu));
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final VisitorType n, final SymbolTable argu) {
		syms.put(n, argu);

		bindings.put(n, new BoaVisitor());
		return bindings.get(n);
	}

	/** {@inheritDoc} */
	@Override
	public BoaType visit(final VisitStatement n, final SymbolTable argu) {
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
			final BoaType t = typeId.accept(this, st);
			if (t == null)
				throw new TypeException(n.f1, "Invalid type '" + typeName + "'");
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
