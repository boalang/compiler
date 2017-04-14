/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import org.scannotation.AnnotationDB;

import boa.aggregators.AggregatorSpec;
import boa.compiler.ast.Operand;
import boa.functions.FunctionSpec;
import boa.types.*;
import boa.types.proto.*;
import boa.types.proto.enums.*;
import boa.types.shadow.*;

/**
 * @author anthonyu
 * @author rdyer
 * @author rramu
 */
public class SymbolTable {
	private static HashMap<String, Class<?>> aggregators;
	private static final Map<Class<?>, BoaType> protomap;
	private static Map<String, BoaType> idmap;
	private static final Map<String, BoaType> globals;
	private static FunctionTrie globalFunctions;

	private FunctionTrie functions;
	private Map<String, BoaType> locals;

	private String id;
	private Operand operand;
	private Stack<BoaType> operandType = new Stack<BoaType>();
	private boolean needsBoxing;
	private boolean isBeforeVisitor = false;
	private boolean isTraverse = false;
	private boolean shadowing = false;

	static {
		aggregators = new HashMap<String, Class<?>>();

		// this maps the Java types in protocol buffers into Boa types
		protomap = new HashMap<Class<?>, BoaType>();

		protomap.put(int.class, new BoaInt());
		protomap.put(long.class, new BoaInt());
		protomap.put(float.class, new BoaFloat());
		protomap.put(double.class, new BoaFloat());
		protomap.put(boolean.class, new BoaBool());
		protomap.put(Object.class, new BoaString());

		// variables with a global scope
		globals = new HashMap<String, BoaType>();

		globals.put("input", new ProjectProtoTuple());
		globals.put("true", new BoaBool());
		globals.put("false", new BoaBool());
		globals.put("PI", new BoaFloat());
		globals.put("Inf", new BoaFloat());
		globals.put("inf", new BoaFloat());
		globals.put("NaN", new BoaFloat());
		globals.put("nan", new BoaFloat());

		// this maps scalar Boa scalar types names to their classes
		idmap = new HashMap<String, BoaType>();

		idmap.put("any", new BoaAny());
		idmap.put("none", null);
		idmap.put("bool", new BoaBool());
		idmap.put("int", new BoaInt());
		idmap.put("float", new BoaFloat());
		idmap.put("time", new BoaTime());
		idmap.put("string", new BoaString());

		final BoaProtoTuple[] dslTupleTypes = {
			new ASTRootProtoTuple(),
			new AttachmentProtoTuple(),
			new CFGProtoTuple(),
			new CFGNodeProtoTuple(),
			new CFGEdgeProtoTuple(),
			new ChangedFileProtoTuple(),
			new CodeRepositoryProtoTuple(),
			new CommentProtoTuple(),
			new CommentsRootProtoTuple(),
			new DeclarationProtoTuple(),
			new ExpressionProtoTuple(),
			new IssueProtoTuple(),
			new IssueCommentProtoTuple(),
			new IssueRepositoryProtoTuple(),
			new IssuesRootProtoTuple(),
			new MethodProtoTuple(),
			new ModifierProtoTuple(),
			new NamespaceProtoTuple(),
			new PersonProtoTuple(),
			new ProjectProtoTuple(),
			new RevisionProtoTuple(),
			new StatementProtoTuple(),
			new TypeProtoTuple(),
			new VariableProtoTuple(),
		};
		final BoaProtoMap[] dslMapTypes = {
			new CFGNodeTypeProtoMap(),
			new CFGEdgeLabelProtoMap(),
			new ChangeKindProtoMap(),
			new CommentKindProtoMap(),
			new ExpressionKindProtoMap(),
			new FileKindProtoMap(),
			new ForgeKindProtoMap(),
			new IssueKindProtoMap(),
			new ModifierKindProtoMap(),
			new RepositoryKindProtoMap(),
			new StatementKindProtoMap(),
			new TraversalKindProtoMap(),
			new TraversalDirectionProtoMap(),
			new TypeKindProtoMap(),
			new VisibilityProtoMap(),
		};

		for (final BoaType t : dslTupleTypes)
			idmap.put(t.toString(), t);

		for (final BoaType t : dslMapTypes)
			idmap.put(t.toString(), t);

		// TODO add shadow types
		idmap.put("IfStatement", new IfStatementShadow());

		globalFunctions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		// helper for shadow type codegen
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("safeget", new BoaFunction(t, new BoaType[] { new BoaProtoList(t), new BoaInt(), new BoaString() }, "(${0}.size() <= ${1} ? (${2})null : ${0}.get((int)${1}))"));

		// FIXME rdyer - def(protolist[i]) should generate "i < protolist.size()"
		globalFunctions.addFunction("def", new BoaFunction(new BoaBool(), new BoaType[] { new BoaAny() }, "(${0} != null)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaProtoList(new BoaAny()) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaArray(new BoaAny()) }, "((long)${0}.length)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "((long)${0}.keySet().size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "((long)${0}.length())"));

		// traversal functions
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaTypeVar("K"), new BoaType[] { new CFGNodeProtoTuple(), new BoaTraversal(new BoaTypeVar("K"))},"${1}.getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaAny(), new BoaType[] { new CFGNodeProtoTuple()},"getValue(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaTraversal()},"${0}.clear()"));

		// map functions
		globalFunctions.addFunction("haskey", new BoaFunction(new BoaBool(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.containsKey(${1})"));
		globalFunctions.addFunction("keys", new BoaFunction(new BoaArray(new BoaTypeVar("K")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.keySet().toArray(new ${K}[0]))"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.values().toArray(new ${V}[0]))"));
		globalFunctions.addFunction("lookup", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K"), new BoaTypeVar("V") }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.clear()"));

		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()), new BoaInt() }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\", ${1})"));
		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()) }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\")"));

		// visitors
		for (final BoaType t : dslTupleTypes) {
			globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { t, new BoaVisitor() }, "${1}.visit(${0})"));
			globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { t }, "visit(${0})"));
			globalFunctions.addFunction("ast_len", new BoaFunction(new BoaInt(), new BoaType[] { t }, "boa.functions.BoaAstIntrinsics.lenVisitor.getCount(${0})"));
		}
		globalFunctions.addFunction("_cur_visitor", new BoaFunction(new BoaVisitor(), new BoaType[] { }, "this"));

		//traversal
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaAny(), new BoaType[] { new CFGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal()}, "${3}.traverse(${0},${1},${2})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaBool(), new BoaType[] { new CFGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal(), new BoaFixP() }, "${3}.traverse(${0},${1},${2},${4})"));

		// stack functions
		globalFunctions.addFunction("push", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.push(${1})"));
		globalFunctions.addFunction("pop", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_pop(${0})"));
		globalFunctions.addFunction("peek", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_peek(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "${0}.clear()"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new ${V}[0]))"));

		// set functions
		globalFunctions.addFunction("contains", new BoaFunction(new BoaBool(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.contains(${1})"));
		globalFunctions.addFunction("containsall", new BoaFunction(new BoaBool(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V"))}, "${0}.containsAll(${1})"));
		globalFunctions.addFunction("add", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.add(${1})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "${0}.clear()"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new ${V}[0]))"));

		globalFunctions.addFunction("union", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_union(${0}, ${1})"));
		globalFunctions.addFunction("intersect", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_intersect(${0}, ${1})"));
		globalFunctions.addFunction("difference", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_difference(${0}, ${1})"));
		globalFunctions.addFunction("symdiff", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_symdiff(${0}, ${1})"));

		// casts from enums to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoMap() }, "${0}.name()"));

		// arrays to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaArray(new BoaAny()) }, "boa.functions.BoaIntrinsics.arrayToString(${0})"));

		//set to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "${0}.toString()"));

		// current() function inside visits
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("current", new BoaFunction(t, new BoaType[] { t }, ""));

		// proto to string
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { t }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// FIXME the json library doesnt support enums
		//for (final BoaType t : dslMapTypes)
		//	globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { t }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// FIXME the json library doesnt support lists
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoList(new BoaAny()) }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// string to bool
		globalFunctions.addFunction("bool", new BoaFunction("boa.functions.BoaCasts.stringToBoolean", new BoaBool(), new BoaType[] { new BoaString() }));

		// bool to int
		globalFunctions.addFunction("int", new BoaFunction("boa.functions.BoaCasts.booleanToLong", new BoaInt(), new BoaType[] { new BoaBool() }));
		// float to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaFloat() }, "((long)${0})"));
		// time to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaTime() }, "${0}"));
		// string to int
		globalFunctions.addFunction("int", new BoaFunction("java.lang.Long.decode", new BoaInt(), new BoaType[] { new BoaString() }));
		// string to int with param base
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString(), new BoaInt() }, "java.lang.Long.parseLong(${0}, (int)${1})"));

		// hashing functions
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "((long)${0}.hashCode())"));
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { t }, "((long)${0}.hashCode())"));
		for (final BoaType t : dslMapTypes)
			globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { t }, "((long)${0}.hashCode())"));

		// int to float
		globalFunctions.addFunction("float", new BoaFunction(new BoaFloat(), new BoaType[] { new BoaInt() }, "(double)${0}"));
		// string to float
		globalFunctions.addFunction("float", new BoaFunction("java.lang.Double.parseDouble", new BoaFloat(), new BoaType[] { new BoaString() }));

		// int to time
		globalFunctions.addFunction("time", new BoaFunction(new BoaTime(), new BoaType[] { new BoaInt() }, "${0}"));
		// string to time
		globalFunctions.addFunction("time", new BoaFunction("boa.functions.BoaCasts.stringToTime", new BoaTime(), new BoaType[] { new BoaString() }));
		// string to time
		globalFunctions.addFunction("time", new BoaFunction("boa.functions.BoaCasts.stringToTime", new BoaTime(), new BoaType[] { new BoaString(), new BoaString() }));

		// bool to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Boolean.toString", new BoaString(), new BoaType[] { new BoaBool() }));
		// int to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.longToString", new BoaString(), new BoaType[] { new BoaInt() }));
		// int to string with parameter base
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.longToString", new BoaString(), new BoaType[] { new BoaInt(), new BoaInt() }));
		// float to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.doubleToString", new BoaString(), new BoaType[] { new BoaFloat() }));
		// time to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.timeToString", new BoaString(), new BoaType[] { new BoaTime() }));

		// self casts
		globalFunctions.addFunction("bool", new BoaFunction(new BoaBool(), new BoaType[] { new BoaBool() }, "${0}"));
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaInt() }, "${0}"));
		globalFunctions.addFunction("float", new BoaFunction(new BoaFloat(), new BoaType[] { new BoaFloat() }, "${0}"));
		globalFunctions.addFunction("time", new BoaFunction(new BoaTime(), new BoaType[] { new BoaTime() }, "${0}"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaString() }, "${0}"));

		/* expose the java.lang.Math class to Sawzall */

		globalFunctions.addFunction("highbit", new BoaFunction("java.lang.Long.highestOneBit", new BoaInt(), new BoaType[] { new BoaInt() }));

		// abs just needs to be overloaded
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaType[] { new BoaInt() }));
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaType[] { new BoaFloat() }));

		// expose the rest of the unary functions
		for (final String s : Arrays.asList("log", "log10", "exp", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "cosh", "sinh", "tanh", "ceil", "floor", "round", "cbrt", "expm1", "log1p", "rint", "signum", "ulp"))
			globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, new BoaFloat(), new BoaType[] { new BoaFloat() }));

		// expose the binary functions
		for (final String s : Arrays.asList("pow", "atan2", "hypot"))
			globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, new BoaFloat(), new BoaType[] { new BoaFloat(), new BoaFloat() }));

		// these three have capitals in the name
		globalFunctions.addFunction("ieeeremainder", new BoaFunction("java.lang.Math.IEEEremainder", new BoaFloat(), new BoaType[] { new BoaFloat(), new BoaFloat() }));
		globalFunctions.addFunction("todegrees", new BoaFunction("java.lang.Math.toDegrees", new BoaFloat(), new BoaType[] { new BoaFloat() }));
		globalFunctions.addFunction("toradians", new BoaFunction("java.lang.Math.toRadians", new BoaFloat(), new BoaType[] { new BoaFloat() }));

		// max and min
		for (final String s : Arrays.asList("max", "min"))
			for (final BoaType t : Arrays.asList(new BoaInt(), new BoaFloat()))
				globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, t, new BoaType[] { t, t }));

		globalFunctions.addFunction("max", new BoaFunction(new BoaTime(), new BoaType[] { new BoaTime(), new BoaTime() }, "(${0} > ${1} ? ${0} : ${1})"));
		globalFunctions.addFunction("min", new BoaFunction(new BoaTime(), new BoaType[] { new BoaTime(), new BoaTime() }, "(${0} < ${1} ? ${0} : ${1})"));

		globalFunctions.addFunction("max", new BoaFunction(new BoaString(), new BoaType[] { new BoaString(), new BoaString() }, "(${0}.compareTo(${1}) > 0 ? ${0} : ${1})"));
		globalFunctions.addFunction("min", new BoaFunction(new BoaString(), new BoaType[] { new BoaString(), new BoaString() }, "(${0}.compareTo(${1}) < 0 ? ${0} : ${1})"));
	}

	public SymbolTable() {
		// variables with a local scope
		this.locals = new HashMap<String, BoaType>();
		functions = new FunctionTrie();
	}

	public static void initialize(final List<URL> libs) throws IOException {
		importLibs(libs);
	}

	public SymbolTable cloneNonLocals() throws IOException {
		SymbolTable st = new SymbolTable();

		st.functions = this.functions;
		st.locals = new HashMap<String, BoaType>(this.locals);
		st.isBeforeVisitor = this.isBeforeVisitor;
		st.shadowing = this.shadowing;

		return st;
	}

	public void set(final String id, final BoaType type) {
		this.set(id, type, false);
	}

	public void set(final String id, final BoaType type, final boolean global) {
		if (idmap.containsKey(id))
			throw new RuntimeException(id + " already declared as type " + idmap.get(id));

		if (type instanceof BoaFunction)
			this.setFunction(id, (BoaFunction) type);

		if (global)
			globals.put(id, type);
		else
			this.locals.put(id, type);
	}

	public boolean hasGlobal(final String id) {
		return globals.containsKey(id);
	}

	public boolean hasLocal(final String id) {
		return this.locals.containsKey(id);
	}

	public BoaType get(final String id) {
		if (idmap.containsKey(id))
			return idmap.get(id);

		if (globals.containsKey(id))
			return globals.get(id);

		if (this.locals.containsKey(id))
			return this.locals.get(id);

		throw new RuntimeException("no such identifier " + id);
	}

	public boolean hasType(final String id) {
		return idmap.containsKey(id);
	}

	public static BoaType getType(final String id) {
		if (idmap.containsKey(id))
			return idmap.get(id);

		if (id.startsWith("array of "))
			return new BoaArray(getType(id.substring("array of ".length()).trim()));

		if (id.startsWith("stack of "))
			return new BoaStack(getType(id.substring("stack of ".length()).trim()));

		if (id.startsWith("set of "))
			return new BoaSet(getType(id.substring("set of ".length()).trim()));

		if (id.startsWith("stack of "))
			return new BoaStack(getType(id.substring("stack of ".length()).trim()));

		if (id.startsWith("map"))
			return new BoaMap(getType(id.substring(id.indexOf(" of ") + " of ".length()).trim()),
					getType(id.substring(id.indexOf("[") + 1, id.indexOf("]")).trim()));

		throw new RuntimeException("no such type " + id);
	}

	public void setType(final String id, final BoaType boaType) {
		idmap.put(id, boaType);
	}

	private static void importAggregator(final Class<?> clazz) {
		if (!clazz.isAnnotationPresent(AggregatorSpec.class))
			return;

		final AggregatorSpec annotation = clazz.getAnnotation(AggregatorSpec.class);

		if (annotation == null)
			return;

		final String type = annotation.type();
		if (type.equals("any"))
			aggregators.put(annotation.name(), clazz);
		else
			aggregators.put(annotation.name() + ":" + type, clazz);
	}

	private static void importAggregator(final String c) {
		try {
			importAggregator(Class.forName(c));
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("no such class " + c, e);
		}
	}

	public Class<?> getAggregator(final String name, final BoaType type) {
		if (aggregators.containsKey(name + ":" + type))
			return aggregators.get(name + ":" + type);
		else if (aggregators.containsKey(name))
			return aggregators.get(name);
		else
			throw new RuntimeException("no such aggregator " + name + " of " + type);
	}

	public List<Class<?>> getAggregators(final String name, final BoaType type) {
		final List<Class<?>> aggregators = new ArrayList<Class<?>>();

		if (type instanceof BoaTuple)
			for (final BoaType subType : ((BoaTuple) type).getTypes())
				aggregators.add(this.getAggregator(name, subType));
		else
			aggregators.add(this.getAggregator(name, type));

		return aggregators;
	}

	private static void importFunction(final Method m) {
		final FunctionSpec annotation = m.getAnnotation(FunctionSpec.class);

		if (annotation == null)
			return;

		final String[] formalParameters = annotation.formalParameters();
		final BoaType[] formalParameterTypes = new BoaType[formalParameters.length];

		for (int i = 0; i < formalParameters.length; i++) {
			final String id = formalParameters[i];

			// check for varargs
			if (id.endsWith("..."))
				formalParameterTypes[i] = new BoaVarargs(getType(id.substring(0, id.indexOf('.'))));
			else
				formalParameterTypes[i] = getType(id);
		}

		globalFunctions.addFunction(annotation.name(), new BoaFunction(m.getDeclaringClass().getCanonicalName() + '.' + m.getName(), getType(annotation.returnType()), formalParameterTypes));
	}

	private static void importFunctions(final Class<?> c) {
		for (final Method m : c.getMethods())
			if (m.isAnnotationPresent(FunctionSpec.class))
				importFunction(m);
	}

	private static void importFunctions(final String c) {
		try {
			importFunctions(Class.forName(c));
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("no such class " + c, e);
		}
	}

	private static void importLibs(final List<URL> urls) throws IOException {
		// load built-in functions
		final Class<?>[] builtinFuncs = {
			boa.functions.BoaAstIntrinsics.class,
			boa.functions.BoaGraphIntrinsics.class,
			boa.functions.BoaIntrinsics.class,
			boa.functions.BoaMetricIntrinsics.class,
			boa.functions.BoaModifierIntrinsics.class,
			boa.functions.BoaCasts.class,
			boa.functions.BoaMathIntrinsics.class,
			boa.functions.BoaSortIntrinsics.class,
			boa.functions.BoaSpecialIntrinsics.class,
			boa.functions.BoaStringIntrinsics.class,
			boa.functions.BoaTimeIntrinsics.class
		};
		for (final Class<?> c : builtinFuncs)
			importFunctions(c);

		// load built-in aggregators
		final Class<?>[] builtinAggs = {
			boa.aggregators.BottomAggregator.class,
			boa.aggregators.CollectionAggregator.class,
			boa.aggregators.ConfidenceIntervalAggregator.class,
			boa.aggregators.DistinctAggregator.class,
			boa.aggregators.FloatHistogramAggregator.class,
			boa.aggregators.FloatMeanAggregator.class,
			boa.aggregators.FloatQuantileAggregator.class,
			boa.aggregators.FloatSumAggregator.class,
			boa.aggregators.GraphAggregator.class,
			boa.aggregators.GraphvizAggregator.class,
			boa.aggregators.IntHistogramAggregator.class,
			boa.aggregators.IntMeanAggregator.class,
			boa.aggregators.IntQuantileAggregator.class,
			boa.aggregators.IntSumAggregator.class,
			boa.aggregators.KurtosisAggregator.class,
			boa.aggregators.LogAggregator.class,
			boa.aggregators.MaximumAggregator.class,
			boa.aggregators.MedianAggregator.class,
			boa.aggregators.MinimumAggregator.class,
			boa.aggregators.SetAggregator.class,
			boa.aggregators.SkewnessAggregator.class,
			boa.aggregators.StatisticsAggregator.class,
			boa.aggregators.StDevAggregator.class,
			boa.aggregators.TopAggregator.class,
			boa.aggregators.UniqueAggregator.class,
			boa.aggregators.VarianceAggregator.class,
		};
		for (final Class<?> c : builtinAggs)
			importAggregator(c);

		// also check any libs passed into the compiler
		if (urls.size() > 0) {
			final AnnotationDB db = new AnnotationDB();
			db.setScanMethodAnnotations(true);
			db.setScanClassAnnotations(true);
//			db.setScanPackages(new String[] {"boa.aggregators", "boa.functions"});

			for (final URL url : urls)
				db.scanArchives(url);
	
			final Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();
	
			for (final String s : annotationIndex.get(AggregatorSpec.class.getCanonicalName()))
				importAggregator(s);
	
			for (final String s : annotationIndex.get(FunctionSpec.class.getCanonicalName()))
				importFunctions(s);
		}
	}

	public BoaFunction getFunction(final String id) {
		return this.getFunction(id, new BoaType[0]);
	}

	public BoaFunction getFunction(final String id, final List<BoaType> formalParameters) {
		return this.getFunction(id, formalParameters.toArray(new BoaType[formalParameters.size()]));
	}

	public BoaFunction getFunction(final String id, final BoaType[] formalParameters) {
		BoaFunction function = globalFunctions.getFunction(id, formalParameters);
		if (function == null)
			function = this.functions.getFunction(id, formalParameters);
		if (function == null)
			throw new RuntimeException("no such function " + id + "(" + Arrays.toString(formalParameters) + ")");
		return function;
	}

	public boolean hasGlobalFunction(final String id) {
		return globalFunctions.hasFunction(id);
	}

	public boolean hasLocalFunction(final String id) {
		return functions.hasFunction(id);
	}

	public void setFunction(final String id, final BoaFunction boaFunction) {
		this.functions.addFunction(id, boaFunction);
	}

	public boolean hasCast(final BoaType from, final BoaType to) {
		try {
			this.getFunction(to.toString(), new BoaType[] { from });

			return true;
		} catch (final RuntimeException e) {
			return false;
		}
	}

	public BoaFunction getCast(final BoaType from, final BoaType to) {
		return this.getFunction(to.toString(), new BoaType[] { from });
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public void setOperand(final Operand operand) {
		this.operand = operand;
	}

	public Operand getOperand() {
		return this.operand;
	}

	public void setOperandType(final BoaType operandType) {
		this.operandType.push(operandType);
	}

	public BoaType getOperandType() {
		return this.operandType.pop();
	}

	public boolean hasOperandType() {
		return !this.operandType.empty();
	}

	public void setNeedsBoxing(final boolean needsBoxing) {
		this.needsBoxing = needsBoxing;
	}

	public boolean getNeedsBoxing() {
		return this.needsBoxing;
	}

	public void setIsTraverse(final boolean isTraverse) {
		this.isTraverse = isTraverse;
	}

	public boolean getIsTraverse() {
		return this.isTraverse;
	}

	public void setIsBeforeVisitor(final boolean isBeforeVisitor) {
		this.isBeforeVisitor = isBeforeVisitor;
	}

	public boolean getIsBeforeVisitor() {
		return this.isBeforeVisitor;
	}

	public void setShadowing(final boolean shadowing) {
		this.shadowing = shadowing;
	}

	public boolean getShadowing() {
		return this.shadowing;
	}

	@Override
	public String toString() {
		final List<String> r = new ArrayList<String>();

		for (final Entry<String, BoaType> entry : this.locals.entrySet())
			r.add(entry.getKey() + ":" + entry.getValue());

		return r.toString();
	}
}
