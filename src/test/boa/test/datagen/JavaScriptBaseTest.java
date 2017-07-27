package boa.test.datagen;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;
import com.googlecode.protobuf.format.JsonFormat;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.junit.Test;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ast.*;

import static org.junit.Assert.assertEquals;

import boa.types.Ast.ASTRoot;
import boa.types.Ast.Declaration;
import boa.types.Diff.ChangedFile;
import boa.datagen.util.FileIO;
import boa.datagen.util.JavaScriptVisitor;
import boa.datagen.util.ProtoMessageVisitor;
import boa.test.compiler.BaseTest;


public class JavaScriptBaseTest extends BaseTest {

	public void nodeTest(String expected, String JS){
		assertEquals(expected, parseJs(JS));
	}

	private String parseJs(String content) {
		CompilerEnvirons cp = new CompilerEnvirons();
		cp.setLanguageVersion(Context.VERSION_ES6);
		final org.mozilla.javascript.Parser parser = new org.mozilla.javascript.Parser(cp);
		AstRoot cu;
		try{
			cu =  parser.parse(content, null, 0);
		}catch(java.lang.IllegalArgumentException ex){
			return "Parse error";
		}catch(org.mozilla.javascript.EvaluatorException ex){
			return "Parse error";
		}
		JavaScriptVisitor visitor = new JavaScriptVisitor(content);
		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		try {
			ast.addNamespaces(visitor.getNamespaces(cu));
		} catch (final UnsupportedOperationException e) {
			return  "Visitor error";
		} catch (final Exception e) {
			return "Visitor error";
		}
		String boaString = ast.build().toString();
		return boaString;
	}

}
