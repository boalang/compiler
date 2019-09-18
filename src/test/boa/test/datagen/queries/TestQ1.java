package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ1 extends QueryTest {
	
	@Test
	public void testQ1 () throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int abstractKw = 0;
		int classes = 0;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		for (String path : snapshot) {
			JavaAbstractCheckVisitor visitor = new JavaAbstractCheckVisitor();
			visitPath(path, visitor);
			int methodCount = visitor.abstractKw;
			abstractKw += methodCount;
			classes += visitor.classes;
		}
		double abstactMean = (double) abstractKw / 1;
		double classesMean = (double) classes / 1;
		expected += "AbstractMax[] = " + projectId + ", " + (double)abstractKw 
					+ "\nAbstractMean[] = " + abstactMean 
					+ "\nAbstractMin[] = " + projectId + ", " + (double)abstractKw 
					+ "\nAbstractTotal[] = " +  abstractKw 
					+ "\nClassMax[] = " + projectId + ", " + (double)classes
					+ "\nClassMean[] = " + classesMean
					+"\nClassMin[] = " + projectId + ", " + (double)classes
					+"\nClassTotal[] = " + classes
					+"\nProjects[] = 1\n";
		queryTest("test/known-good/q1.boa", expected);
	}
	
	public class JavaAbstractCheckVisitor extends ASTVisitor {
		public int abstractKw = 0;
		public int classes = 0;
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface())
				return false;
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration){
				classes ++;
			}
			return true;
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			if (isAbstract(node.modifiers())) {
			abstractKw ++;
			}
			return true;
		}

		private boolean isAbstract(List<?> modifiers) {
			for (Object m: modifiers) {
				if (m instanceof Modifier)
				if (((Modifier)m).isAbstract())
					return true;
			}
			return false;
		}
	}
}
