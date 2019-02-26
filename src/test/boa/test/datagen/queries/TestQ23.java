package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ23 extends QueryTest {
	
	@Test
	public void testQ23() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int fields = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaConditionCheckVisitor visitor = new JavaConditionCheckVisitor();
			visitPath(path, visitor);
			int FieldCount = visitor.conditions;
			fields += FieldCount;
			if (methodsMax < visitor.maxFields)
				methodsMax = visitor.maxFields;
			if (FieldCount > 0 && methodsMin > visitor.minFields)
				methodsMin = visitor.minFields;
			files += visitor.classes;
		}
		double mean = (double) fields / files;
		expected += "ConditionMax[] = " + projectId + ", " + (double) methodsMax 
				+ "\nConditionMean[] = " + mean
				+ "\nConditionMin[] = " + projectId + ", " + (double) methodsMin 
				+ "\nConditionTotal[] = " + fields + "\n";
		queryTest("test/known-good/q23.boa", expected);
	}
	
	public class JavaConditionCheckVisitor extends ASTVisitor {
		public int conditions = 0;
		public int condition2 = 0;
		public int classes = 0;
		public int maxFields = 0;
		public int minFields = Integer.MAX_VALUE;
		private Stack<Integer> conditionStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof MethodDeclaration || node instanceof Initializer || node instanceof AnnotationTypeMemberDeclaration) {
				classes++;
				conditionStack.push(condition2);
				condition2 = 0;
			}
			return true;
		}
	
		@Override
		public void endVisit(MethodDeclaration node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public void endVisit(AnnotationTypeMemberDeclaration node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public void endVisit(Initializer node) {
			if (maxFields < condition2)
				maxFields = condition2;
			if (minFields > condition2)
				minFields = condition2;
			condition2 = conditionStack.pop();
		}
		
		@Override
		public boolean visit(IfStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(ForStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		
		@Override
		public boolean visit(DoStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(SwitchStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
		@Override
		public boolean visit(WhileStatement node) {
			conditions ++;
			condition2 ++;
			return true;
		}
		
	}
}
