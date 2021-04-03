/*
 * Copyright 2017, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
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
import boa.functions.FunctionSpec;
import boa.types.*;
import boa.types.ml.*;
import boa.types.proto.*;
import boa.types.proto.enums.*;
import boa.compiler.ast.Operand;
import boa.compiler.ast.statements.VisitStatement;

/**
 * @author anthonyu
 * @author rdyer
 * @author rramu
 * @author marafat
 */
public class SymbolTable {
	private static HashMap<String, Class<?>> aggregators;
	private static final Map<Class<?>, BoaType> protomap;
	private static Map<String, BoaType> types;
	private static final Map<String, BoaType> globals;
	private static FunctionTrie globalFunctions;

	private FunctionTrie functions;
	private Map<String, BoaType> locals;

	private String id;
	private Operand operand;
	private Stack<BoaType> operandType = new Stack<BoaType>();
	private boolean needsBoxing;
	private Stack<Boolean> isVisitor = new Stack<Boolean>();
	private Stack<VisitStatement> lastVisit = new Stack<VisitStatement>();
	private boolean isTraverse = false;
	private boolean shadowing = false;
	private boolean isLhs = false;

	private final static BoaProtoTuple[] dslTupleTypes = {
		new ASTRootProtoTuple(),
		new AttachmentProtoTuple(),
		new CFGProtoTuple(),
		new CFGNodeProtoTuple(),
		new CFGEdgeProtoTuple(),
		new CDGProtoTuple(),
		new CDGNodeProtoTuple(),
		new CDGEdgeProtoTuple(),
		new DDGProtoTuple(),
		new DDGNodeProtoTuple(),
		new DDGEdgeProtoTuple(),
		new PDGProtoTuple(),
		new PDGNodeProtoTuple(),
		new PDGEdgeProtoTuple(),
		new DTreeProtoTuple(),
		new PDTreeProtoTuple(),
		new TreeNodeProtoTuple(),
		new CFGSlicerProtoTuple(),
		new PDGSlicerProtoTuple(),
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
	private final static BoaProtoMap[] dslMapTypes = {
		new NodeTypeProtoMap(),
		new EdgeLabelProtoMap(),
		new EdgeTypeProtoMap(),
		new ChangeKindProtoMap(),
		new CommentKindProtoMap(),
		new ExpressionKindProtoMap(),
		new FileKindProtoMap(),
		new ForgeKindProtoMap(),
		new IssueStatusProtoMap(),
		new IssuePriorityProtoMap(),
		new IssueLabelProtoMap(),
		new ModifierKindProtoMap(),
		new RepositoryKindProtoMap(),
		new StatementKindProtoMap(),
		new TraversalKindProtoMap(),
		new TraversalDirectionProtoMap(),
		new TypeKindProtoMap(),
		new VisibilityProtoMap(),
	};

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
		types = new HashMap<String, BoaType>();
		resetTypeMap();

		globalFunctions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		// FIXME rdyer - def(protolist[i]) should generate "i < protolist.size()"
		globalFunctions.addFunction("def", new BoaFunction(new BoaBool(), new BoaType[] { new BoaAny() }, "(${0} != null)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaProtoList(new BoaAny()) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaArray(new BoaAny()) }, "((long)${0}.length)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "((long)${0}.keySet().size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaQueue(new BoaTypeVar("V")) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "((long)${0}.length())"));

		// traversal functions
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaTypeVar("K"), new BoaType[] { new CFGNodeProtoTuple(), new BoaTraversal(new BoaTypeVar("K"))},"${1}.getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaAny(), new BoaType[] { new CFGNodeProtoTuple()},"getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaTypeVar("K"), new BoaType[] { new CDGNodeProtoTuple(), new BoaTraversal(new BoaTypeVar("K"))},"${1}.getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaAny(), new BoaType[] { new CDGNodeProtoTuple()},"getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaTypeVar("K"), new BoaType[] { new DDGNodeProtoTuple(), new BoaTraversal(new BoaTypeVar("K"))},"${1}.getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaAny(), new BoaType[] { new DDGNodeProtoTuple()},"getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaTypeVar("K"), new BoaType[] { new PDGNodeProtoTuple(), new BoaTraversal(new BoaTypeVar("K"))},"${1}.getValue(${0})"));
		globalFunctions.addFunction("getvalue", new BoaFunction(new BoaAny(), new BoaType[] { new PDGNodeProtoTuple()},"getValue(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaTraversal()},"${0}.clear()"));

		// graph functions
		globalFunctions.addFunction("getoutedge", new BoaFunction(new CFGEdgeProtoTuple(), new BoaType[] { new CFGNodeProtoTuple(), new CFGNodeProtoTuple() }, "${0}.getOutEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getinedge", new BoaFunction(new CFGEdgeProtoTuple(), new BoaType[] { new CFGNodeProtoTuple(), new CFGNodeProtoTuple() }, "${0}.getInEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getoutedge", new BoaFunction(new CDGEdgeProtoTuple(), new BoaType[] { new CDGNodeProtoTuple(), new CDGNodeProtoTuple() }, "${0}.getOutEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getinedge", new BoaFunction(new CDGEdgeProtoTuple(), new BoaType[] { new CDGNodeProtoTuple(), new CDGNodeProtoTuple() }, "${0}.getInEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getoutedge", new BoaFunction(new DDGEdgeProtoTuple(), new BoaType[] { new DDGNodeProtoTuple(), new DDGNodeProtoTuple() }, "${0}.getOutEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getinedge", new BoaFunction(new DDGEdgeProtoTuple(), new BoaType[] { new DDGNodeProtoTuple(), new DDGNodeProtoTuple() }, "${0}.getInEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getoutedge", new BoaFunction(new PDGEdgeProtoTuple(), new BoaType[] { new PDGNodeProtoTuple(), new PDGNodeProtoTuple() }, "${0}.getOutEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("getinedge", new BoaFunction(new PDGEdgeProtoTuple(), new BoaType[] { new PDGNodeProtoTuple(), new PDGNodeProtoTuple() }, "${0}.getInEdge(${1}).newBuilder().build()"));
		globalFunctions.addFunction("normalize", new BoaFunction(new PDGProtoTuple(), new BoaType[] { new PDGProtoTuple() }, "${0}.normalize()"));
		globalFunctions.addFunction("gettotalnodes", new BoaFunction(new BoaInt(), new BoaType[] { new PDGProtoTuple() }, "${0}.getTotalNodes()"));
		globalFunctions.addFunction("gettotalcontrolnodes", new BoaFunction(new BoaInt(), new BoaType[] { new PDGProtoTuple() }, "${0}.getTotalControlNodes()"));
		globalFunctions.addFunction("gettotaledges", new BoaFunction(new BoaInt(), new BoaType[] { new PDGProtoTuple() }, "${0}.getTotalEdges()"));
		globalFunctions.addFunction("gettotalnodes", new BoaFunction(new BoaInt(), new BoaType[] { new PDGSlicerProtoTuple() }, "${0}.getTotalNodes()"));
		globalFunctions.addFunction("gettotalcontrolnodes", new BoaFunction(new BoaInt(), new BoaType[] { new PDGSlicerProtoTuple() }, "${0}.getTotalControlNodes()"));
		globalFunctions.addFunction("gettotaledges", new BoaFunction(new BoaInt(), new BoaType[] { new PDGSlicerProtoTuple() }, "${0}.getTotalEdges()"));

		// map functions
		globalFunctions.addFunction("haskey", new BoaFunction(new BoaBool(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.containsKey(${1})"));
		globalFunctions.addFunction("keys", new BoaFunction(new BoaArray(new BoaTypeVar("K")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.keySet().toArray(new ${K}[0]))"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.values().toArray(new ${V}[0]))"));
		globalFunctions.addFunction("lookup", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K"), new BoaTypeVar("V") }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.clear()"));

		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()), new BoaInt() }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\", ${1})"));
		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()) }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\")"));

		// clone functions
		globalFunctions.addFunction("clone", new BoaFunction(new BoaMap(new BoaTypeVar("K"), new BoaTypeVar("V")), new BoaType[] {new BoaMap(new BoaTypeVar("K"), new BoaTypeVar("V"))},"(java.util.HashMap)${0}.clone()"));
		globalFunctions.addFunction("clone", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] {new BoaSet(new BoaTypeVar("V"))},"(java.util.HashSet)${0}.clone()"));
		globalFunctions.addFunction("clone", new BoaFunction(new BoaString(), new BoaType[] {new BoaString()},"new String(${0})"));

		// visitors
		for (final BoaType t : dslTupleTypes) {
			globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { t, new BoaVisitor() }, "${1}.visit(${0})"));
			globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { t }, "visit(${0})"));
			globalFunctions.addFunction("ast_len", new BoaFunction(new BoaInt(), new BoaType[] { t }, "boa.functions.BoaAstIntrinsics.lenVisitor.getCount(${0})"));
		}
		globalFunctions.addFunction("_cur_visitor", new BoaFunction(new BoaVisitor(), new BoaType[] { }, "this"));

		// traversal
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaAny(), new BoaType[] { new CFGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal()}, "${3}.traverse(${0},${1},${2})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaBool(), new BoaType[] { new CFGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal(), new BoaFixP() }, "${3}.traverse(${0},${1},${2},${4})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaAny(), new BoaType[] { new CDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal()}, "${3}.traverse(${0},${1},${2})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaBool(), new BoaType[] { new CDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal(), new BoaFixP() }, "${3}.traverse(${0},${1},${2},${4})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaAny(), new BoaType[] { new DDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal()}, "${3}.traverse(${0},${1},${2})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaBool(), new BoaType[] { new DDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal(), new BoaFixP() }, "${3}.traverse(${0},${1},${2},${4})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaAny(), new BoaType[] { new PDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal()}, "${3}.traverse(${0},${1},${2})"));
		globalFunctions.addFunction("traverse", new BoaFunction(new BoaBool(), new BoaType[] { new PDGProtoTuple(), new TraversalDirectionProtoMap(), new TraversalKindProtoMap(), new BoaTraversal(), new BoaFixP() }, "${3}.traverse(${0},${1},${2},${4})"));


		// stack functions
		globalFunctions.addFunction("push", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.push(${1})"));
		globalFunctions.addFunction("pop", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_pop(${0})"));
		globalFunctions.addFunction("peek", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_peek(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "${0}.clear()"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new ${V}[0]))"));

		// queue functions
		globalFunctions.addFunction("offer", new BoaFunction(new BoaAny(), new BoaType[] { new BoaQueue(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.offer(${1})"));
		globalFunctions.addFunction("poll", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaQueue(new BoaTypeVar("V")) }, "${0}.poll()"));
		globalFunctions.addFunction("peek", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaQueue(new BoaTypeVar("V")) }, "${0}.peekFirst()"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaQueue(new BoaTypeVar("V")) }, "${0}.clear()"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaQueue(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new ${V}[0]))"));

		// set functions
		globalFunctions.addFunction("contains", new BoaFunction(new BoaBool(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.contains(${1})"));
		globalFunctions.addFunction("containsall", new BoaFunction(new BoaBool(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V"))}, "${0}.containsAll(${1})"));
		globalFunctions.addFunction("add", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.add(${1})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "${0}.clear()"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new ${V}[0]))"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaSet(new BoaString())), new BoaType[] { new BoaSet(new BoaSet(new BoaString())) }, "boa.functions.BoaIntrinsics.basic_array(${0}.toArray(new java.util.HashSet[0]))"));

		globalFunctions.addFunction("union", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_union(${0}, ${1})"));
		globalFunctions.addFunction("intersect", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_intersect(${0}, ${1})"));
		globalFunctions.addFunction("difference", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_difference(${0}, ${1})"));
		globalFunctions.addFunction("symdiff", new BoaFunction(new BoaSet(new BoaTypeVar("V")), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaSet(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.set_symdiff(${0}, ${1})"));

		// casts from enums to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoMap() }, "${0}.name()"));

		// arrays to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaArray(new BoaAny()) }, "boa.functions.BoaIntrinsics.arrayToString(${0})"));

		// set to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "${0}.toString()"));

		// map to string
        globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.toString()"));

		// current() function inside visits
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("current", new BoaFunction(t, new BoaType[] { t }, ""));

		// proto to string
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { t }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// FIXME the json library doesnt support lists
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoList(new BoaAny()) }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// string to bool
		globalFunctions.addFunction("bool", new BoaFunction("boa.functions.BoaCasts.stringToBoolean", new BoaBool(), new BoaType[] { new BoaString() }));

		// bool to int
		globalFunctions.addFunction("int", new BoaFunction("boa.functions.BoaCasts.booleanToLong", new BoaInt(), new BoaType[] { new BoaBool() }));
		// float to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaFloat() }, "((long)(${0}))"));
		// time to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaTime() }, "${0}"));
		// string to int
		globalFunctions.addFunction("int", new BoaFunction("java.lang.Long.decode", new BoaInt(), new BoaType[] { new BoaString() }));
		// string to int with param base
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString(), new BoaInt() }, "java.lang.Long.parseLong(${0}, (int)(${1}))"));

		// hashing functions
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "((long)${0}.hashCode())"));
		for (final BoaType t : dslTupleTypes)
			globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { t }, "((long)${0}.hashCode())"));
		for (final BoaType t : dslMapTypes)
			globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { t }, "((long)${0}.hashCode())"));

		// int to float
		globalFunctions.addFunction("float", new BoaFunction(new BoaFloat(), new BoaType[] { new BoaInt() }, "((double)(${0}))"));
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
		for (final String s : Arrays.asList("pow", "atan2", "hypot", "IEEEremainder", "toDegrees", "toRadians"))
			globalFunctions.addFunction(s.toLowerCase(), new BoaFunction("java.lang.Math." + s, new BoaFloat(), new BoaType[] { new BoaFloat(), new BoaFloat() }));

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

	public static void resetTypeMap() {
		types = new HashMap<String, BoaType>();
		types.clear();

		types.put("any", new BoaAny());
		types.put("none", null);
		types.put("bool", new BoaBool());
		types.put("int", new BoaInt());
		types.put("float", new BoaFloat());
		types.put("time", new BoaTime());
		types.put("string", new BoaString());
		types.put("model", new BoaModel());
		types.put("tuple", new BoaTuple());

		types.put("LinearRegression", new BoaLinearRegression());
		types.put("AdaBoostM1", new BoaAdaBoostM1());
		types.put("ZeroR", new BoaZeroR());
		types.put("Vote", new BoaVote());
		types.put("SMO", new BoaSMO());
		types.put("SimpleKMeans", new BoaSimpleKMeans());
		types.put("RandomForest", new BoaRandomForest());
		types.put("AdditiveRegression", new BoaAdditiveRegression());
		types.put("AttributeSelectedClassifier", new BoaAttributeSelectedClassifier());
		types.put("PART", new BoaPART());
		types.put("OneR", new BoaOneR());
		types.put("NaiveBayesMultinomialUpdateable", new BoaNaiveBayesMultinomialUpdateable());
		types.put("NaiveBayes", new BoaNaiveBayes());
		types.put("MultiScheme", new BoaMultiScheme());
		types.put("MultilayerPerceptron", new BoaMultilayerPerceptron());
		types.put("MultiScheme", new BoaMultiScheme());
		types.put("MultiClassClassifier", new BoaMultiClassClassifier());
		types.put("MultilayerPerceptron", new BoaMultilayerPerceptron());
		types.put("Bagging", new BoaBagging());
		types.put("BayesNet", new BoaBayesNet());
		types.put("ClassificationViaRegression", new BoaClassificationViaRegression());
		types.put("LWL", new BoaLWL());
		types.put("LogitBoost", new BoaLogitBoost());
		types.put("LMT", new BoaLMT());
		types.put("LogisticRegression", new BoaLogisticRegression());
		types.put("J48", new BoaJ48());
		types.put("JRip", new BoaJRip());
		types.put("KStar", new BoaKStar());
		types.put("CVParameterSelection", new BoaCVParameterSelection());
		types.put("DecisionStump", new BoaDecisionStump());
		types.put("DecisionTable", new BoaDecisionTable());
		types.put("FilteredClassifier", new BoaFilteredClassifier());
		types.put("GaussianProcesses", new BoaGaussianProcesses());
		types.put("InputMappedClassifier", new BoaInputMappedClassifier());
		types.put("Word2Vec", new BoaWord2Vec());
		types.put("Seq2Vec", new BoaSequence2Vec());

		for (final BoaType t : dslTupleTypes)
			types.put(t.toString(), t);

		for (final BoaType t : dslMapTypes)
			types.put(t.toString(), t);
	}

	public SymbolTable cloneNonLocals() throws IOException {
		final SymbolTable st = new SymbolTable();

		st.functions = new FunctionTrie(this.functions);
		st.locals = new HashMap<String, BoaType>(this.locals);
		st.isVisitor = this.isVisitor;
		st.lastVisit = this.lastVisit;
		st.shadowing = this.shadowing;
		st.isLhs = this.isLhs;

		return st;
	}

	public void set(final String id, final BoaType type) {
		this.set(id, type, false);
	}

	public void set(final String id, final BoaType type, final boolean global) {
		if (types.containsKey(id))
			throw new RuntimeException(id + " already declared as type " + types.get(id));

		if (type instanceof BoaFunction) {
			if (global)
				globalFunctions.addFunction(id, (BoaFunction) type);
			else
				this.functions.addFunction(id, (BoaFunction) type);
		}

		if (global)
			globals.put(id, type);
		else
			this.locals.put(id, type);
	}

	public void removeLocal(final String id) {
		locals.remove(id);
	}

	public boolean hasGlobal(final String id) {
		return globals.containsKey(id);
	}

	public boolean hasLocal(final String id) {
		return this.locals.containsKey(id);
	}

	public BoaType get(final String id) {
		if (types.containsKey(id))
			return types.get(id);

		if (globals.containsKey(id))
			return globals.get(id);

		if (this.locals.containsKey(id))
			return this.locals.get(id);

		throw new RuntimeException("no such identifier " + id);
	}

	public boolean hasType(final String id) {
		return types.containsKey(id);
	}

	public static BoaType getType(final String id) {
		if (types.containsKey(id))
			return types.get(id);

		if (id.equals("traversal"))
			return new BoaTraversal();

		if (id.startsWith("array of "))
			return new BoaArray(getType(id.substring("array of ".length()).trim()));

		if (id.startsWith("stack of "))
			return new BoaStack(getType(id.substring("stack of ".length()).trim()));

		if (id.startsWith("set of "))
			return new BoaSet(getType(id.substring("set of ".length()).trim()));

		if (id.startsWith("queue of "))
			return new BoaQueue(getType(id.substring("queue of ".length()).trim()));

		if (id.startsWith("map"))
			return new BoaMap(getType(id.substring(id.indexOf(" of ") + " of ".length()).trim()),
					getType(id.substring(id.indexOf("[") + 1, id.indexOf("]")).trim()));

		throw new RuntimeException("no such type " + id);
	}

	public void setType(final String id, final BoaType boaType) {
		types.put(id, boaType);
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
		throw new RuntimeException("no such aggregator " + name + " of " + type);
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
			boa.functions.BoaMLIntrinsics.class,
			boa.functions.BoaMetricIntrinsics.class,
			boa.functions.BoaNormalFormIntrinsics.class,
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
			boa.aggregators.PreconditionAggregator.class,
			boa.aggregators.ml.LinearRegressionAggregator.class,
			boa.aggregators.ml.AdaBoostM1Aggregator.class,
			boa.aggregators.ml.ZeroRAggregator.class,
			boa.aggregators.ml.VoteAggregator.class,
			boa.aggregators.ml.SMOAggregator.class,
			boa.aggregators.ml.SimpleKMeansAggregator.class,
			boa.aggregators.ml.RandomForestAggregator.class,
			boa.aggregators.ml.AdditiveRegressionAggregator.class,
			boa.aggregators.ml.AttributeSelectedClassifierAggregator.class,
			boa.aggregators.ml.PARTAggregator.class,
			boa.aggregators.ml.OneRAggregator.class,
			boa.aggregators.ml.NaiveBayesMultinomialUpdateableAggregator.class,
			boa.aggregators.ml.NaiveBayesAggregator.class,
			boa.aggregators.ml.MultiSchemeAggregator.class,
			boa.aggregators.ml.MultiClassClassifierAggregator.class,
			boa.aggregators.ml.MultilayerPerceptronAggregator.class,
			boa.aggregators.ml.BaggingAggregator.class,
			boa.aggregators.ml.BayesNetAggregator.class,
			boa.aggregators.ml.ClassificationViaRegressionAggregator.class,
			boa.aggregators.ml.LWLAggregator.class,
			boa.aggregators.ml.LogitBoostAggregator.class,
			boa.aggregators.ml.LMTAggregator.class,
			boa.aggregators.ml.LogisticRegressionAggregator.class,
			boa.aggregators.ml.J48Aggregator.class,
			boa.aggregators.ml.KStarAggregator.class,
			boa.aggregators.ml.JRipAggregator.class,
			boa.aggregators.ml.CVParameterSelectionAggregator.class,
			boa.aggregators.ml.DecisionStumpAggregator.class,
			boa.aggregators.ml.DecisionTableAggregator.class,
			boa.aggregators.ml.FilteredClassifierAggregator.class,
			boa.aggregators.ml.GaussianProcessesAggregator.class,
			boa.aggregators.ml.InputMappedClassifierAggregator.class,
			boa.aggregators.ml.Word2VectorAggregator.class,
			boa.aggregators.ml.Sequence2VectorAggregator.class
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
		final BoaFunction f = globalFunctions.getFunction(id);
		if (f != null)
			return f;
		return functions.getFunction(id);
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

	public void setIsVisitor(final boolean isVisitor) {
		this.isVisitor.push(isVisitor);
	}

	public void unsetIsVisitor() {
		this.isVisitor.pop();
	}

	public boolean getIsVisitor() {
		return this.isVisitor.peek();
	}

	public void setLastVisit(final VisitStatement visit) {
		this.lastVisit.push(visit);
	}

	public void unsetLastVisit() {
		this.lastVisit.pop();
	}

	public VisitStatement getLastVisit() {
		return this.lastVisit.peek();
	}

	public void setShadowing(final boolean shadowing) {
		this.shadowing = shadowing;
	}

	public boolean getShadowing() {
		return this.shadowing;
	}

	public void setIsLhs(final boolean isLhs) {
		this.isLhs = isLhs;
	}

	public boolean getIsLhs() {
		return this.isLhs;
	}

	@Override
	public String toString() {
		final List<String> r = new ArrayList<String>();

		for (final Entry<String, BoaType> entry : this.locals.entrySet())
			r.add(entry.getKey() + ":" + entry.getValue());

		return r.toString();
	}
	
	public static BoaType getMLAggregatorType(String aggregtorName) {
		for (Entry<String, BoaType> e : types.entrySet()) {
			if (e.getKey().equalsIgnoreCase(aggregtorName)) {
				return e.getValue();
			}
		}
		return null;
	}
}
