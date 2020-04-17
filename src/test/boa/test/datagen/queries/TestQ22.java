package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ22 extends QueryTest {
	
	@Test
	public void testQ22() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int stringField = 0;
		int stringFieldMax = 0;
		int stringFieldMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaStringFieldVisitor visitor = new JavaStringFieldVisitor();
			visitPath(path, visitor);
			int methodCount = visitor.stringField;
			stringField += methodCount;
			if (stringFieldMax < visitor.maxStringField)
				stringFieldMax = visitor.maxStringField;
			if (stringFieldMin > visitor.minStringField)
				stringFieldMin = visitor.minStringField;
			files += visitor.classes;
		}
		double mean = (double) stringField / files;
		expected += "StringFieldMax[] = " + projectId + ", " + (double) stringFieldMax 
				+ "\nStringFieldMean[] = " + mean
				+ "\nStringFieldMin[] = " + projectId + ", " + (double) stringFieldMin 
				+ "\nStringFieldTotal[] = " + stringField + "\n";
		queryTest("test/known-good/q22.boa", expected);
	}
	
	public class JavaStringFieldVisitor extends ASTVisitor {
		public int stringField = 0;
		public int stringField2 = 0;
		public int classes = 0;
		public int maxStringField = 0;
		public int minStringField = Integer.MAX_VALUE;
		public int field = 0;
		private Stack<Integer> stringFieldStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface())
				return false;
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration){
				stringFieldStack.push(stringField2);
				stringField2 = 0;
			}
			return true;
		}
		
		@Override
		public void endVisit(AnonymousClassDeclaration node) {
			classes++;
			if (maxStringField < stringField2)
				maxStringField = stringField2;
			if (minStringField > stringField2)
				minStringField = stringField2;
			stringField2 = stringFieldStack.pop();
		}
		
		@Override
		public void endVisit(TypeDeclaration node) {
			if (node.isInterface())
				return;
			classes++;
			if (maxStringField < stringField2)
				maxStringField = stringField2;
			if (minStringField > stringField2)
				minStringField = stringField2;
			stringField2 = stringFieldStack.pop();
		}
		
		@Override
		public boolean visit(FieldDeclaration node) {
			field ++;
			org.eclipse.jdt.core.dom.Type type = node.getType();
			if (type instanceof SimpleType && ((SimpleType)type).getName().getFullyQualifiedName().equals("String")) {
				stringField += node.fragments().size();
				stringField2 += node.fragments().size();
			}
			return true;
		}
	}
}
