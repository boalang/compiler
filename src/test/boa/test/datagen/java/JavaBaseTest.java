/*
 * Copyright 2016-2022, Hridesh Rajan, Robert Dyer, Huaiyao Ma,
 *                 Iowa State University of Science and Technology,
 *                 Bowling Green State University
 *                 and University of Nebraska Board of Regents
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
package boa.test.datagen.java;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

import boa.datagen.DefaultProperties;
import boa.datagen.util.FileIO;
import boa.datagen.util.JavaVisitor;
import boa.test.compiler.BaseTest;
import boa.test.datagen.ProtoMessageVisitor;
import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Diff.ChangedFile;


/*
 * @author rdyer
 * @author huaiyao
 */
public class JavaBaseTest extends BaseTest {
	protected static int astLevel = DefaultProperties.DEFAULT_JAVA_ASTLEVEL;
	protected static String javaVersion = JavaCore.VERSION_15;
	protected static JavaVisitor visitor = new JavaVisitor("");

	protected static void setJavaVersion() {
		astLevel = DefaultProperties.DEFAULT_JAVA_ASTLEVEL;
		javaVersion = JavaCore.VERSION_15;
		visitor = new JavaVisitor("");
	}

	protected static void dumpJavaWrapped(final String content) {
		setJavaVersion();
		dumpJava(getWrapped(content));
	}

	protected static void dumpJava(final String content) {
		setJavaVersion();
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(content.toCharArray());

		final Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(javaVersion, options);
		parser.setCompilerOptions(options);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		try {
			final UglyMathCommentsExtractor cex = new UglyMathCommentsExtractor(cu, content);
			final ASTDumper dumper = new ASTDumper(cex);
			dumper.dump(cu);
			cex.close();
		} catch (final Exception e) {}
	}

	public static String parseJava(final String content) {
		final File f = new File(new File(System.getProperty("java.io.tmpdir")), UUID.randomUUID().toString());
		FileIO.writeFileContents(f, content);
		final String res = parseJavaFile(f.getPath());
		try {
			FileIO.delete(f);
		} catch (final Exception e) {}
		return res;
	}

	public static String parseJavaFile(final String path) {
		setJavaVersion();
		final StringBuilder sb = new StringBuilder();
		final FileASTRequestor r = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				final ASTRoot.Builder ast = ASTRoot.newBuilder();
				try {
					ast.addNamespaces(visitor.getNamespaces(cu));
				} catch (final Exception e) {
					System.err.println(e);
					e.printStackTrace();
				}

				sb.append(JsonFormat.printToString(ast.build()));
			}
		};
		final Map<String, String> fileContents = new HashMap<String, String>();
		@SuppressWarnings("rawtypes")
		final Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, javaVersion);
		options.put(JavaCore.COMPILER_SOURCE, javaVersion);
		final ASTParser parser = ASTParser.newParser(astLevel);
		parser.setCompilerOptions(options);
		parser.setEnvironment(new String[0], new String[]{}, new String[]{}, true);
		parser.setResolveBindings(true);
		parser.createASTs(new String[] { path }, null, new String[0], r, null);

		return FileIO.normalizeEOL(sb.toString());
	}

	protected static String getWrapped(final String content) {
		String s = "class t {\n   void m() {\n      " + content.replaceAll("\n", "\n      ").trim();
		if (content.indexOf(";") == -1 && !s.endsWith("}"))
			s += ";";
		if (!s.endsWith("\n"))
			s += "\n";
		s += "   }\n}";
		return s;
	}

	protected static String parseWrapped(final String content) {
		setJavaVersion();
		return parseJava(getWrapped(content));
	}

	protected static String getWrappedResult(final String expected) {
		return "{\n"
				+ "   \"namespaces\": [\n"
				+ "      {\n"
				+ "         \"name\": \"\",\n"
				+ "         \"declarations\": [\n"
				+ "            {\n"
				+ "               \"name\": \"t\",\n"
				+ "               \"kind\": \"CLASS\",\n"
				+ "               \"methods\": [\n"
				+ "                  {\n"
				+ "                     \"name\": \"m\",\n"
				+ "                     \"return_type\": {\n"
				+ "                        \"name\": \"void\",\n"
				+ "                        \"kind\": \"PRIMITIVE\"\n"
				+ "                     },\n"
				+ "                     \"statements\": [\n"
				+ "                        {\n"
				+ "                           \"kind\": \"BLOCK\",\n"
				+ "                           \"statements\": [\n"
				+ "                              " + expected.replaceAll("\n", "\n                              ") + "\n"
				+ "                           ]\n"
				+ "                        }\n"
				+ "                     ]\n"
				+ "                  }\n"
				+ "               ],\n"
				+ "               \"fully_qualified_name\": \"t\"\n"
				+ "            }\n"
				+ "         ]\n"
				+ "      }\n"
				+ "   ]\n"
				+ "}";
	}

	protected static Declaration getDeclaration(final SequenceFile.Reader ar, final ChangedFile cf, final int nodeId, final HashMap<Integer, Declaration> declarations) {
		long astpos = cf.getKey();
		if (cf.getAst() && astpos > -1) {
			try {
				ar.seek(astpos);
				final Writable astkey = new LongWritable();
				final BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				final byte[] bytes = val.getBytes();
				final ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				final ProtoMessageVisitor v = new ProtoMessageVisitor() {
					private boolean found = false;

					@Override
					public boolean preVisit(final Message message) {
						if (found)
							return false;
						if (message instanceof Declaration) {
							final Declaration temp = (Declaration) message;
							Declaration type = declarations.get(temp.getKey());
							if (type == null) {
								type = Declaration.newBuilder(temp).build();
								declarations.put(type.getKey(), type);
							}
							if (type.getKey() == nodeId) {
								found = true;
								return false;
							}
						}
						return true;
					};
				};
				v.visit(root);
			} catch (final IOException e) {}
		}
		return declarations.get(nodeId);
	}

	protected static Message getMessage(final SequenceFile.Reader ar, final ChangedFile cf, final int nodeId) {
		long astpos = cf.getKey();
		if (cf.getAst() && astpos > -1) {
			try {
				ar.seek(astpos);
				final Writable astkey = new LongWritable();
				final BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				final byte[] bytes = val.getBytes();
				final ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				return getMessage(root, nodeId);
			} catch (final IOException e) {}
		}
		return null;
	}

	protected static Message getMessage(final Message root, final int nodeId) {
		final Message[] m = new Message[1];
		final ProtoMessageVisitor v = new ProtoMessageVisitor() {
			private boolean found = false;

			@Override
			public boolean preVisit(final Message message) {
				if (found)
					return false;
				final Object v = getFieldValue(message, "key");
				if (v != null && (Integer) v == nodeId) {
					m[0] = message;
					found = true;
					return false;
				}
				return true;
			};
		};
		v.visit(root);
		return m[0];
	}

	protected static Object getFieldValue(final Message message, final String name) {
		for (final Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<FieldDescriptor, Object> field = iter.next();
			if (field.getKey().getName().equals(name)) {
				return field.getValue();
			}
		}
		return null;
	}

	protected static HashMap<Integer, HashMap<Integer, Declaration>> collectDeclarations(final SequenceFile.Reader ar, List<ChangedFile> snapshot) throws IOException {
		final HashMap<Integer, HashMap<Integer, Declaration>> fileNodeDeclaration = new HashMap<Integer, HashMap<Integer, Declaration>>();
		for (int fileIndex = 0; fileIndex < snapshot.size(); fileIndex++) {
			final ChangedFile cf = snapshot.get(fileIndex);
			long astpos = cf.getKey();
			if (!cf.getAst())
				continue;
			if (astpos > -1) {
				ar.seek(astpos);
				final Writable astkey = new LongWritable();
				final BytesWritable val = new BytesWritable();
				ar.next(astkey, val);
				final byte[] bytes = val.getBytes();
				final ASTRoot root = ASTRoot.parseFrom(CodedInputStream.newInstance(bytes, 0, val.getLength()));
				final HashMap<Integer, Declaration> nodeDeclaration = collectDeclarations(root);
				fileNodeDeclaration.put(fileIndex, nodeDeclaration);
			}
		}
		return fileNodeDeclaration;
	}

	protected static HashMap<Integer, Declaration> collectDeclarations(final Message message) {
		final HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
		if (message instanceof Declaration) {
			nodeDeclaration.put(((Declaration) message).getKey(), (Declaration) message);
		}
		for (final Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter.hasNext();) {
			final Map.Entry<FieldDescriptor, Object> field = iter.next();
			nodeDeclaration.putAll(collectDeclarations(field.getKey(), field.getValue()));
		}
		return nodeDeclaration;
	}

	protected static HashMap<Integer, Declaration> collectDeclarations(final FieldDescriptor field, final Object value) {
		final HashMap<Integer, Declaration> nodeDeclaration = new HashMap<Integer, Declaration>();
		if (field.isRepeated()) {
			// Repeated field. Print each element.
			for (final Iterator<?> iter = ((List<?>) value).iterator(); iter.hasNext();)
				if (field.getType() == Type.MESSAGE)
					nodeDeclaration.putAll(collectDeclarations((Message) iter.next()));
		}
		return nodeDeclaration;
	}
}
