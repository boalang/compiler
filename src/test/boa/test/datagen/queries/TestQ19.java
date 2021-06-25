package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ19 extends QueryTest {
	
	@Test
	public void testQ19() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int methods = 0;
		int methodsMax = 0;
		int methodsMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaFieldCheckVisitor visitor = new JavaFieldCheckVisitor();
			visitPath(path, visitor);
			int methodCount = visitor.fields;
			methods += methodCount;
			if (methodsMax < visitor.maxFields)
				methodsMax = visitor.maxFields;
			if (methodCount > 0 && methodsMin > visitor.minFields)
				methodsMin = visitor.minFields;
			files += visitor.classes;
		}
		double mean = (double) methods / files;
		expected += "FieldMax[] = " + projectId + ", " + (double)methodsMax 
					+ "\nFieldMean[] = " + mean 
					+ "\nFieldMin[] = " + projectId + ", " + (double)methodsMin 
					+ "\nFieldTotal[] = " +  methods + "\n";
		queryTest("test/known-good/q19.boa", expected);
	}
	
	public class JavaFieldCheckVisitor extends ASTVisitor {
		public int fields = 0;
		public int fieds2 = 0;
		public int classes = 0;
		public int maxFields = 0;
		public int minFields = Integer.MAX_VALUE;
		private Stack<Integer> fieldsStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface())
				return false;
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration){
				fieldsStack.push(fieds2);
				fieds2 = 0;
			}
			return true;
		}
		
		@Override
		public void endVisit(AnonymousClassDeclaration node) {
			classes++;
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
			classes++;
			if (maxFields < fieds2)
				maxFields = fieds2;
			if (minFields > fieds2)
				minFields = fieds2;
			fieds2 = fieldsStack.pop();
		}
		
		@Override
		public boolean visit(FieldDeclaration node) {
			int fieldsCur = node.fragments().size();
			fields += fieldsCur;
			fieds2 += fieldsCur;
			return false;
		}
	}
}
