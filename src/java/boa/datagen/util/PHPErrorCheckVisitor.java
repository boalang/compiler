package boa.datagen.util;

import org.eclipse.php.internal.core.ast.nodes.ASTError;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.visitor.ApplyAll;

public class PHPErrorCheckVisitor extends ApplyAll{
	public boolean hasError = false;
	

	@Override
	protected boolean apply(ASTNode node) {
		if (node == null || (node instanceof ASTError) || (node.getFlags() & ASTNode.MALFORMED) != 0 || (node.getFlags() & ASTNode.RECOVERED) != 0)
			hasError = true;
		return !hasError;
	}
}
