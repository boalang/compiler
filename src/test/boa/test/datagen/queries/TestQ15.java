package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ15 extends QueryTest {
	
	@Test
	public void testQ15() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int methods = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaVoidMethodCheckVisitor visitor = new JavaVoidMethodCheckVisitor();
			visitPath(path, visitor);
			int methodCount = visitor.methods;
			methods += methodCount;
			if (methodsMax < visitor.maxMethods)
				methodsMax = visitor.maxMethods;
			if (methodCount > 0 && methodsMin > visitor.minMethods)
				methodsMin = visitor.minMethods;
			files += visitor.classes;
		}
		double mean = (double) methods / files;
		expected += "VoidMethodsMax[] = " + projectId + ", " + (double) methodsMax 
				+ "\nVoidMethodsMean[] = " + mean
				+ "\nVoidMethodsMin[] = " + projectId + ", " + (double) methodsMin 
				+ "\nVoidMethodsTotal[] = " + methods + "\n";
		queryTest("test/known-good/q15.boa", expected);
	}

	
	public class JavaVoidMethodCheckVisitor extends ASTVisitor {
		public int methods = 0;
		public int methods2 = 0;
		public int classes = 0;
		public int maxMethods = 0;
		public int minMethods = Integer.MAX_VALUE;
		private Stack<Integer> methodsStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface())
				return false;
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration){
				methodsStack.push(methods2);
				methods2 = 0;
			}
			return true;
		}
		
		@Override
		public void endVisit(AnonymousClassDeclaration node) {
			if (methods2 > 0) {
				classes ++;
				if (maxMethods < methods2)
					maxMethods = methods2;
				if (minMethods > methods2)
					minMethods = methods2;
			}
			
			methods2 = methodsStack.pop();
		}
		
		@Override
		public void endVisit(TypeDeclaration node) {
			if (node.isInterface())
				return;
			if (methods2 > 0) {
				classes ++;
				if (maxMethods < methods2)
					maxMethods = methods2;
				if (minMethods > methods2)
					minMethods = methods2;
			}
			methods2 = methodsStack.pop();
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			org.eclipse.jdt.core.dom.Type type = node.getReturnType2();
			if (type instanceof org.eclipse.jdt.core.dom.PrimitiveType && ((org.eclipse.jdt.core.dom.PrimitiveType) type).getPrimitiveTypeCode().toString().equals("void")) {
				methods++;
				methods2++;
			}
			return true;
		}
	}
}
