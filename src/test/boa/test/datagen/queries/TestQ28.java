package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ28 extends QueryTest {
	
	@Test
	public void testQ28 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int throwStatements = 0;
		int throwMax = 0;
		int throwMin = Integer.MAX_VALUE;
		int projectId = 140492550;

		List<String> snapshot2 = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot2) {
			JavaLocalsCheckVisitor visitor = new JavaLocalsCheckVisitor();
			visitPath(path, visitor);
			int FieldCount = visitor.locals;
			throwStatements += FieldCount;
			if (throwMax < visitor.maxLocals)
				throwMax = visitor.maxLocals;
			if (FieldCount > 0 && throwMin > visitor.minLocals)
				throwMin = visitor.minLocals;
			files += visitor.classes;
		}
		double mean = (double) throwStatements / files;
		expected += "LocalsMax[] = " + projectId + ", " + (double) throwMax 
				+ "\nLocalsMean[] = " + mean
				+ "\nLocalsMin[] = " + projectId + ", " + (double) throwMin 
				+ "\nLocalsTotal[] = " + throwStatements + "\n";
		queryTest("test/known-good/q28.boa", expected);
	}

	public class JavaLocalsCheckVisitor extends ASTVisitor {
		public int locals = 0;
		public int locals2 = 0;
		public int classes = 0;
		public int maxLocals = 0;
		public int minLocals = Integer.MAX_VALUE;
		private Stack<Integer> tryStack = new Stack<Integer>();

		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer
					|| node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				tryStack.push(locals2);
				locals2 = 0;
			}
			return true;
		}

		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}

		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}

		@Override
		public void endVisit(Initializer node) {
			if (maxLocals < locals2)
				maxLocals = locals2;
			if (minLocals > locals2)
				minLocals = locals2;
			locals2 = tryStack.pop();
		}

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			locals += node.fragments().size();
			locals2 += node.fragments().size();
			return true;
		}

		@Override
		public boolean visit(VariableDeclarationExpression node) {
			locals += node.fragments().size();
			locals2 += node.fragments().size();
			return true;
		}

		@Override
		public boolean visit(EnhancedForStatement node) {
			locals++;
			locals2++;
			return true;
		}

		@Override
		public boolean visit(CatchClause node) {
			locals++;
			locals2++;
			return true;
		}

		@Override
		public boolean visit(LambdaExpression node) {
			locals += node.parameters().size();
			locals2 += node.parameters().size();
			return true;
		}
	}
}
