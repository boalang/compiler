package boa.datagen.util;

import org.eclipse.php.internal.core.ast.nodes.ASTError;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

public class PHPErrorCheckVisitor extends AbstractVisitor{
	public boolean hasError = false;
	
	@Override
	public boolean visit(ASTNode node){
		if ((node.getFlags() & ASTNode.MALFORMED) != 0 || (node.getFlags() & ASTNode.RECOVERED) != 0 || (node instanceof ASTError))
			hasError = true;
		return !hasError;
	}
}
