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

public class TestQ13 extends QueryTest {

	@Test
	public void testQ13() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int methods = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		int interfacesWM = 0;
		int interfaces = 0;
		List<String> snapshot = setPaths();
		String expected = "";
		for (String path : snapshot) {
			JavaInterfaceCheckVisitor visitor = new JavaInterfaceCheckVisitor();
			visitPath(path, visitor);
			int methodCount = visitor.methods;
			methods += methodCount;
			if (methodsMax < visitor.maxMethods)
				methodsMax = visitor.maxMethods;
			if (methodCount > 0 && methodsMin > visitor.minMethods)
				methodsMin = visitor.minMethods;
			interfacesWM += visitor.interfacesWithMethods;
			interfaces += visitor.interfaces;
		}
		// System.out.println("files " + files);
		double mean = (double) methods / interfacesWM;
		expected += "InterfaceMax[] = " + projectId + ", " + (double)interfaces
					+ "\nInterfaceMean[] = " + (double)interfaces 
					+ "\nInterfaceMin[] = " + projectId +  ", " + (double)interfaces
					+ "\nInterfaceTotal[] = " + interfaces 
					+ "\nMethodsInterfaceMax[] = " + projectId + ", " + (double) methodsMax + "\nMethodsInterfaceMean[] = "
					+ mean + "\nMethodsInterfaceMin[] = " + projectId + ", " + (double) methodsMin
					+ "\nMethodsInterfaceTotal[] = " + methods + "\n";
		queryTest("test/known-good/q13.boa", expected);
	}

	public class JavaInterfaceCheckVisitor extends ASTVisitor {
		public int methods = 0;
		public int methods2 = 0;
		public int interfacesWithMethods = 0;
		public int interfaces = 0;
		public int maxMethods = 0;
		public int minMethods = Integer.MAX_VALUE;
		private Stack<Integer> methodsStack = new Stack<Integer>();

		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface()) {
				methodsStack.push(methods2);
				methods2 = 0;
				interfaces++;
				return true;
			}
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration
					|| node instanceof EnumDeclaration) {
				return false;
			}
			return true;
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			if (node.isInterface()) {
				if (methods2 > 0) {
					interfacesWithMethods++;
				}
				if (maxMethods < methods2)
					maxMethods = methods2;
				if (minMethods > methods2)
					minMethods = methods2;
				methods2 = methodsStack.pop();
			} else
				return;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			methods++;
			methods2++;
			return true;
		}
	}
}
