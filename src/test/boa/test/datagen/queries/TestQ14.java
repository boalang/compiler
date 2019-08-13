package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ14 extends QueryTest {

	@Test
	public void testQ14() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int arity = 0;
		int arityMax = 0;
		int arityMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		int methods = 0;
		List<String> snapshot = setPaths();
		String expected = "";
		for (String path : snapshot) {
			JavaArityCheckVisitor visitor = new JavaArityCheckVisitor();
			visitPath(path, visitor);
			int arityCount = visitor.arity;
			arity += arityCount;
			if (arityMax < visitor.maxArity)
				arityMax = visitor.maxArity;
			if (arityCount > 0 && arityMin > visitor.minArity)
				arityMin = visitor.minArity;
			methods += visitor.methods;
		}
		double mean = (double) arity / methods;
		expected += "ArityMax[] = " + projectId + ", " + (double) arityMax 
				+ "\nArityMean[] = " + mean
				+ "\nArityMin[] = " + projectId + ", " + (double) arityMin 
				+ "\nArityTotal[] = " + arity + "\n";
		queryTest("test/known-good/q14.boa", expected);
	}

	public class JavaArityCheckVisitor extends ASTVisitor {
		public int arity = 0;
		public int arity2 = 0;
		public int methods = 0;
		public int interfaces = 0;
		public int maxArity = 0;
		public int minArity = Integer.MAX_VALUE;

		@Override
		public boolean visit(MethodDeclaration node) {
			int arityCurValue = node.parameters().size();
			arity += arityCurValue;
			if (arityCurValue > 0) {
				methods ++;
				if (maxArity < arityCurValue)
					maxArity = arityCurValue;
				if (minArity > arityCurValue)
					minArity = arityCurValue;
			}
			return true;
		}
	}
}
