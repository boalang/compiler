package boa.datagen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
//
//import org.eclipse.dltk.compiler.IElementRequestor;
//import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
//
//import com.puppycrawl.tools.checkstyle.checks.coding.SuperCloneCheck;
//
//import org.eclipse.dltk.ast.expressions.Expression;
//import org.eclipse.dltk.ast.ASTVisitor;
//import org.eclipse.dltk.ast.declarations.ModuleDeclaration;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;


public class NewPythonVisitor2 extends ASTVisitor {
	
    @Override
    public boolean visitGeneral(ASTNode node) throws Exception {
		System.out.println("Sayem :  "+node.toString());

        return true;
    }
    @Override
    public boolean visit(ASTNode node) throws Exception {
		System.out.println("Sayem :  "+node.toString());

		
        return false;
    }
	
//	public boolean visit(ModuleDeclaration md)
//	{
//		System.out.println("Sayem :  "+md.toString());
//		return true;
//	}
//	
//	public boolean visit(Exception md)
//	{
//		System.out.println("Sayem :  "+md.toString());
//		return true;
//	}
	
}
