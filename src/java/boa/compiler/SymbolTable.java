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
import boa.functions.FunctionSpec;
import boa.types.*;
import boa.types.proto.*;
import boa.types.proto.enums.*;

import boa.compiler.ast.Operand;

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

		idmap.put("ASTRoot", new ASTRootProtoTuple());
		idmap.put("Attachment", new AttachmentProtoTuple());
		idmap.put("ChangedFile", new ChangedFileProtoTuple());
		idmap.put("ChangeKind", new ChangeKindProtoMap());
		idmap.put("CodeRepository", new CodeRepositoryProtoTuple());
		idmap.put("CommentKind", new CommentKindProtoMap());
		idmap.put("Comment", new CommentProtoTuple());
		idmap.put("CommentsRoot", new CommentsRootProtoTuple());
		idmap.put("Declaration", new DeclarationProtoTuple());
		idmap.put("ExpressionKind", new ExpressionKindProtoMap());
		idmap.put("Expression", new ExpressionProtoTuple());
		idmap.put("FileKind", new FileKindProtoMap());
		idmap.put("ForgeKind", new ForgeKindProtoMap());
		idmap.put("Issue", new IssueProtoTuple());
		idmap.put("IssueComment", new IssueCommentProtoTuple());
		idmap.put("IssueKind", new IssueKindProtoMap());
		idmap.put("IssueRepository", new IssueRepositoryProtoTuple());
		idmap.put("IssuesRoot", new IssuesRootProtoTuple());
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
		idmap.put("CFG", new CFGProtoTuple());
		idmap.put("CFGNode", new CFGNodeProtoTuple());
		idmap.put("CFGEdge", new CFGEdgeProtoTuple());

		globalFunctions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		// FIXME rdyer - def(protolist[i]) should generate "i < protolist.size()"
		globalFunctions.addFunction("def", new BoaFunction(new BoaBool(), new BoaType[] { new BoaAny() }, "(${0} != null)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaProtoList(new BoaScalar()) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaArray(new BoaScalar()) }, "((long)${0}.length)"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaMap(new BoaScalar(), new BoaScalar()) }, "((long)${0}.keySet().size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaStack(new BoaScalar()) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaSet(new BoaScalar()) }, "((long)${0}.size())"));
		globalFunctions.addFunction("len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaString() }, "((long)${0}.length())"));

		// map functions
		globalFunctions.addFunction("haskey", new BoaFunction(new BoaBool(), new BoaType[] { new BoaMap(new BoaScalar(), new BoaScalar()), new BoaScalar() }, "${0}.containsKey(${1})"));
		globalFunctions.addFunction("keys", new BoaFunction(new BoaArray(new BoaTypeVar("K")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.keySet().toArray(new ${K}[0]))"));
		globalFunctions.addFunction("values", new BoaFunction(new BoaArray(new BoaTypeVar("V")), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "boa.functions.BoaIntrinsics.basic_array(${0}.values().toArray(new ${V}[0]))"));
		globalFunctions.addFunction("lookup", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K"), new BoaTypeVar("V") }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")), new BoaTypeVar("K") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaMap(new BoaTypeVar("V"), new BoaTypeVar("K")) }, "${0}.clear()"));

		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()), new BoaInt() }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\", ${1})"));
		globalFunctions.addFunction("regex", new BoaFunction(new BoaString(), new BoaType[] { new BoaName(new BoaScalar()) }, "boa.functions.BoaSpecialIntrinsics.regex(\"${0}\")"));

		// visitors
		globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { new BoaScalar(), new BoaVisitor() }, "${1}.visit(${0})"));
		globalFunctions.addFunction("visit", new BoaFunction(new BoaAny(), new BoaType[] { new BoaScalar() }, "visit(${0})"));
		globalFunctions.addFunction("_cur_visitor", new BoaFunction(new BoaVisitor(), new BoaType[] { }, "this"));
		globalFunctions.addFunction("ast_len", new BoaFunction(new BoaInt(), new BoaType[] { new BoaAny() }, "boa.functions.BoaAstIntrinsics.lenVisitor.getCount(${0})"));

		// stack functions
		globalFunctions.addFunction("push", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.push(${1})"));
		globalFunctions.addFunction("pop", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_pop(${0})"));
		globalFunctions.addFunction("peek", new BoaFunction(new BoaTypeVar("V"), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "boa.functions.BoaIntrinsics.stack_peek(${0})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaStack(new BoaTypeVar("V")) }, "${0}.clear()"));

		// set functions
		globalFunctions.addFunction("contains", new BoaFunction(new BoaBool(), new BoaType[] { new BoaSet(new BoaScalar()), new BoaScalar() }, "${0}.contains(${1})"));
		globalFunctions.addFunction("add", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.add(${1})"));
		globalFunctions.addFunction("remove", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")), new BoaTypeVar("V") }, "${0}.remove(${1})"));
		globalFunctions.addFunction("clear", new BoaFunction(new BoaAny(), new BoaType[] { new BoaSet(new BoaTypeVar("V")) }, "${0}.clear()"));

		// casts from enums to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoMap() }, "${0}.name()"));

		// arrays to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaArray(new BoaScalar()) }, "boa.functions.BoaIntrinsics.arrayToString(${0})"));

		// FIXME
		globalFunctions.addFunction("current", new BoaFunction(new ASTRootProtoTuple(), new BoaType[] { new ASTRootProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new AttachmentProtoTuple(), new BoaType[] { new AttachmentProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new ChangedFileProtoTuple(), new BoaType[] { new ChangedFileProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new CodeRepositoryProtoTuple(), new BoaType[] { new CodeRepositoryProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new CommentProtoTuple(), new BoaType[] { new CommentProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new CommentsRootProtoTuple(), new BoaType[] { new CommentsRootProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new DeclarationProtoTuple(), new BoaType[] { new DeclarationProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new ExpressionProtoTuple(), new BoaType[] { new ExpressionProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new IssueProtoTuple(), new BoaType[] { new IssueProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new IssueCommentProtoTuple(), new BoaType[] { new IssueCommentProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new IssueRepositoryProtoTuple(), new BoaType[] { new IssueRepositoryProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new IssuesRootProtoTuple(), new BoaType[] { new IssuesRootProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new MethodProtoTuple(), new BoaType[] { new MethodProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new ModifierProtoTuple(), new BoaType[] { new ModifierProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new NamespaceProtoTuple(), new BoaType[] { new NamespaceProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new PersonProtoTuple(), new BoaType[] { new PersonProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new ProjectProtoTuple(), new BoaType[] { new ProjectProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new RevisionProtoTuple(), new BoaType[] { new RevisionProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new StatementProtoTuple(), new BoaType[] { new StatementProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new TypeProtoTuple(), new BoaType[] { new TypeProtoTuple() }, ""));
		globalFunctions.addFunction("current", new BoaFunction(new VariableProtoTuple(), new BoaType[] { new VariableProtoTuple() }, ""));

		// proto to string
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ASTRootProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new AttachmentProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ChangedFileProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new CodeRepositoryProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new CommentProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new CommentsRootProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new DeclarationProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ExpressionProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new IssueProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new IssueCommentProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new IssueRepositoryProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new IssuesRootProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new MethodProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ModifierProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new NamespaceProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new PersonProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ProjectProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new RevisionProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new StatementProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new TypeProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new VariableProtoTuple() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// FIXME the json library doesnt support enums
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ChangeKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new CommentKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ExpressionKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new FileKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ForgeKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new IssueKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new ModifierKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new RepositoryKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new StatementKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new TypeKindProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new VisibilityProtoMap() }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// FIXME the json library doesnt support lists
		//globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaProtoList(new BoaScalar()) }, "com.googlecode.protobuf.format.JsonFormat.printToString(${0})"));

		// string to bool
		globalFunctions.addFunction("bool", new BoaFunction("boa.functions.BoaCasts.stringToBoolean", new BoaBool(), new BoaScalar[] { new BoaString() }));

		// bool to int
		globalFunctions.addFunction("int", new BoaFunction("boa.functions.BoaCasts.booleanToLong", new BoaInt(), new BoaScalar[] { new BoaBool() }));
		// float to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaFloat() }, "((long)${0})"));
		// time to int
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaTime() }, "${0}"));
		// string to int
		globalFunctions.addFunction("int", new BoaFunction("java.lang.Long.decode", new BoaInt(), new BoaScalar[] { new BoaString() }));
		// string to int with param base
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaString(), new BoaInt() }, "java.lang.Long.parseLong(${0}, (int)${1})"));

		// hashing functions
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaScalar[] { new BoaString() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ASTRootProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new AttachmentProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ChangedFileProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ChangeKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new CodeRepositoryProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new CommentKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new CommentProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new CommentsRootProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new DeclarationProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ExpressionKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ExpressionProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new FileKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ForgeKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new IssueProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new IssueCommentProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new IssueKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new IssueRepositoryProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new IssuesRootProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new MethodProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ModifierKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ModifierProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new NamespaceProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new PersonProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new ProjectProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new RepositoryKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new RevisionProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new StatementKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new StatementProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new TypeKindProtoMap() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new TypeProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new VariableProtoTuple() }, "((long)${0}.hashCode())"));
		globalFunctions.addFunction("hash", new BoaFunction(new BoaInt(), new BoaType[] { new VisibilityProtoMap() }, "((long)${0}.hashCode())"));

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

		// bool to string
		globalFunctions.addFunction("string", new BoaFunction("java.lang.Boolean.toString", new BoaString(), new BoaScalar[] { new BoaBool() }));
		// int to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.longToString", new BoaString(), new BoaScalar[] { new BoaInt() }));
		// int to string with parameter base
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.longToString", new BoaString(), new BoaScalar[] { new BoaInt(), new BoaInt() }));
		// float to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.doubleToString", new BoaString(), new BoaScalar[] { new BoaFloat() }));
		// time to string
		globalFunctions.addFunction("string", new BoaFunction("boa.functions.BoaCasts.timeToString", new BoaString(), new BoaScalar[] { new BoaTime() }));

		// self casts
		globalFunctions.addFunction("bool", new BoaFunction(new BoaBool(), new BoaType[] { new BoaBool() }, "${0}"));
		globalFunctions.addFunction("int", new BoaFunction(new BoaInt(), new BoaType[] { new BoaInt() }, "${0}"));
		globalFunctions.addFunction("float", new BoaFunction(new BoaFloat(), new BoaType[] { new BoaFloat() }, "${0}"));
		globalFunctions.addFunction("time", new BoaFunction(new BoaTime(), new BoaType[] { new BoaTime() }, "${0}"));
		globalFunctions.addFunction("string", new BoaFunction(new BoaString(), new BoaType[] { new BoaString() }, "${0}"));

		/* expose the java.lang.Math class to Sawzall */

		globalFunctions.addFunction("highbit", new BoaFunction("java.lang.Long.highestOneBit", new BoaInt(), new BoaScalar[] { new BoaInt() }));

		// abs just needs to be overloaded
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaScalar[] { new BoaInt() }));
		globalFunctions.addFunction("abs", new BoaFunction("java.lang.Math.abs", new BoaFloat(), new BoaScalar[] { new BoaFloat() }));

		// expose the rest of the unary functions
		for (final String s : Arrays.asList("log", "log10", "exp", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "cosh", "sinh", "tanh", "ceil", "floor", "round", "cbrt", "expm1", "log1p", "rint", "signum", "ulp"))
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
