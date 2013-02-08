package sizzle.compiler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.scannotation.AnnotationDB;

import sizzle.aggregators.AggregatorSpec;
import sizzle.functions.FunctionSpec;
import sizzle.parser.syntaxtree.Operand;
import sizzle.types.ASTRootProtoTuple;
import sizzle.types.ChangeKindProtoMap;
import sizzle.types.CommentKindProtoMap;
import sizzle.types.DeclarationProtoTuple;
import sizzle.types.ExpressionKindProtoMap;
import sizzle.types.ExpressionProtoTuple;
import sizzle.types.ModifierKindProtoMap;
import sizzle.types.ModifierProtoTuple;
import sizzle.types.BugProtoTuple;
import sizzle.types.BugRepositoryProtoTuple;
import sizzle.types.BugStatusProtoMap;
import sizzle.types.CodeRepositoryProtoTuple;
import sizzle.types.CommentProtoTuple;
import sizzle.types.NamespaceProtoTuple;
import sizzle.types.SizzleStack;
import sizzle.types.SizzleVisitor;
import sizzle.types.StatementKindProtoMap;
import sizzle.types.StatementProtoTuple;
import sizzle.types.TypeKindProtoMap;
import sizzle.types.VariableProtoTuple;
import sizzle.types.ChangedFileProtoTuple;
import sizzle.types.FileKindProtoMap;
import sizzle.types.MethodProtoTuple;
import sizzle.types.PersonProtoTuple;
import sizzle.types.ProjectProtoTuple;
import sizzle.types.RepositoryKindProtoMap;
import sizzle.types.RevisionProtoTuple;
import sizzle.types.SizzleAny;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFingerprint;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleProtoList;
import sizzle.types.SizzleProtoTuple;
import sizzle.types.SizzleScalar;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleTime;
import sizzle.types.SizzleTuple;
import sizzle.types.SizzleType;
import sizzle.types.SizzleName;
import sizzle.types.SizzleVarargs;
import sizzle.types.TypeProtoTuple;
import sizzle.types.VisibilityProtoMap;

public class SymbolTable {
	private static final boolean strictCompatibility = true;

	private static HashMap<String, Class<?>> aggregators;
	private static final Map<Class<?>, SizzleType> protomap;
	private static Map<String, SizzleType> idmap;
	private static final Map<String, SizzleType> globals;

	private final ClassLoader loader;

	private FunctionTrie functions;
	private Map<String, SizzleType> locals;

	private String id;
	private Operand operand;
	private Stack<SizzleType> operandType = new Stack<SizzleType>();
	private boolean needsBoxing;
	private boolean isBeforeVisitor = false;

	static {
		aggregators = new HashMap<String, Class<?>>();

		// this maps the Java types in protocol buffers into Sizzle types
		protomap = new HashMap<Class<?>, SizzleType>();

		protomap.put(int.class, new SizzleInt());
		protomap.put(long.class, new SizzleInt());
		protomap.put(float.class, new SizzleFloat());
		protomap.put(double.class, new SizzleFloat());
		protomap.put(boolean.class, new SizzleBool());
		protomap.put(byte[].class, new SizzleBytes());
		protomap.put(Object.class, new SizzleString());

		// variables with a global scope
		globals = new HashMap<String, SizzleType>();

		globals.put("input", new SizzleBytes());
		globals.put("true", new SizzleBool());
		globals.put("false", new SizzleBool());
		globals.put("PI", new SizzleFloat());
		globals.put("Inf", new SizzleFloat());
		globals.put("inf", new SizzleFloat());
		globals.put("NaN", new SizzleFloat());
		globals.put("nan", new SizzleFloat());

		// this maps scalar Sizzle scalar types names to their classes
		idmap = new HashMap<String, SizzleType>();

		idmap.put("any", new SizzleAny());
		idmap.put("none", null);
		idmap.put("bool", new SizzleBool());
		idmap.put("int", new SizzleInt());
		idmap.put("float", new SizzleFloat());
		idmap.put("time", new SizzleTime());
		idmap.put("fingerprint", new SizzleFingerprint());
		idmap.put("string", new SizzleString());
		idmap.put("bytes", new SizzleBytes());

		idmap.put("ASTRoot", new ASTRootProtoTuple());
		idmap.put("Bug", new BugProtoTuple());
		idmap.put("BugRepository", new BugRepositoryProtoTuple());
		idmap.put("BugStatus", new BugStatusProtoMap());
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
	}

	private SymbolTable() {
		this.loader = Thread.currentThread().getContextClassLoader();
	}

	public SymbolTable(final List<URL> libs) throws IOException {
		this.loader = Thread.currentThread().getContextClassLoader();

		// variables with a local scope
		this.locals = new HashMap<String, SizzleType>();
		this.functions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		// FIXME rdyer - def(protolist[i]) should generate "i < protolist.size()"
		this.setFunction("def", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleAny() }, "${0} != null"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleProtoList(new SizzleScalar()) }, "${0}.size()"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleArray(new SizzleScalar()) }, "${0}.length"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()) }, "${0}.keySet().size()"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleStack(new SizzleScalar()) }, "${0}.size()"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleString() }, "${0}.length()"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleBytes() }, "${0}.length"));
		this.setFunction("haskey", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()),
				new SizzleScalar() }, "${0}.containsKey(${1})"));
		this.setFunction("keys", new SizzleFunction(new SizzleArray(new SizzleScalar()), new SizzleType[] { new SizzleMap(new SizzleScalar(),
				new SizzleScalar()) }, "${0}.keySet().toArray()"));
		this.setFunction("lookup", new SizzleFunction(new SizzleScalar(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()),
				new SizzleScalar(), new SizzleScalar() }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));

		this.setFunction("regex", new SizzleFunction(new SizzleString(), new SizzleType[] { new SizzleName(new SizzleScalar()), new SizzleInt() },
				"sizzle.functions.SizzleSpecialIntrinsics.regex(\"${0}\", ${1})"));
		this.setFunction("regex", new SizzleFunction(new SizzleString(), new SizzleType[] { new SizzleName(new SizzleScalar()) },
				"sizzle.functions.SizzleSpecialIntrinsics.regex(\"${0}\")"));
		// these fingerprints are identity functions
		this.setFunction("fingerprintof", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleInt() }));
		this.setFunction("fingerprintof", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleTime() }));

		this.setFunction("visit", new SizzleFunction(new SizzleAny(), new SizzleType[] { new SizzleScalar(), new SizzleVisitor() }, "${1}.visit(${0})"));
		this.setFunction("visit", new SizzleFunction(new SizzleAny(), new SizzleType[] { new SizzleScalar() }, "visit(${0})"));

		// stack functions
		this.setFunction("push", new SizzleFunction(new SizzleAny(), new SizzleType[] { new SizzleStack(new SizzleScalar()), new SizzleScalar() }, "${0}.push(${1})"));
		this.setFunction("pop", new SizzleFunction(new SizzleAny(), new SizzleType[] { new SizzleStack(new SizzleScalar()) }, "sizzle.functions.BoaIntrinsics.stack_pop(${0})"));
		this.setFunction("peek", new SizzleFunction(new SizzleAny(), new SizzleType[] { new SizzleStack(new SizzleScalar()) }, "sizzle.functions.BoaIntrinsics.stack_peek(${0})"));

		// expose the casts for all possible input types
		this.setFunction(new ProjectProtoTuple().toString(), new SizzleFunction(new ProjectProtoTuple(), new SizzleType[] { new SizzleBytes() }, new ProjectProtoTuple().toJavaType() + ".parseFrom(${0})"));

		// string to bool
		this.setFunction("bool", new SizzleFunction("sizzle.functions.SizzleCasts.stringToBoolean", new SizzleBool(), new SizzleScalar[] { new SizzleString() }));

		// bool to int
		this.setFunction("int", new SizzleFunction("sizzle.functions.SizzleCasts.booleanToLong", new SizzleInt(), new SizzleScalar[] { new SizzleBool() }));
		// float to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleFloat() }, "(long)${0}"));
		// time to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleTime() }));
		// fingerprint to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleFingerprint() }));
		// string to int
		this.setFunction("int", new SizzleFunction("java.lang.Long.decode", new SizzleInt(), new SizzleScalar[] { new SizzleString() }));
		// string to int with param base
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleString(), new SizzleInt() }, "java.lang.Long.parseLong(${0}, (int)${1})"));
		// bytes to int with param encoding format
		this.setFunction("int", new SizzleFunction("sizzle.functions.SizzleCasts.bytesToLong", new SizzleInt(), new SizzleScalar[] { new SizzleBytes(), new SizzleString() }));

		// int to float
		this.setFunction("float", new SizzleFunction(new SizzleFloat(), new SizzleScalar[] { new SizzleInt() }, "(double)${0}"));
		// string to float
		this.setFunction("float", new SizzleFunction("java.lang.Double.parseDouble", new SizzleFloat(), new SizzleScalar[] { new SizzleString() }));

		// int to time
		this.setFunction("time", new SizzleFunction(new SizzleTime(), new SizzleScalar[] { new SizzleInt() }));
		// string to time
		this.setFunction("time", new SizzleFunction("sizzle.functions.SizzleCasts.stringToTime", new SizzleTime(), new SizzleScalar[] { new SizzleString() }));
		// string to time
		this.setFunction("time", new SizzleFunction("sizzle.functions.SizzleCasts.stringToTime", new SizzleTime(), new SizzleScalar[] { new SizzleString(), new SizzleString() }));

		// int to fingerprint
		this.setFunction("fingerprint", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleInt() }));
		// string to fingerprint
		this.setFunction("fingerprint", new SizzleFunction("java.lang.Long.parseLong", new SizzleInt(), new SizzleScalar[] { new SizzleString() }));
		// string to fingerprint with param base
		this.setFunction("fingerprint", new SizzleFunction("java.lang.Long.parseLong", new SizzleInt(), new SizzleScalar[] { new SizzleString(), new SizzleInt() }));
		// bytes to fingerprint
		this.setFunction("fingerprint", new SizzleFunction("sizzle.functions.SizzleCasts.bytesToFingerprint", new SizzleFingerprint(), new SizzleScalar[] { new SizzleBytes() }));

		// bool to string
		this.setFunction("string", new SizzleFunction("java.lang.Boolean.toString", new SizzleString(), new SizzleScalar[] { new SizzleBool() }));
		// int to string
		this.setFunction("string", new SizzleFunction("java.lang.Long.toString", new SizzleString(), new SizzleScalar[] { new SizzleInt() }));
		// int to string with parameter base
		this.setFunction("string", new SizzleFunction("sizzle.functions.SizzleCasts.longToString", new SizzleString(), new SizzleScalar[] { new SizzleInt(), new SizzleInt() }));
		// float to string
		this.setFunction("string", new SizzleFunction("java.lang.Double.toString", new SizzleString(), new SizzleScalar[] { new SizzleFloat() }));
		// time to string
		this.setFunction("string", new SizzleFunction("sizzle.functions.SizzleCasts.timeToString", new SizzleString(), new SizzleScalar[] { new SizzleTime() }));
		// fingerprint to string
		this.setFunction("string", new SizzleFunction("java.lang.Long.toHexString", new SizzleString(), new SizzleScalar[] { new SizzleFingerprint() }));
		// bytes to string
		this.setFunction("string", new SizzleFunction("new java.lang.String", new SizzleString(), new SizzleScalar[] { new SizzleBytes() }));
		// bytes to string
		this.setFunction("string", new SizzleFunction("new java.lang.String", new SizzleString(), new SizzleScalar[] { new SizzleBytes(), new SizzleString() }));

		// int to bytes with param encoding format
		this.setFunction("bytes", new SizzleFunction("sizzle.functions.SizzleCasts.longToBytes", new SizzleInt(), new SizzleScalar[] { new SizzleInt(), new SizzleString() }));
		// fingerprint to bytes
		this.setFunction("bytes", new SizzleFunction("sizzle.functions.SizzleCasts.fingerprintToBytes", new SizzleBytes(), new SizzleScalar[] { new SizzleFingerprint() }));
		// string to bytes
		this.setFunction("bytes", new SizzleFunction("sizzle.functions.SizzleCasts.stringToBytes", new SizzleBytes(), new SizzleScalar[] { new SizzleString() }));

		/* expose the java.lang.Math class to Sawzall */

		this.setFunction("highbit", new SizzleFunction("java.lang.Long.highestOneBit", new SizzleInt(), new SizzleScalar[] { new SizzleInt() }));

		// abs just needs to be overloaded
		this.setFunction("abs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleInt() }));
		this.setFunction("abs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// abs is also named fabs in Sawzall
		this.setFunction("fabs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// log is named ln in Sawzall
		this.setFunction("ln", new SizzleFunction("java.lang.Math.log", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// expose the rest of the unary functions
		for (final String s : Arrays.asList("log10", "exp", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "cosh", "sinh", "tanh", "ceil", "floor", "round"))
			this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// expose the binary functions
		for (final String s : Arrays.asList("pow", "atan2"))
			this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(), new SizzleFloat() }));

		for (final String s : Arrays.asList("max", "min"))
			for (final SizzleScalar t : Arrays.asList(new SizzleInt(), new SizzleFloat()))
				this.setFunction(s, new SizzleFunction("java.lang.Math." + s, t, new SizzleScalar[] { t, t }));

		this.setFunction("max", new SizzleFunction(new SizzleTime(), new SizzleScalar[] { new SizzleTime(), new SizzleTime() }, "(${0} > ${1} ? ${0} : ${1})"));
		this.setFunction("min", new SizzleFunction(new SizzleTime(), new SizzleScalar[] { new SizzleTime(), new SizzleTime() }, "(${0} < ${1} ? ${0} : ${1})"));

		this.setFunction("max", new SizzleFunction(new SizzleString(), new SizzleScalar[] { new SizzleString(), new SizzleString() }, "(${0}.compareTo(${1}) > 0 ? ${0} : ${1})"));
		this.setFunction("min", new SizzleFunction(new SizzleString(), new SizzleScalar[] { new SizzleString(), new SizzleString() }, "(${0}.compareTo(${1}) < 0 ? ${0} : ${1})"));

		// expose whatever is left, assuming we are not aiming for strict
		// compatibility
		if (!strictCompatibility) {
			// random takes no argument

			// these three have capitals in the name
			this.setFunction("ieeeremainder", new SizzleFunction("java.lang.Math.IEEEremainder", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(), new SizzleFloat() }));
			this.setFunction("todegrees", new SizzleFunction("java.lang.Math.toDegrees", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));
			this.setFunction("toradians", new SizzleFunction("java.lang.Math.toRadians", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

			// the unaries
			for (final String s : Arrays.asList("cbrt", "expm1", "log1p", "rint", "signum", "ulp"))
				this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

			// and binaries
			this.setFunction("hypot", new SizzleFunction("java.lang.Math.hypot", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(), new SizzleFloat() }));
		}

		// add in the default tables
		// FIXME: support format strings and files
		this.set("stdout", new SizzleTable(new SizzleString()));
		this.set("stderr", new SizzleTable(new SizzleString()));

		this.importLibs(libs);
	}

	public SymbolTable cloneNonLocals() throws IOException {
		SymbolTable st = new SymbolTable();

		st.functions = this.functions;
		st.locals = new HashMap<String, SizzleType>(this.locals);
		st.isBeforeVisitor = this.isBeforeVisitor;

		return st;
	}

	public void set(final String id, final SizzleType type) {
		this.set(id, type, false);
	}

	public void set(final String id, final SizzleType type, final boolean global) {
		if (idmap.containsKey(id))
			throw new RuntimeException(id + " already declared as " + idmap.get(id));

		if (type instanceof SizzleFunction)
			this.setFunction(id, (SizzleFunction) type);

		if (global)
			globals.put(id, type);
		else
			this.locals.put(id, type);
	}

	public boolean contains(final String id) {
		return globals.containsKey(id) || this.locals.containsKey(id);
	}

	public SizzleType get(final String id) {
		if (idmap.containsKey(id))
			return new SizzleName(idmap.get(id));

		if (globals.containsKey(id))
			return globals.get(id);

		if (this.locals.containsKey(id))
			return this.locals.get(id);

		throw new RuntimeException("no such identifier " + id);
	}

	public boolean hasType(final String id) {
		return idmap.containsKey(id);
	}

	public SizzleType getType(final String id) {
		if (idmap.containsKey(id))
			return idmap.get(id);

		if (id.startsWith("array of "))
			return new SizzleArray(this.getType(id.substring("array of ".length()).trim()));

		if (id.startsWith("map"))
			return new SizzleMap(this.getType(id.substring(id.indexOf(" of ") + " of ".length()).trim()),
					this.getType(id.substring(id.indexOf("[") + 1, id.indexOf("]")).trim()));

		throw new RuntimeException("no such type " + id);
	}

	public void setType(final String id, final SizzleType sizzleType) {
		idmap.put(id, sizzleType);
	}

	private void importAggregator(final Class<?> clazz) {
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

	private void importAggregator(final String c) {
		try {
			this.importAggregator(Class.forName(c, false, this.loader));
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("no such class " + c, e);
		}
	}

	public Class<?> getAggregator(final String name, final SizzleScalar type) {
		if (aggregators.containsKey(name + ":" + type))
			return aggregators.get(name + ":" + type);
		else if (aggregators.containsKey(name))
			return aggregators.get(name);
		else
			throw new RuntimeException("no such aggregator " + name + " of " + type);
	}

	public List<Class<?>> getAggregators(final String name, final SizzleType type) {
		final List<Class<?>> aggregators = new ArrayList<Class<?>>();

		if (type instanceof SizzleTuple)
			for (final SizzleType subType : ((SizzleTuple) type).getTypes())
				aggregators.add(this.getAggregator(name, (SizzleScalar) subType));
		else
			aggregators.add(this.getAggregator(name, (SizzleScalar) type));

		return aggregators;
	}

	private void importFunction(final Method m) {
		final FunctionSpec annotation = m.getAnnotation(FunctionSpec.class);

		if (annotation == null)
			return;

		final String[] formalParameters = annotation.formalParameters();
		final SizzleType[] formalParameterTypes = new SizzleType[formalParameters.length];

		for (int i = 0; i < formalParameters.length; i++) {
			final String id = formalParameters[i];

			// check for varargs
			if (id.endsWith("..."))
				formalParameterTypes[i] = new SizzleVarargs(this.getType(id.substring(0, id.indexOf('.'))));
			else
				formalParameterTypes[i] = this.getType(id);
		}

		for (final String dep : annotation.typeDependencies())
			if (dep.endsWith(".proto"))
				this.importProto(dep);
			else if (dep.endsWith(".avro"))
				this.importAvro(dep);
			else
				throw new RuntimeException("unknown dependency in " + dep);

		this.setFunction(annotation.name(),
				new SizzleFunction(m.getDeclaringClass().getCanonicalName() + '.' + m.getName(), this.getType(annotation.returnType()), formalParameterTypes));
	}

	private void importFunctions(final Class<?> c) {
		for (final Method m : c.getMethods())
			if (m.isAnnotationPresent(FunctionSpec.class))
				this.importFunction(m);
	}

	private void importFunctions(final String c) {
		try {
			this.importFunctions(Class.forName(c));
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("no such class " + c, e);
		}
	}

	private void importLibs(final List<URL> urls) throws IOException {
		// load built-in functions
		final Class<?>[] builtinFuncs = {
			sizzle.functions.BoaAstIntrinsics.class,
			sizzle.functions.BoaIntrinsics.class,
			sizzle.functions.BoaJavaFeaturesIntrinsics.class,
			sizzle.functions.BoaMetricIntrinsics.class,
			sizzle.functions.BoaModifierIntrinsics.class,
			sizzle.functions.SizzleCasts.class,
			sizzle.functions.SizzleEncodingIntrinsics.class,
			sizzle.functions.SizzleFileIntrinsics.class,
			sizzle.functions.SizzleMathIntrinsics.class,
			sizzle.functions.SizzleSortIntrinsics.class,
			sizzle.functions.SizzleSpecialIntrinsics.class,
			sizzle.functions.SizzleStringIntrinsics.class,
			sizzle.functions.SizzleTimeIntrinsics.class
		};
		for (final Class<?> c : builtinFuncs)
			this.importFunctions(c);

		// load built-in aggregators
		final Class<?>[] builtinAggs = {
			sizzle.aggregators.BottomAggregator.class,
			sizzle.aggregators.CollectionAggregator.class,
			sizzle.aggregators.DistinctAggregator.class,
			sizzle.aggregators.FloatHistogramAggregator.class,
			sizzle.aggregators.FloatMeanAggregator.class,
			sizzle.aggregators.FloatQuantileAggregator.class,
			sizzle.aggregators.FloatSumAggregator.class,
			sizzle.aggregators.IntHistogramAggregator.class,
			sizzle.aggregators.IntMeanAggregator.class,
			sizzle.aggregators.IntQuantileAggregator.class,
			sizzle.aggregators.IntSumAggregator.class,
			sizzle.aggregators.LogAggregator.class,
			sizzle.aggregators.MaximumAggregator.class,
			sizzle.aggregators.MinimumAggregator.class,
			sizzle.aggregators.MrcounterAggregator.class,
			sizzle.aggregators.SetAggregator.class,
			sizzle.aggregators.StderrAggregator.class,
			sizzle.aggregators.StdoutAggregator.class,
			sizzle.aggregators.TextAggregator.class,
			sizzle.aggregators.TopAggregator.class,
			sizzle.aggregators.UniqueAggregator.class
		};
		for (final Class<?> c : builtinAggs)
			this.importAggregator(c);

		// also check any libs passed into the compiler
		if (urls.size() > 0) {
			final AnnotationDB db = new AnnotationDB();
			db.setScanMethodAnnotations(true);
			db.setScanClassAnnotations(true);
//			db.setScanPackages(new String[] {"sizzle.aggregators", "sizzle.functions"});

			for (final URL url : urls)
				db.scanArchives(url);
	
			final Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();
	
			for (final String s : annotationIndex.get(AggregatorSpec.class.getCanonicalName()))
				this.importAggregator(s);
	
			for (final String s : annotationIndex.get(FunctionSpec.class.getCanonicalName()))
				this.importFunctions(s);
		}
	}

	void importProto(final String name) {
		final String camelCased = SymbolTable.camelCase(name.substring(0, name.indexOf('.')));

		Class<?> wrapper;
		try {
			wrapper = Class.forName("sizzle.types." + camelCased);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("no such proto " + name);
		}

		for (final Class<?> c : wrapper.getClasses()) {
			final List<SizzleType> members = new ArrayList<SizzleType>();
			final Map<String, Integer> names = new HashMap<String, Integer>();

			int i = 0;
			for (final Field field : c.getDeclaredFields()) {
				if (!field.getName().endsWith("_") || field.getName().startsWith("bitField"))
					continue;

				final String member = SymbolTable.deCamelCase(field.getName().substring(0, field.getName().length() - 1));

				final Class<?> type = field.getType();

				names.put(member, i++);
				members.add(protomap.get(type));
			}

			idmap.put(c.getSimpleName(), new SizzleProtoTuple(members, names));
			// TODO support protocol buffer casts
		}
	}

	private void importAvro(final String dep) {
		throw new RuntimeException("unimplemented");
	}

	public SizzleFunction getFunction(final String id) {
		return this.getFunction(id, new SizzleType[0]);
	}

	public SizzleFunction getFunction(final String id, final List<SizzleType> formalParameters) {
		return this.getFunction(id, formalParameters.toArray(new SizzleType[formalParameters.size()]));
	}

	public SizzleFunction getFunction(final String id, final SizzleType[] formalParameters) {
		return this.functions.getFunction(id, formalParameters);
	}

	public void setFunction(final String id, final SizzleFunction sizzleFunction) {
		this.functions.addFunction(id, sizzleFunction);
	}

	public boolean hasCast(final SizzleType from, final SizzleType to) {
		try {
			this.getFunction(to.toString(), new SizzleType[] { from });

			return true;
		} catch (final RuntimeException e) {
			return false;
		}
	}

	public SizzleFunction getCast(final SizzleType from, final SizzleType to) {
		return this.getFunction(to.toString(), new SizzleType[] { from });
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

	public void setOperandType(final SizzleType operandType) {
		this.operandType.push(operandType);
	}

	public SizzleType getOperandType() {
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

		for (final Entry<String, SizzleType> entry : this.locals.entrySet())
			r.add(entry.getKey() + ":" + entry.getValue());

		return r.toString();
	}

	private static String camelCase(final String string) {
		final StringBuilder camelized = new StringBuilder();

		boolean lower = false;
		for (final char c : string.toCharArray())
			if (c == '_')
				lower = false;
			else if (Character.isDigit(c)) {
				camelized.append(c);
				lower = false;
			} else if (Character.isLetter(c)) {
				if (lower)
					camelized.append(c);
				else
					camelized.append(Character.toUpperCase(c));

				lower = true;
			}

		return camelized.toString();
	}

	private static String deCamelCase(final String string) {
		final StringBuilder decamelized = new StringBuilder();

		for (final char c : string.toCharArray())
			if (Character.isUpperCase(c))
				decamelized.append(Character.toString('_') + Character.toLowerCase(c));
			else
				decamelized.append(c);

		return decamelized.toString();
	}
}
