package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ25 extends QueryTest {

	@Test
	public void testQ25() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int finallys = 0;
		int tryStatements = 0;
		int tryMax = 0;
		int tryMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaTryCheckVisitor visitor = new JavaTryCheckVisitor();
			visitPath(path, visitor);
			int FieldCount = visitor.tryStatements;
			tryStatements += FieldCount;
			finallys += visitor.finallys;
			if (tryMax < visitor.maxTry)
				tryMax = visitor.maxTry;
			if (FieldCount > 0 && tryMin > visitor.minTry)
				tryMin = visitor.minTry;
			files += visitor.classes;
		}
		double mean = (double) tryStatements / files;
		expected += "FinallyTotal[] = " + finallys 
				+ "\nTryMax[] = " + projectId + ", " + (double) tryMax
				+ "\nTryMean[] = " + mean 
				+ "\nTryMin[] = " + projectId + ", " + (double) tryMin 
				+ "\nTryTotal[] = " + tryStatements + "\n";
		queryTest("test/known-good/q25.boa", expected);
	}
	
	public class JavaTryCheckVisitor extends ASTVisitor {
		public int finallys = 0;
		public int tryStatements = 0;
		public int try2 = 0;
		public int classes = 0;
		public int maxTry = 0;
		public int minTry = Integer.MAX_VALUE;
		private Stack<Integer> tryStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				tryStack.push(try2);
				try2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxTry < try2)
				maxTry = try2;
			if (minTry > try2)
				minTry = try2;
			try2 = tryStack.pop();
		}
		
		@Override
		public boolean visit(TryStatement node) {
			tryStatements ++;
			try2 ++;
			if (node.getFinally() != null)
				finallys ++;
			return true;
		}
	}
}
