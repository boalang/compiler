package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ21 extends QueryTest {
	
	@Test
	public void testQ21() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int fields = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaStaticFieldCheckVisitor visitor = new JavaStaticFieldCheckVisitor();
			visitPath(path, visitor);
			int FieldCount = visitor.fields;
			fields += FieldCount;
			if (methodsMax < visitor.maxFields)
				methodsMax = visitor.maxFields;
			if (FieldCount > 0 && methodsMin > visitor.minFields)
				methodsMin = visitor.minFields;
			files += visitor.classes;
		}
		double mean = (double) fields / files;
		expected += "StaticFieldMax[] = " + projectId + ", " + (double) methodsMax 
				+ "\nStaticFieldMean[] = " + mean
				+ "\nStaticFieldMin[] = " + projectId + ", " + (double) methodsMin 
				+ "\nStaticFieldTotal[] = " + fields+ "\n";
		queryTest("test/known-good/q21.boa", expected);
	}
	
	public class JavaStaticFieldCheckVisitor extends ASTVisitor {
		public int fields = 0;
		public int fieds2 = 0;
		public int classes = 0;
		public int maxFields = 0;
		public int minFields = Integer.MAX_VALUE;
		private Stack<Integer> fieldsStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface()) {
				return false;
			}
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration) {
				classes++;
				fieldsStack.push(fieds2);
				fieds2 = 0;
			}
			return true;
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
		//	classes++;
			for (Object d : node.bodyDeclarations()) {
				if (d instanceof FieldDeclaration)
					((FieldDeclaration)d).accept(this);
				if (d instanceof MethodDeclaration)
					((MethodDeclaration)d).accept(this);
			}
			return false;
		}
		
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
		//	classes++;
			for (Object d : node.bodyDeclarations()) {
				if (d instanceof FieldDeclaration)
					((FieldDeclaration)d).accept(this);
				if (d instanceof MethodDeclaration)
					((MethodDeclaration)d).accept(this);
			}
			return false;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			return true;
		}
		
		@Override
		public void endVisit(AnonymousClassDeclaration node) {
			if (maxFields < fieds2)
				maxFields = fieds2;
			if (minFields > fieds2)
				minFields = fieds2;
			fieds2 = fieldsStack.pop();
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			if (node.isInterface())
				return;
			if (maxFields < fieds2)
				maxFields = fieds2;
			if (minFields > fieds2)
				minFields = fieds2;
			fieds2 = fieldsStack.pop();
		}
		
		@Override
		public boolean visit(FieldDeclaration node) {
			for (Object m : node.modifiers()) {
				if (((org.eclipse.jdt.core.dom.Modifier) m).isStatic()) {
					fields++;
					fieds2++;
				}
			}
			return false;
		}
	}
}
