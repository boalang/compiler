package boa.test.datagen.queries;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.junit.Test;

public class TestQ20 extends QueryTest {
	
	@Test
	public void testQ20() throws MissingObjectException, IncorrectObjectTypeException, IOException {
		int transientKW = 0;
		int transientMax = 0;
		int transientMin = Integer.MAX_VALUE;
		int volatileKW = 0;
		int volatileMax = 0;
		int volatileMin = Integer.MAX_VALUE;
		int projectId = 140492550;
		List<String> snapshot = setPaths();
		String expected = "";
		int files = 0;
		for (String path : snapshot) {
			JavaStaticFieldCheckVisitor visitor = new JavaStaticFieldCheckVisitor();
			visitPath(path, visitor);
			int transientCount = visitor.transientKW;
			transientKW += transientCount;
			if (transientMax < visitor.maxTransient)
				transientMax = visitor.maxTransient;
			if (transientMin > visitor.minTransient)
				transientMin = visitor.minTransient;
			int volatileCount = visitor.volatileKW;
			volatileKW += volatileCount;
			if (volatileMax < visitor.maxVolatile)
				volatileMax = visitor.maxVolatile;
			if (volatileMin > visitor.minVolatile)
				volatileMin = visitor.minVolatile;
			files += visitor.classes;
		}
		double transientMean = (double) transientKW / files;
		double volatileMean = (double) volatileKW / files;
		expected += "TransientMax[] = " + projectId + ", " + (double) transientMax 
				+ "\nTransientMean[] = " + transientMean 
				+ "\nTransientMin[] = " + projectId + ", " + (double) transientMin
				+ "\nTransientTotal[] = " + transientKW + "\n" + "VolatileMax[] = " + projectId + ", " + (double) volatileMax 
				+ "\nVolatileMean[] = " + volatileMean 
				+ "\nVolatileMin[] = " + projectId + ", "+ (double) volatileMin 
				+ "\nVolatileTotal[] = " + volatileKW + "\n";
		;
		queryTest("test/known-good/q20.boa", expected);
	}
	
	public class JavaStaticFieldCheckVisitor extends ASTVisitor {
		public int transientKW = 0;
		public int transient2 = 0;
		public int classes = 0;
		public int maxTransient = 0;
		public int minTransient = Integer.MAX_VALUE;
		public int volatileKW = 0;
		public int volatile2 = 0;
		public int maxVolatile = 0;
		public int minVolatile = Integer.MAX_VALUE;
		private Stack<Integer> transientStack = new Stack<Integer>();
		private Stack<Integer> volatileStack = new Stack<Integer>();
		
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof EnumDeclaration|| node instanceof TypeDeclaration && ((TypeDeclaration) node).isInterface()) {
				return false;
			}
			if (node instanceof TypeDeclaration || node instanceof AnonymousClassDeclaration) {
				classes++;
				transientStack.push(transient2);
				transient2 = 0;
				volatileStack.push(transient2);
				volatile2 = 0;
			}
			return true;
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
		//	classes++;
			for (Object d : node.bodyDeclarations()) {
				if (d instanceof FieldDeclaration)
					((FieldDeclaration)d).accept(this);
				if (d instanceof MethodDeclaration)
					((MethodDeclaration)d).accept(this);
			}
			return false;
		}
		
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
		//	classes++;
			for (Object d : node.bodyDeclarations()) {
				if (d instanceof FieldDeclaration)
					((FieldDeclaration)d).accept(this);
				if (d instanceof MethodDeclaration)
					((MethodDeclaration)d).accept(this);
			}
			return false;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			return true;
		}
		
		@Override
		public void endVisit(AnonymousClassDeclaration node) {
			if (maxTransient < transient2)
				maxTransient = transient2;
			if (minTransient > transient2)
				minTransient = transient2;
			transient2 = transientStack.pop();
			if (maxVolatile < volatile2)
				maxVolatile = volatile2;
			if (minVolatile > volatile2)
				minVolatile = volatile2;
			volatile2 = volatileStack.pop();
		}

		@Override
		public void endVisit(TypeDeclaration node) {
			if (node.isInterface())
				return;
			if (maxTransient < transient2)
				maxTransient = transient2;
			if (minTransient > transient2)
				minTransient = transient2;
			transient2 = transientStack.pop();
			if (maxVolatile < volatile2)
				maxVolatile = volatile2;
			if (minVolatile > volatile2)
				minVolatile = volatile2;
			volatile2 = volatileStack.pop();
		}
		
		@Override
		public boolean visit(FieldDeclaration node) {
			for (Object m : node.modifiers()) {
				if (((org.eclipse.jdt.core.dom.Modifier) m).isTransient()) {
					transientKW++;
					transient2++;
				}
				if (((org.eclipse.jdt.core.dom.Modifier) m).isVolatile()) {
					volatileKW++;
					volatile2++;
				}
			}
			return false;
		}
	}
	
	
}
