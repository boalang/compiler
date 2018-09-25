package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ18 extends QueryTest {

	@Test
	public void testQ18() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int thisKeyword = 0;
		int thisMax = 0;
		int thisMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int methods = 0;
		for (String path : snapshot) {
			JavaVoidThisCheckVisitor visitor = new JavaVoidThisCheckVisitor();
			visitPath(path, visitor);
			int thisCount = visitor.thisKeyword;
			thisKeyword += thisCount;
			if (thisMax < visitor.maxThis)
				thisMax = visitor.maxThis;
			if (thisCount > 0 && thisMin > visitor.minThis)
				thisMin = visitor.minThis;
			methods += visitor.methods;
		}
		double mean = (double) thisKeyword / methods;
		expected += "ThisMax[] = " + projectId + ", " + (double) thisMax 
				+ "\nThisMean[] = " + mean
				+ "\nThisMin[] = " + projectId + ", " + (double) thisMin 
				+ "\nThisTotal[] = " + thisKeyword + "\n";
		queryTest("test/known-good/q18.boa", expected);
	}

	public class JavaVoidThisCheckVisitor extends ASTVisitor {
		public int thisKeyword = 0;
		public int this2 = 0;
		public int methods = 0;
		public int maxThis = 0;
		public int minThis = Integer.MAX_VALUE;
		private Stack<Integer> thisStack = new Stack<Integer>();

		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			methods++;
			if (maxThis < this2)
				maxThis = this2;
			if (minThis > this2)
				minThis = this2;
			this2 = thisStack.pop();
		}

		@Override
		public boolean visit(AnnotationTypeMemberDeclaration node) {
			thisStack.push(this2);
			this2 = 0;
			return true;
		}
		@Override
		public void endVisit(MethodDeclaration node) {
			methods++;
			if (maxThis < this2)
				maxThis = this2;
			if (minThis > this2)
				minThis = this2;
			this2 = thisStack.pop();
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			thisStack.push(this2);
			this2 = 0;
			return true;
		}
		
		
		public boolean visit(ThisExpression node) {
			thisKeyword ++;
			this2++;
			return true;
		}
	}
}
