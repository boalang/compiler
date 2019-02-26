package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ3 extends QueryTest {
	
	@Test
	public void testQ3 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int classes = 0;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		for (String path : snapshot) {
			JavaAbstractCheckVisitor visitor = new JavaAbstractCheckVisitor();
			visitPath(path, visitor);
			classes += visitor.anon;
		}
		double classesMean = (double) classes / 1;
		expected +=  "AnonMax[] = " + projectId + ", " + (double)classes
					+ "\nAnonMean[] = " + classesMean
					+"\nAnonMin[] = " + projectId + ", " + (double)classes
					+"\nAnonTotal[] = " + classes +"\n";
		queryTest("test/known-good/q3.boa", expected);
	}
	
	public class JavaAbstractCheckVisitor extends ASTVisitor {
		public int anon = 0;
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof AnonymousClassDeclaration){
				anon ++;
			}
			return true;
		}

	}
}
