package boa.compiler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import org.scannotation.AnnotationDB;

import boa.aggregators.AggregatorSpec;
import boa.functions.FunctionSpec;
import boa.types.*;
import boa.types.proto.*;
import boa.types.proto.enums.*;

import boa.compiler.ast.Operand;

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

	static {
		aggregators = new HashMap<String, Class<?>>();

		// this maps the Java types in protocol buffers into Boa types
		protomap = new HashMap<Class<?>, BoaType>();

		protomap.put(int.class, new BoaInt());
		protomap.put(long.class, new BoaInt());
		protomap.put(float.class, new BoaFloat());
		protomap.put(double.class, new BoaFloat());
		protomap.put(boolean.class, new BoaBool());
		protomap.put(byte[].class, new BoaBytes());
		protomap.put(Object.class, new BoaString());

		// variables with a global scope
		globals = new HashMap<String, BoaType>();

		globals.put("input", new BoaBytes());
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
		idmap.put("fingerprint", new BoaFingerprint());
		idmap.put("string", new BoaString());
		idmap.put("bytes", new BoaBytes());

		idmap.put("ASTRoot", new ASTRootProtoTuple());
		idmap.put("Bug", new BugProtoTuple());
		idmap.put("BugRepository", new BugRepositoryProtoTuple());
		idmap.put("BugStatus", new IssueKindProtoMap());
		idmap.put("ChangedFile", new ChangedFileProtoTuple());
		idmap.put("ChangeKind", new ChangeKindProtoMap());
		idmap.put("CodeRepository", new CodeRepositoryProtoTuple());
		idmap.put("CommentKind", new CommentKindProtoMap());
		idmap.put("Comment", new CommentProtoTuple());
		idmap.put("Declaration", new DeclarationProtoTuple());
		idmap.put("ExpressionKind", new ExpressionKindProtoMap());
		idmap.put("Expression", new ExpressionProtoTuple());
		idmap.put("FileKind", new FileKindProtoMap());
		idmap.put("Method", new MethodProtoTuple());
		idmap.put("ModifierKind", new ModifierKindProtoMap());
		idmap.put("Modifier", new ModifierProtoTuple());
		idmap.put("Namespace", new NamespaceProtoTuple());
		idmap.put("Person", new PersonProtoTuple());
		idmap.put("Project", new ProjectProtoTuple());
		idmap.put("RepositoryKind", new RepositoryKindProtoMap());
		idmap.put("Revision", new RevisionProtoTuple());
		idmap.put("StatementKind", new StatementKindProtoMap());
		idmap.put("Statement", new StatementProtoTuple());
		idmap.put("TypeKind", new TypeKindProtoMap());
		idmap.put("Type", new TypeProtoTuple());
		idmap.put("Variable", new VariableProtoTuple());
		idmap.put("Visibility", new VisibilityProtoMap());

		globalFunctions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		// FIXME rdyer - def(protolist[i]) should generate "i < protolist.size()"
		globalFunctions.addFunction("def", new BoaFunction(new BoaBool(), new BoaType[] { new BoaAny() }, "(${0} != null)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaProtoList(new BoaScalar()) }, "${0}.size()"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaArray(new BoaScalar()) }, "${0}.length"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaMap(new BoaScalar(), new BoaScalar()) }, "${0}.keySet().size()"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaStack(new BoaScalar()) }, "${0}.size()"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "${0}.length()"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaBytes() }, "${0}.length"));

		// map functions
		globalFunctions.addFunction("haskey", new BoaFunction(new BoaBool(), new BoaType[] { new BoaMap(new BoaScalar(), new BoaScalar()), new BoaScalar() }, "${0}.containsKey(${1})"));
		globalFunctions.addFunction("keys", new BoaFunction(new BoaArray(new BoaTypeVar("K")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.keySet().toArray(new ${K}[0])"));
		globalFunctions.addFunction("lookup", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K"), new BoaTypeVar("V") }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.clear()"));

		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()), new BoaInt() }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\", ${1})"));
		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()) }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\")"));
		// these fingerprints are identity functions
		globalFunctions.addFunction("fingerprintof", new BoaFunction(new BoaFingerprint(), new BoaScalar[] { new BoaInt() }));
		globalFunctions.addFunction("fingerprintof", new BoaFunction(new BoaFingerprint(), new BoaScalar[] { new BoaTime() }));

		// visitors
		globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { new BoaScalar(), new BoaVisitor() }, "${1}.visit(${0})"));
		globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { new BoaScalar() }, "visit(${0})"));
		globalFunctions.addFunction("ast_len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaAny() }, "boa.functions.BoaAstIntrinsics.lenVisitor.getCount(${0})"));

		// stack functions
		globalFunctions.addFunction("push", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.push(${1})"));
		globalFunctions.addFunction("pop", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_pop(${0})"));
		globalFunctions.addFunction("peek", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_peek(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "${0}.clear()"));

		// AST comparisons
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new ProjectProtoTuple(), new ProjectProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new StatementProtoTuple(), new StatementProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new ASTRootProtoTuple(), new ASTRootProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new BugProtoTuple(), new BugProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new BugRepositoryProtoTuple(), new BugRepositoryProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new ChangedFileProtoTuple(), new ChangedFileProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new CodeRepositoryProtoTuple(), new CodeRepositoryProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new CommentProtoTuple(), new CommentProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new DeclarationProtoTuple(), new DeclarationProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new ExpressionProtoTuple(), new ExpressionProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new MethodProtoTuple(), new MethodProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new ModifierProtoTuple(), new ModifierProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new NamespaceProtoTuple(), new NamespaceProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new PersonProtoTuple(), new PersonProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new RevisionProtoTuple(), new RevisionProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new TypeProtoTuple(), new TypeProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));
		globalFunctions.addFunction("isequal", new BoaFunction(new BoaBool(), new BoaType[] { new VariableProtoTuple(), new VariableProtoTuple() }, "(${0}.hashCode() == ${1}.hashCode())"));

		// expose the casts for all possible input types
		globalFunctions.addFunction(new ProjectProtoTuple().toString(), new BoaFunction(new ProjectProtoTuple(), new BoaType[] { new BoaBytes() }, "${0}"));

		// string to bool
		globalFunctions.addFunction("bool", new BoaFunction("boa.functions.BoaCasts.stringToBoolean", new BoaBool(), new BoaScalar[] { new BoaString() }));

		// bool to int
		globalFunctions.addFunction("int", new BoaFunction("boa.functions.BoaCasts.booleanToLong", new BoaInt(), new BoaScalar[] { new BoaBool() }));
		// float to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaFloat() }, "(long)${0}"));
		// time to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaTime() }, "${0}"));
		// fingerprint to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaFingerprint() }, "${0}"));
		// string to int
		globalFunctions.addFunction("int", new BoaFunction("java.lang.Long.decode", new BoaInt(), new BoaScalar[] { new BoaString() }));
		// string to int with param base
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaString(), new BoaInt() }, "java.lang.Long.parseLong(${0}, (int)${1})"));
		// bytes to int with param encoding format
		globalFunctions.addFunction("int", new BoaFunction("boa.functions.BoaCasts.bytesToLong", new BoaInt(), new BoaScalar[] { new BoaBytes(), new BoaString() }));

		// int to float
		globalFunctions.addFunction("float", new BoaFunction(new BoaFloat(), new BoaScalar[] { new BoaInt() }, "(double)${0}"));
		// string to float
		globalFunctions.addFunction("float", new BoaFunction("java.lang.Double.parseDouble", new BoaFloat(), new BoaScalar[] { new BoaString() }));

		// int to time
		globalFunctions.addFunction("time", new BoaFunction(new BoaTime(), new BoaScalar[] { new BoaInt() }, "${0}"));
		// string to time
		globalFunctions.addFunction("time", new BoaFunction("boa.functions.BoaCasts.stringToTime", new BoaTime(), new BoaScalar[] { new BoaString() }));
		// string to time
		globalFunctions.addFunction("time", new BoaFunction("boa.functions.BoaCasts.stringToTime", new BoaTime(), new BoaScalar[] { new BoaString(), new BoaString() }));

		// int to fingerprint
		globalFunctions.addFunction("fingerprint", new BoaFunction(new BoaFingerprint(), new BoaScalar[] { new BoaInt() }, "${0}"));
		// string to fingerprint
		globalFunctions.addFunction("fingerprint", new BoaFunction("java.lang.Long.parseLong", new BoaInt(), new BoaScalar[] { new BoaString() }));
		// string to fingerprint with param base
		globalFunctions.addFunction("fingerprint", new BoaFunction("java.lang.Long.parseLong", new BoaInt(), new BoaScalar[] { new BoaString(), new BoaInt() }));
		// bytes to fingerprint
		globalFunctions.addFunction("fingerprint", new BoaFunction("boa.functions.BoaCasts.bytesToFingerprint", new BoaFingerprint(), new BoaScalar[] { new BoaBytes() }));

		// bool to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Boolean.toString", new BoaString(), new BoaScalar[] { new BoaBool() }));
		// int to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Long.toString", new BoaString(), new BoaScalar[] { new BoaInt() }));
		// int to string with parameter base
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.longToString", new BoaString(), new BoaScalar[] { new BoaInt(), new BoaInt() }));
		// float to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Double.toString", new BoaString(), new BoaScalar[] { new BoaFloat() }));
		// time to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.timeToString", new BoaString(), new BoaScalar[] { new BoaTime() }));
		// fingerprint to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Long.toHexString", new BoaString(), new BoaScalar[] { new BoaFingerprint() }));
		// bytes to string
		globalFunctions.addFunction("string", new BoaFunction("new java.lang.String", new BoaString(), new BoaScalar[] { new BoaBytes() }));
		// bytes to string
		globalFunctions.addFunction("string", new BoaFunction("new java.lang.String", new BoaString(), new BoaScalar[] { new BoaBytes(), new BoaString() }));

		// int to bytes with param encoding format
		globalFunctions.addFunction("bytes", new BoaFunction("boa.functions.BoaCasts.longToBytes", new BoaInt(), new BoaScalar[] { new BoaInt(), new BoaString() }));
		// fingerprint to bytes
		globalFunctions.addFunction("bytes", new BoaFunction("boa.functions.BoaCasts.fingerprintToBytes", new BoaBytes(), new BoaScalar[] { new BoaFingerprint() }));
		// string to bytes
		globalFunctions.addFunction("bytes", new BoaFunction("boa.functions.BoaCasts.stringToBytes", new BoaBytes(), new BoaScalar[] { new BoaString() }));

		/* expose the java.lang.Math class to Sawzall */

		globalFunctions.addFunction("highbit", new BoaFunction("java.lang.Long.highestOneBit", new BoaInt(), new BoaScalar[] { new BoaInt() }));

		// abs just needs to be overloaded
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaScalar[] { new BoaInt() }));
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// abs is also named fabs in Sawzall
		globalFunctions.addFunction("fabs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// log is named ln in Sawzall
		globalFunctions.addFunction("ln", new BoaFunction("java.lang.Math.log", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// expose the rest of the unary functions
		for (final String s : Arrays.asList("log10", "exp", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "cosh", "sinh", "tanh", "ceil", "floor", "round", "cbrt", "expm1", "log1p", "rint", "signum", "ulp"))
			globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// expose the binary functions
		for (final String s : Arrays.asList("pow", "atan2", "hypot"))
			globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, new BoaFloat(), new BoaScalar[] { new BoaFloat(), new BoaFloat() }));

		// these three have capitals in the name
		globalFunctions.addFunction("ieeeremainder", new BoaFunction("java.lang.Math.IEEEremainder", new BoaFloat(), new BoaScalar[] { new BoaFloat(), new BoaFloat() }));
		globalFunctions.addFunction("todegrees", new BoaFunction("java.lang.Math.toDegrees", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));
		globalFunctions.addFunction("toradians", new BoaFunction("java.lang.Math.toRadians", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// max and min
		for (final String s : Arrays.asList("max", "min"))
			for (final BoaScalar t : Arrays.asList(new BoaInt(), new BoaFloat()))
				globalFunctions.addFunction(s, new BoaFunction("java.lang.Math." + s, t, new BoaScalar[] { t, t }));

		globalFunctions.addFunction("max", new BoaFunction(new BoaTime(), new BoaScalar[] { new BoaTime(), new BoaTime() }, "(${0} > ${1} ? ${0} : ${1})"));
		globalFunctions.addFunction("min", new BoaFunction(new BoaTime(), new BoaScalar[] { new BoaTime(), new BoaTime() }, "(${0} < ${1} ? ${0} : ${1})"));

		globalFunctions.addFunction("max", new BoaFunction(new BoaString(), new BoaScalar[] { new BoaString(), new BoaString() }, "(${0}.compareTo(${1}) > 0 ? ${0} : ${1})"));
		globalFunctions.addFunction("min", new BoaFunction(new BoaString(), new BoaScalar[] { new BoaString(), new BoaString() }, "(${0}.compareTo(${1}) < 0 ? ${0} : ${1})"));
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

		return st;
	}

	public void set(final String id, final BoaType type) {
		this.set(id, type, false);
	}

	public void set(final String id, final BoaType type, final boolean global) {
		if (idmap.containsKey(id))
			throw new RuntimeException(id + " already declared as " + idmap.get(id));

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

	public Class<?> getAggregator(final String name, final BoaScalar type) {
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
				aggregators.add(this.getAggregator(name, (BoaScalar) subType));
		else
			aggregators.add(this.getAggregator(name, (BoaScalar) type));

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
			boa.functions.BoaIntrinsics.class,
			boa.functions.BoaJavaFeaturesIntrinsics.class,
			boa.functions.BoaMetricIntrinsics.class,
			boa.functions.BoaModifierIntrinsics.class,
			boa.functions.BoaCasts.class,
			boa.functions.BoaEncodingIntrinsics.class,
			boa.functions.BoaFileIntrinsics.class,
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
			boa.aggregators.IntHistogramAggregator.class,
			boa.aggregators.IntMeanAggregator.class,
			boa.aggregators.IntQuantileAggregator.class,
			boa.aggregators.IntSumAggregator.class,
			boa.aggregators.KurtosisAggregator.class,
			boa.aggregators.LogAggregator.class,
			boa.aggregators.MaximumAggregator.class,
			boa.aggregators.MedianAggregator.class,
			boa.aggregators.MinimumAggregator.class,
			boa.aggregators.MrcounterAggregator.class,
			boa.aggregators.SetAggregator.class,
			boa.aggregators.SkewnessAggregator.class,
			boa.aggregators.StatisticsAggregator.class,
			boa.aggregators.StDevAggregator.class,
			boa.aggregators.TextAggregator.class,
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

	public boolean hasFunction(final String id) {
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

	public void setNeedsBoxing(final boolean needsBoxing) {
		this.needsBoxing = needsBoxing;
	}

	public boolean getNeedsBoxing() {
		return this.needsBoxing;
	}

	public void setIsBeforeVisitor(final boolean isBeforeVisitor) {
		this.isBeforeVisitor = isBeforeVisitor;
	}

	public boolean getIsBeforeVisitor() {
		return this.isBeforeVisitor;
	}

	@Override
	public String toString() {
		final List<String> r = new ArrayList<String>();

		for (final Entry<String, BoaType> entry : this.locals.entrySet())
			r.add(entry.getKey() + ":" + entry.getValue());

		return r.toString();
	}
}
