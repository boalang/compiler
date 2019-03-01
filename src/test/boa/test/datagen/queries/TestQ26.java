package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ26 extends QueryTest {
	
	@Test
	public void testQ26() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int throwStatements = 0;
		int throwMax = 0;
		int throwMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaThrowCheckVisitor visitor = new JavaThrowCheckVisitor();
			visitPath(path, visitor);
			int FieldCount = visitor.throwStatements;
			throwStatements += FieldCount;
			if (throwMax < visitor.maxThrow)
				throwMax = visitor.maxThrow;
			if (FieldCount > 0 && throwMin > visitor.minThrow)
				throwMin = visitor.minThrow;
			files += visitor.classes;
		}
		double mean = (double) throwStatements / files;
		expected += "ThrowMax[] = " + projectId + ", " + (double) throwMax 
				+ "\nThrowMean[] = " + mean
				+ "\nThrowMin[] = " + projectId + ", " + (double) throwMin 
				+ "\nThrowTotal[] = " + throwStatements + "\n";
		queryTest("test/known-good/q26.boa", expected);
	}
	
	public class JavaThrowCheckVisitor extends ASTVisitor {
		public int throwStatements = 0;
		public int throw2 = 0;
		public int classes = 0;
		public int maxThrow = 0;
		public int minThrow = Integer.MAX_VALUE;
		private Stack<Integer> tryStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				tryStack.push(throw2);
				throw2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxThrow < throw2)
				maxThrow = throw2;
			if (minThrow > throw2)
				minThrow = throw2;
			throw2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxThrow < throw2)
				maxThrow = throw2;
			if (minThrow > throw2)
				minThrow = throw2;
			throw2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxThrow < throw2)
				maxThrow = throw2;
			if (minThrow > throw2)
				minThrow = throw2;
			throw2 = tryStack.pop();
		}
		
		@Override
		public boolean visit(ThrowStatement node) {
			throwStatements ++;
			throw2 ++;
			return true;
		}
	}
}
